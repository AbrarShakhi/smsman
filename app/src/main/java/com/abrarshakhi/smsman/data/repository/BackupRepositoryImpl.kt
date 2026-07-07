package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.BackupConversation
import com.abrarshakhi.smsman.domain.model.BackupData
import com.abrarshakhi.smsman.domain.model.BackupMessage
import com.abrarshakhi.smsman.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : BackupRepository {

    override suspend fun export(outputStream: OutputStream): Int = withContext(io) {
        val conversations = conversationDao.getAll()
        val messages = messageDao.getAll()
        val data = BackupData(
            exportedAt = System.currentTimeMillis(),
            conversations = conversations.map { it.toBackup() },
            messages = messages.map { it.toBackup() },
        )
        outputStream.bufferedWriter().use { it.write(Json.encodeToString(BackupData.serializer(), data)) }
        messages.size
    }

    override suspend fun import(inputStream: InputStream): Int = withContext(io) {
        val json = inputStream.bufferedReader().use { it.readText() }
        val data = runCatching {
            Json.decodeFromString(BackupData.serializer(), json)
        }.getOrNull() ?: return@withContext 0

        conversationDao.upsertAll(data.conversations.map { it.toEntity() })
        messageDao.upsertAll(data.messages.map { it.toEntity() })
        data.messages.size
    }

    private fun ConversationEntity.toBackup() = BackupConversation(
        threadId = threadId,
        recipientAddresses = recipientAddresses,
        snippet = snippet,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        archived = archived,
        pinned = pinned,
        blocked = blocked,
        muted = muted,
    )

    private fun MessageEntity.toBackup() = BackupMessage(
        id = id,
        threadId = threadId,
        address = address,
        body = body,
        date = date,
        type = type,
        read = read,
        status = status,
        subscriptionId = subscriptionId,
        protocol = protocol,
    )

    private fun BackupConversation.toEntity() = ConversationEntity(
        threadId = threadId,
        recipientAddresses = recipientAddresses,
        snippet = snippet,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        archived = archived,
        pinned = pinned,
        pinnedAt = null,
        blocked = blocked,
        subscriptionId = null,
        draft = null,
        muted = muted,
    )

    private fun BackupMessage.toEntity() = MessageEntity(
        id = id,
        threadId = threadId,
        address = address,
        body = body,
        date = date,
        type = type,
        read = read,
        seen = read,
        status = status,
        subscriptionId = subscriptionId,
        errorCode = 0,
        protocol = protocol,
        mmsContentLocation = null,
        mmsExpiry = null,
        mmsDownloadState = 0,
    )
}
