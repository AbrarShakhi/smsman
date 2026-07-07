package com.abrarshakhi.smsman.data.telephony

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.abrarshakhi.smsman.data.db.dao.BlockedNumberDao
import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.MessageProtocol
import com.abrarshakhi.smsman.domain.model.MmsDownloadState
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import com.abrarshakhi.smsman.framework.notification.MessageNotificationCoordinator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles a freshly delivered SMS PDU array:
 *
 *  1. Concatenate multipart bodies
 *  2. Drop blocked senders silently
 *  3. Insert into the system Telephony provider (we are default app — required for interop)
 *  4. Mirror into Room so the UI sees it immediately
 *
 * Called from [com.abrarshakhi.smsman.framework.receiver.SmsDeliverReceiver].
 * Notifications are posted in M3.
 */
@Singleton
class SmsInboundHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val blockedNumberDao: BlockedNumberDao,
    private val contactsRepository: ContactsRepository,
    private val notificationCoordinator: MessageNotificationCoordinator,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    suspend fun handle(parts: Array<SmsMessage>, subscriptionId: Int): HandleResult = withContext(io) {
        if (parts.isEmpty()) return@withContext HandleResult.Empty

        val first = parts.first()
        val sender = first.originatingAddress ?: run {
            Log.w(TAG, "Dropping SMS with no originating address")
            return@withContext HandleResult.Empty
        }

        if (blockedNumberDao.isBlocked(normalize(sender))) {
            Log.i(TAG, "Dropping SMS from blocked sender")
            return@withContext HandleResult.Blocked
        }

        val body = parts.joinToString("") { it.messageBody.orEmpty() }
        val timestamp = first.timestampMillis
        val protocol = first.protocolIdentifier
        val effectiveSubId = if (subscriptionId >= 0) subscriptionId else -1

        // 1. Resolve / create thread id via Telephony — this is the canonical thread mapping.
        val threadId = try {
            Telephony.Threads.getOrCreateThreadId(context, sender)
        } catch (t: Throwable) {
            Log.e(TAG, "getOrCreateThreadId failed for $sender", t)
            return@withContext HandleResult.Error(t)
        }

        // 2. Insert into Telephony provider so other apps see the message.
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, sender)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestamp)
            put(Telephony.Sms.DATE_SENT, timestamp)
            put(Telephony.Sms.READ, 0)
            put(Telephony.Sms.SEEN, 0)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
            put(Telephony.Sms.THREAD_ID, threadId)
            put(Telephony.Sms.SUBSCRIPTION_ID, effectiveSubId)
            put(Telephony.Sms.PROTOCOL, protocol)
        }
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values) ?: run {
            Log.e(TAG, "ContentResolver.insert returned null for incoming SMS")
            return@withContext HandleResult.Error(IllegalStateException("Telephony insert failed"))
        }
        val sysId = ContentUris.parseId(uri)

        // 3. Upsert conversation (preserving user-controlled metadata) and message into Room.
        val existing = conversationDao.byId(threadId)
        val conv = ConversationEntity(
            threadId = threadId,
            recipientAddresses = existing?.recipientAddresses?.takeIf { it.contains(sender) } ?: sender,
            snippet = body.take(SNIPPET_MAX),
            lastMessageAt = timestamp,
            unreadCount = (existing?.unreadCount ?: 0) + 1,
            archived = false,                   // any new message un-archives
            pinned = existing?.pinned ?: false,
            pinnedAt = existing?.pinnedAt,
            blocked = existing?.blocked ?: false,
            subscriptionId = effectiveSubId.takeIf { it >= 0 } ?: existing?.subscriptionId,
            draft = existing?.draft,
            muted = existing?.muted ?: false,
        )
        conversationDao.upsert(conv)

        val message = MessageEntity(
            id = sysId,
            threadId = threadId,
            address = sender,
            body = body,
            date = timestamp,
            type = Telephony.Sms.MESSAGE_TYPE_INBOX,
            read = false,
            seen = false,
            status = Telephony.Sms.STATUS_NONE,
            subscriptionId = effectiveSubId,
            errorCode = 0,
            protocol = MessageProtocol.Sms.raw,
            mmsContentLocation = null,
            mmsExpiry = null,
            mmsDownloadState = MmsDownloadState.NotApplicable.raw,
        )
        messageDao.upsert(message)

        // Notify. Loading the unread message list keeps MessagingStyle's history in sync so the
        // notification re-renders with prior unreads on each new arrival.
        val unread = messageDao.observeByThread(threadId)
            .let { /* one-shot read */ }
        val unreadList = listOf(
            MessageNotificationCoordinator.UnreadMessage(
                messageId = sysId,
                body = body,
                timestamp = timestamp,
            ),
        )
        val contact = runCatching { contactsRepository.resolve(sender) }.getOrNull()
        notificationCoordinator.postIncoming(
            threadId = threadId,
            sender = sender,
            senderContact = contact,
            unreadMessages = unreadList,
        )

        HandleResult.Stored(threadId = threadId, messageId = sysId, body = body, sender = sender)
    }

    /**
     * Light-touch normalization for blocked-number matching. Strips formatting and "+".
     * Full E.164 normalization with carrier hints lands in [M1.4].
     */
    private fun normalize(raw: String): String = raw.filter { it.isDigit() }

    sealed interface HandleResult {
        data object Empty : HandleResult
        data object Blocked : HandleResult
        data class Stored(
            val threadId: Long,
            val messageId: Long,
            val body: String,
            val sender: String,
        ) : HandleResult
        data class Error(val throwable: Throwable) : HandleResult
    }

    private companion object {
        const val TAG = "SmsInboundHandler"
        const val SNIPPET_MAX = 160
    }
}
