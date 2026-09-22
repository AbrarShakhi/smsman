package com.abrarshakhi.smsman.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class MarkReadReceiver : BroadcastReceiver(), KoinComponent {

    private val messages: MessagesDataSource by inject()
    private val notifier: MessageNotifier by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L)
        if (threadId < 0) return

        val pending = goAsync()
        scope.launch {
            try {
                val updated = messages.markThreadRead(threadId)
                notifier.cancel(threadId)
                Log.i("MarkReadReceiver", "Marked $updated message(s) read in thread $threadId")
            } finally {
                pending.finish()
            }
        }
    }
}
