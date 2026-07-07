package com.abrarshakhi.smsman.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.abrarshakhi.smsman.data.db.dao.ScheduledMessageDao
import com.abrarshakhi.smsman.data.db.entity.ScheduledMessageEntity
import com.abrarshakhi.smsman.di.IoDispatcher
import com.abrarshakhi.smsman.domain.model.ScheduledMessage
import com.abrarshakhi.smsman.domain.model.ScheduledStatus
import com.abrarshakhi.smsman.domain.repository.ScheduleRequest
import com.abrarshakhi.smsman.domain.repository.ScheduledMessageRepository
import com.abrarshakhi.smsman.framework.worker.ScheduledSendWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledMessageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ScheduledMessageDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ScheduledMessageRepository {

    override fun observePending(): Flow<List<ScheduledMessage>> =
        dao.observePending().map { list -> list.map { it.toDomain() } }

    override suspend fun byId(id: Long): ScheduledMessage? = withContext(io) {
        dao.byId(id)?.toDomain()
    }

    override suspend fun schedule(request: ScheduleRequest): Long = withContext(io) {
        val now = System.currentTimeMillis()
        val initialDelay = (request.scheduledAt - now).coerceAtLeast(0L)

        // Pre-insert with placeholder workRequestId so we can put a real one on right after.
        val pending = ScheduledMessageEntity(
            threadId = request.threadId,
            recipients = request.recipients.joinToString(","),
            body = request.body,
            attachmentsJson = Json.encodeToString(emptyList<String>()),
            scheduledAt = request.scheduledAt,
            subscriptionId = request.subscriptionId,
            workRequestId = "",
            status = ScheduledStatus.Pending.ordinal,
            lastErrorMessage = null,
        )
        val id = dao.insert(pending)

        val req = OneTimeWorkRequestBuilder<ScheduledSendWorker>()
            .setInputData(Data.Builder().putLong(ScheduledSendWorker.KEY_ID, id).build())
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG_PREFIX + id)
            .build()
        WorkManager.getInstance(context).enqueue(req)
        dao.upsert(pending.copy(id = id, workRequestId = req.id.toString()))
        id
    }

    override suspend fun cancel(id: Long) = withContext(io) {
        val row = dao.byId(id) ?: return@withContext
        runCatching {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_PREFIX + id)
        }
        dao.updateStatus(id, ScheduledStatus.Cancelled.ordinal, null)
    }

    override suspend fun markSent(id: Long) = withContext(io) {
        dao.updateStatus(id, ScheduledStatus.Sent.ordinal, null)
    }

    override suspend fun markFailed(id: Long, error: String) = withContext(io) {
        dao.updateStatus(id, ScheduledStatus.Failed.ordinal, error)
    }

    private fun ScheduledMessageEntity.toDomain(): ScheduledMessage = ScheduledMessage(
        id = id,
        threadId = threadId,
        recipients = recipients.split(',').filter { it.isNotBlank() },
        body = body,
        attachments = runCatching {
            Json.decodeFromString<List<String>>(attachmentsJson)
        }.getOrDefault(emptyList()),
        scheduledAt = scheduledAt,
        subscriptionId = subscriptionId,
        workRequestId = workRequestId,
        status = ScheduledStatus.entries.getOrElse(status) { ScheduledStatus.Pending },
        lastErrorMessage = lastErrorMessage,
    )

    private companion object {
        const val WORK_TAG_PREFIX = "scheduled_msg_"
    }
}
