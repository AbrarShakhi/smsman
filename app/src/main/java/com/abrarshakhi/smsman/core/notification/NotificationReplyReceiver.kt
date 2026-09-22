package com.abrarshakhi.smsman.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.RemoteInput
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.SmsSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "NotificationReply"

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** Handles the inline reply from a notification without ever starting an Activity. */
class NotificationReplyReceiver : BroadcastReceiver(), KoinComponent {

    private val sender: SmsSender by inject()
    private val messages: MessagesDataSource by inject()
    private val notifier: MessageNotifier by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L)
        val address = intent.getStringExtra(EXTRA_ADDRESS)
        val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_REPLY_TEXT)

        if (threadId < 0 || address.isNullOrBlank() || reply.isNullOrBlank()) {
            Log.w(TAG, "Ignoring reply with missing thread, address or text")
            return
        }

        val pending = goAsync()
        scope.launch {
            try {
                sender.send(address, reply.toString(), SubscriptionManager.INVALID_SUBSCRIPTION_ID)
                    .onFailure { Log.e(TAG, "Reply failed", it) }
                // Replying implies the thread has been dealt with.
                messages.markThreadRead(threadId)
                notifier.cancel(threadId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ADDRESS = "com.abrarshakhi.smsman.REPLY_ADDRESS"
    }
}
