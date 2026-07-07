package com.abrarshakhi.smsman.framework.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.abrarshakhi.smsman.domain.model.SendRequest
import com.abrarshakhi.smsman.domain.model.SendResult
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import com.abrarshakhi.smsman.domain.repository.ScheduledMessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduledSendWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduledRepository: ScheduledMessageRepository,
    private val messageRepository: MessageRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, -1L).takeIf { it >= 0 } ?: return Result.failure()
        val scheduled = scheduledRepository.byId(id) ?: return Result.failure()

        return when (val result = messageRepository.send(
            SendRequest(
                threadId = scheduled.threadId,
                recipients = scheduled.recipients,
                body = scheduled.body,
                subscriptionId = scheduled.subscriptionId,
            ),
        )) {
            is SendResult.Queued -> {
                scheduledRepository.markSent(id)
                Log.i(TAG, "Scheduled message $id dispatched (messageId=${result.messageId})")
                Result.success()
            }
            is SendResult.Failed -> {
                scheduledRepository.markFailed(id, result.reason)
                Log.e(TAG, "Scheduled message $id failed: ${result.reason}")
                Result.retry()
            }
        }
    }

    companion object {
        const val KEY_ID = "scheduled_id"
        private const val TAG = "ScheduledSendWorker"
    }
}
