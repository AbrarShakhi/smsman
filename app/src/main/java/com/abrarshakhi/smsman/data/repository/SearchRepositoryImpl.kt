package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.data.db.mapper.toDomain
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.SearchHit
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import com.abrarshakhi.smsman.domain.repository.SearchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val contactsRepository: ContactsRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SearchRepository {

    override suspend fun searchMessages(query: String, limit: Int): List<SearchHit> = withContext(io) {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LEN) return@withContext emptyList()

        val rawHits = messageDao.search(trimmed, limit)
        if (rawHits.isEmpty()) return@withContext emptyList()

        val threadIds = rawHits.map { it.threadId }.toSet()
        val conversations = threadIds
            .mapNotNull { conversationDao.byId(it) }
            .associateBy { it.threadId }

        // Resolve contacts for the first recipient of each thread in one batch.
        val firstAddresses = conversations.values
            .mapNotNull { it.recipientAddresses.split(',').firstOrNull()?.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        val contacts = contactsRepository.resolveAll(firstAddresses)

        rawHits.mapNotNull { msg ->
            val conv = conversations[msg.threadId] ?: return@mapNotNull null
            val addr = conv.recipientAddresses.split(',').firstOrNull()?.trim().orEmpty()
            SearchHit(
                message = msg.toDomain(),
                conversation = conv.toDomain(),
                contact = contacts[addr],
            )
        }
    }

    override suspend fun searchInThread(threadId: Long, query: String): List<SearchHit> = withContext(io) {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LEN) return@withContext emptyList()
        val rawHits = messageDao.searchInThread(threadId, trimmed)
        if (rawHits.isEmpty()) return@withContext emptyList()
        val conv = conversationDao.byId(threadId) ?: return@withContext emptyList()
        val addr = conv.recipientAddresses.split(',').firstOrNull()?.trim().orEmpty()
        val contact = contactsRepository.resolve(addr)
        rawHits.map { msg ->
            SearchHit(message = msg.toDomain(), conversation = conv.toDomain(), contact = contact)
        }
    }

    private companion object {
        const val MIN_QUERY_LEN = 2
    }
}
