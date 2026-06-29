package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.ScheduledMessage
import kotlinx.coroutines.flow.Flow

interface ScheduledMessageRepository {
    fun observePending(): Flow<List<ScheduledMessage>>
    suspend fun byId(id: Long): ScheduledMessage?
    suspend fun schedule(request: ScheduleRequest): Long
    suspend fun cancel(id: Long)
    suspend fun markSent(id: Long)
    suspend fun markFailed(id: Long, error: String)
}

data class ScheduleRequest(
    val threadId: Long?,
    val recipients: List<String>,
    val body: String,
    val scheduledAt: Long,
    val subscriptionId: Int = -1,
)
