package com.abrarshakhi.smsman.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteInput
import com.abrarshakhi.smsman.domain.model.SendRequest
import com.abrarshakhi.smsman.domain.model.SendResult
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import com.abrarshakhi.smsman.framework.notification.MessageNotificationCoordinator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when the user taps "Reply" in a notification and types a response. The system passes
 * the typed text via [RemoteInput.getResultsFromIntent].
 */
@AndroidEntryPoint
class NotificationReplyReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var notificationCoordinator: MessageNotificationCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L).takeIf { it >= 0 } ?: return
        val recipient = intent.getStringExtra(EXTRA_RECIPIENT) ?: return
        val body = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(KEY_REPLY_TEXT)?.toString()?.trim()
            .orEmpty()
        if (body.isEmpty()) {
            Log.w(TAG, "Reply received with empty body, ignoring")
            return
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = messageRepository.send(
                    SendRequest(
                        threadId = threadId,
                        recipients = listOf(recipient),
                        body = body,
                        subscriptionId = -1,
                    ),
                )
                if (result is SendResult.Queued) {
                    // The notification stays — the system will refresh it via the next inbound,
                    // or the mark-read action collapses it. For now, just cancel it since we
                    // just satisfied the user's reply intent.
                    notificationCoordinator.cancelThread(threadId)
                } else if (result is SendResult.Failed) {
                    Log.e(TAG, "Notification reply failed: ${result.reason}")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Notification reply handler crashed", t)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val KEY_REPLY_TEXT = "key_reply_text"
        const val EXTRA_THREAD_ID = "extra_thread_id"
        const val EXTRA_RECIPIENT = "extra_recipient"
        private const val TAG = "NotifReplyReceiver"
    }
}
