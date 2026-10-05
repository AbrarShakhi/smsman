package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.database.MessageFingerprint
import com.abrarshakhi.smsman.core.database.dao.ConversationMetaDao
import com.abrarshakhi.smsman.core.database.dao.MessageMetaDao
import com.abrarshakhi.smsman.core.database.entity.ConversationMetaEntity
import com.abrarshakhi.smsman.core.database.entity.MessageMetaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MessageMetadataRepository(
    private val conversationMetaDao: ConversationMetaDao,
    private val messageMetaDao: MessageMetaDao,
) {

    fun observeFavoriteThreadIds(): Flow<Set<Long>> =
        conversationMetaDao.observeFavoriteThreadIds().map { it.toSet() }

    fun observeAllPinned(): Flow<List<MessageMetaEntity>> = messageMetaDao.observeAllPinned()

    fun observePinnedIdsInThread(threadId: Long): Flow<Set<Long>> =
        messageMetaDao.observePinnedIdsInThread(threadId).map { it.toSet() }

    suspend fun setFavorite(threadId: Long, favorite: Boolean, recipientKey: String? = null) {
        val existing = conversationMetaDao.find(threadId)
        conversationMetaDao.upsert(
            (existing ?: ConversationMetaEntity(threadId = threadId)).copy(
                isFavorite = favorite,
                favoritedAt = if (favorite) System.currentTimeMillis() else null,
                recipientKey = recipientKey ?: existing?.recipientKey,
            ),
        )
    }

    suspend fun pin(messageId: Long, threadId: Long, date: Long, type: Int, body: String) {
        messageMetaDao.upsert(
            MessageMetaEntity(
                messageId = messageId,
                threadId = threadId,
                fingerprint = MessageFingerprint.of(threadId, date, type, body),
                pinnedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun unpin(messageId: Long) = messageMetaDao.unpin(messageId)

    suspend fun forgetThread(threadId: Long) {
        conversationMetaDao.delete(threadId)
        messageMetaDao.prune(messageMetaDao.observePinnedIdsInThread(threadId).first())
    }

    suspend fun prunePins(missingMessageIds: List<Long>) {
        if (missingMessageIds.isNotEmpty()) messageMetaDao.prune(missingMessageIds)
    }
}
