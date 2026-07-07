package com.abrarshakhi.smsman.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import com.abrarshakhi.smsman.framework.notification.MessageNotificationCoordinator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationMarkReadReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var notificationCoordinator: MessageNotificationCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L).takeIf { it >= 0 } ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                messageRepository.markThreadRead(threadId)
                notificationCoordinator.cancelThread(threadId)
            } catch (t: Throwable) {
                Log.e(TAG, "Mark-read handler crashed", t)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_THREAD_ID = "extra_thread_id"
        private const val TAG = "NotifMarkReadReceiver"
    }
}
