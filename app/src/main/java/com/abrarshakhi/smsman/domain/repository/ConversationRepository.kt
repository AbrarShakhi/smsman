package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.Conversation
import com.abrarshakhi.smsman.domain.model.ConversationFilter
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observe(filter: ConversationFilter): Flow<List<Conversation>>
    suspend fun byId(threadId: Long): Conversation?
    fun observeById(threadId: Long): Flow<Conversation?>
    suspend fun markRead(threadId: Long)
    suspend fun setArchived(threadId: Long, archived: Boolean)
    suspend fun setPinned(threadId: Long, pinned: Boolean)
    suspend fun setBlocked(threadId: Long, blocked: Boolean)
    suspend fun setMuted(threadId: Long, muted: Boolean)
    suspend fun delete(threadIds: Set<Long>)
}
