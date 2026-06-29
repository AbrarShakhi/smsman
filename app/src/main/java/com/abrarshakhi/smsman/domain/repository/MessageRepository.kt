package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.Message
import com.abrarshakhi.smsman.domain.model.SendRequest
import com.abrarshakhi.smsman.domain.model.SendResult
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeByThread(threadId: Long): Flow<List<Message>>
    suspend fun byId(id: Long): Message?
    suspend fun send(request: SendRequest): SendResult
    suspend fun delete(ids: Set<Long>)
    suspend fun markThreadRead(threadId: Long)
    suspend fun saveDraft(threadId: Long, body: String?)
}
