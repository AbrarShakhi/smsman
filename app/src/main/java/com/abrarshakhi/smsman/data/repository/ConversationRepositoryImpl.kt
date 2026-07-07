package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.db.dao.ConversationDao
import com.abrarshakhi.smsman.data.db.mapper.toDomain
import com.abrarshakhi.smsman.domain.model.Conversation
import com.abrarshakhi.smsman.domain.model.ConversationFilter
import com.abrarshakhi.smsman.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val dao: ConversationDao,
) : ConversationRepository {

    override fun observe(filter: ConversationFilter): Flow<List<Conversation>> {
        val source = when (filter) {
            ConversationFilter.All -> dao.observeActive()
            ConversationFilter.Unread -> dao.observeUnread()
            ConversationFilter.Archived -> dao.observeArchived()
            ConversationFilter.Blocked -> dao.observeBlocked()
        }
        return source.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun byId(threadId: Long): Conversation? =
        dao.byId(threadId)?.toDomain()

    override fun observeById(threadId: Long): Flow<Conversation?> =
        dao.observeById(threadId).map { it?.toDomain() }

    override suspend fun markRead(threadId: Long) = dao.markRead(threadId)

    override suspend fun setArchived(threadId: Long, archived: Boolean) =
        dao.setArchived(threadId, archived)

    override suspend fun setPinned(threadId: Long, pinned: Boolean) =
        dao.setPinned(threadId, pinned, if (pinned) currentTimeMillis() else null)

    override suspend fun setBlocked(threadId: Long, blocked: Boolean) =
        dao.setBlocked(threadId, blocked)

    override suspend fun setMuted(threadId: Long, muted: Boolean) =
        dao.setMuted(threadId, muted)

    override suspend fun delete(threadIds: Set<Long>) =
        dao.delete(threadIds.toList())
}
