package com.abrarshakhi.smsman.data.telephony

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.MessageProtocol
import com.abrarshakhi.smsman.domain.model.MmsDownloadState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads SMS rows from the system Telephony provider into Room. Idempotent (uses upsert).
 *
 * Caps the import at the most recent [MAX_MESSAGES] rows so that first-launch on a phone with
 * tens of thousands of messages doesn't block the UI. Older history stays in the Telephony
 * provider and is searchable on demand; only the recent slice is mirrored locally for fast
 * Compose rendering.
 *
 * MMS is intentionally not imported here — that arrives in M4. MMS rows in Telephony are joined
 * via `content://mms-sms/conversations`, which has its own column model.
 */
@Singleton
class TelephonyImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    suspend fun importRecent(): ImportResult = withContext(io) {
        val resolver = context.contentResolver
        val messages = readMessages(resolver)
        if (messages.isEmpty()) return@withContext ImportResult(0, 0)

        val threads = buildConversations(messages)
        // FK: conversations must exist before messages.
        conversationDao.upsertAll(threads)
        messageDao.upsertAll(messages)
        ImportResult(threadCount = threads.size, messageCount = messages.size)
    }

    private suspend fun readMessages(resolver: ContentResolver): List<MessageEntity> {
        val cursor = resolver.query(
            Telephony.Sms.CONTENT_URI,
            PROJECTION,
            null,
            null,
            "${Telephony.Sms.DATE} DESC LIMIT $MAX_MESSAGES",
        ) ?: return emptyList()

        return cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow(Telephony.Sms._ID)
            val threadIdIdx = c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIdx = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIdx = c.getColumnIndexOrThrow(Telephony.Sms.READ)
            val seenIdx = c.getColumnIndexOrThrow(Telephony.Sms.SEEN)
            val statusIdx = c.getColumnIndexOrThrow(Telephony.Sms.STATUS)
            val subIdIdx = c.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)
            val errIdx = c.getColumnIndex(Telephony.Sms.ERROR_CODE)

            buildList(c.count) {
                while (c.moveToNext()) {
                    val address = c.getString(addressIdx) ?: continue
                    add(
                        MessageEntity(
                            id = c.getLong(idIdx),
                            threadId = c.getLong(threadIdIdx),
                            address = address,
                            body = if (c.isNull(bodyIdx)) null else c.getString(bodyIdx),
                            date = c.getLong(dateIdx),
                            type = c.getInt(typeIdx),
                            read = c.getInt(readIdx) == 1,
                            seen = c.getInt(seenIdx) == 1,
                            status = c.getInt(statusIdx),
                            subscriptionId = if (subIdIdx >= 0) c.getInt(subIdIdx) else -1,
                            errorCode = if (errIdx >= 0) c.getInt(errIdx) else 0,
                            protocol = MessageProtocol.Sms.raw,
                            mmsContentLocation = null,
                            mmsExpiry = null,
                            mmsDownloadState = MmsDownloadState.NotApplicable.raw,
                        ),
                    )
                }
            }
        }
    }

    private suspend fun buildConversations(messages: List<MessageEntity>): List<ConversationEntity> {
        // Group messages by thread to derive snippet, lastMessageAt, unreadCount.
        val byThread = messages.groupBy { it.threadId }
        return byThread.map { (threadId, msgs) ->
            val latest = msgs.maxBy { it.date }
            val unread = msgs.count {
                !it.read && it.type == Telephony.Sms.MESSAGE_TYPE_INBOX
            }
            val existing = conversationDao.byId(threadId)
            ConversationEntity(
                threadId = threadId,
                recipientAddresses = latest.address,
                snippet = latest.body?.take(SNIPPET_MAX) ?: "",
                lastMessageAt = latest.date,
                unreadCount = unread,
                // Preserve user-controlled metadata across re-imports.
                archived = existing?.archived ?: false,
                pinned = existing?.pinned ?: false,
                pinnedAt = existing?.pinnedAt,
                blocked = existing?.blocked ?: false,
                subscriptionId = latest.subscriptionId.takeIf { it >= 0 } ?: existing?.subscriptionId,
                draft = existing?.draft,
                muted = existing?.muted ?: false,
            )
        }
    }

    data class ImportResult(val threadCount: Int, val messageCount: Int)

    private companion object {
        const val MAX_MESSAGES = 1000
        const val SNIPPET_MAX = 160

        val PROJECTION = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ,
            Telephony.Sms.SEEN,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBSCRIPTION_ID,
            Telephony.Sms.ERROR_CODE,
        )
    }
}
