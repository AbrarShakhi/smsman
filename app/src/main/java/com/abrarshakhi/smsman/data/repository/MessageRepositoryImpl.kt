package com.abrarshakhi.smsman.data.repository

import android.content.Context
import android.provider.Telephony
import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import com.abrarshakhi.smsman.data.db.mapper.toDomain
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.Message
import com.abrarshakhi.smsman.domain.model.MessageProtocol
import com.abrarshakhi.smsman.domain.model.MmsDownloadState
import com.abrarshakhi.smsman.domain.model.SendRequest
import com.abrarshakhi.smsman.domain.model.SendResult
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import com.abrarshakhi.smsman.domain.repository.SettingsRepository
import com.abrarshakhi.smsman.framework.sender.SmsSender
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val smsSender: SmsSender,
    private val settingsRepository: SettingsRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : MessageRepository {

    override fun observeByThread(threadId: Long): Flow<List<Message>> =
        messageDao.observeByThread(threadId).map { list -> list.map { it.toDomain() } }

    override suspend fun byId(id: Long): Message? = messageDao.byId(id)?.toDomain()

    override suspend fun send(request: SendRequest): SendResult = withContext(io) {
        if (request.recipients.isEmpty() || request.body.isBlank()) {
            return@withContext SendResult.Failed("Empty recipient or body")
        }

        // For SMS we send to a single recipient. Group SMS is technically supported by the
        // SmsManager API but real-world delivery semantics are inconsistent — group goes via MMS
        // in M4. Here we treat the first recipient as the target.
        val recipient = request.recipients.first()
        val now = System.currentTimeMillis()

        try {
            val threadId = request.threadId ?: Telephony.Threads.getOrCreateThreadId(context, recipient)

            val sysId = smsSender.insertOutbox(
                address = recipient,
                body = request.body,
                timestamp = now,
                threadId = threadId,
                subscriptionId = request.subscriptionId,
            )

            // Mirror into Room before SmsManager fires the broadcasts so the UI sees the outbound
            // bubble immediately.
            val existing = conversationDao.byId(threadId)
            conversationDao.upsert(
                ConversationEntity(
                    threadId = threadId,
                    recipientAddresses = existing?.recipientAddresses?.takeIf { it.contains(recipient) } ?: recipient,
                    snippet = request.body.take(SNIPPET_MAX),
                    lastMessageAt = now,
                    unreadCount = existing?.unreadCount ?: 0,
                    archived = false,                     // sending un-archives
                    pinned = existing?.pinned ?: false,
                    pinnedAt = existing?.pinnedAt,
                    blocked = existing?.blocked ?: false,
                    subscriptionId = request.subscriptionId.takeIf { it >= 0 } ?: existing?.subscriptionId,
                    draft = null,                          // sending clears the draft
                    muted = existing?.muted ?: false,
                ),
            )
            messageDao.upsert(
                MessageEntity(
                    id = sysId,
                    threadId = threadId,
                    address = recipient,
                    body = request.body,
                    date = now,
                    type = Telephony.Sms.MESSAGE_TYPE_OUTBOX,
                    read = true,
                    seen = true,
                    status = Telephony.Sms.STATUS_PENDING,
                    subscriptionId = request.subscriptionId,
                    errorCode = 0,
                    protocol = MessageProtocol.Sms.raw,
                    mmsContentLocation = null,
                    mmsExpiry = null,
                    mmsDownloadState = MmsDownloadState.NotApplicable.raw,
                ),
            )

            val requestDelivery = settingsRepository.settings.first().requestDeliveryReports
            val parts = smsSender.send(
                address = recipient,
                body = request.body,
                subscriptionId = request.subscriptionId,
                messageId = sysId,
                requestDeliveryReport = requestDelivery,
            )

            SendResult.Queued(messageId = sysId, threadId = threadId, parts = parts)
        } catch (t: Throwable) {
            SendResult.Failed(reason = t.message ?: "Send failed", cause = t)
        }
    }

    override suspend fun delete(ids: Set<Long>) = withContext(io) {
        if (ids.isEmpty()) return@withContext
        // Cascade in Telephony first so other apps see the deletion.
        val where = "${Telephony.Sms._ID} IN (${ids.joinToString(",")})"
        context.contentResolver.delete(Telephony.Sms.CONTENT_URI, where, null)
        messageDao.delete(ids.toList())
    }

    override suspend fun markThreadRead(threadId: Long): Unit = withContext(io) {
        messageDao.markThreadRead(threadId)
        conversationDao.markRead(threadId)
        // Mirror to Telephony so the system unread count agrees.
        val where = "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0"
        val values = android.content.ContentValues().apply {
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
        }
        context.contentResolver.update(Telephony.Sms.CONTENT_URI, values, where, arrayOf(threadId.toString()))
    }

    override suspend fun saveDraft(threadId: Long, body: String?) = withContext(io) {
        conversationDao.updateDraft(threadId, body?.takeIf { it.isNotBlank() })
    }

    private companion object {
        const val SNIPPET_MAX = 160
    }
}
