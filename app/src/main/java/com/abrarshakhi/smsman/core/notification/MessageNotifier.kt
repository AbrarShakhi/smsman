package com.abrarshakhi.smsman.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.MainActivity

const val EXTRA_THREAD_ID = "com.abrarshakhi.smsman.THREAD_ID"
const val KEY_REPLY_TEXT = "com.abrarshakhi.smsman.REPLY_TEXT"

class MessageNotifier(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    fun notifyIncoming(threadId: Long, senderLabel: String, address: String, body: String) {
        NotificationChannels.ensureCreated(context)

        val person = Person.Builder().setName(senderLabel).setKey(address).build()
        val style = NotificationCompat.MessagingStyle(
            Person.Builder().setName("You").build(),
        ).addMessage(body, System.currentTimeMillis(), person)

        val notification = NotificationCompat.Builder(context, NotificationChannels.INCOMING_MESSAGES)
            .setSmallIcon(R.drawable.ic_tab_messages)
            .setStyle(style)
            .setContentTitle(senderLabel)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openThreadIntent(threadId))
            .addAction(replyAction(threadId, address))
            .addAction(markReadAction(threadId))
            .build()

        manager?.notify(threadId.toInt(), notification)
    }

    fun cancel(threadId: Long) = manager?.cancel(threadId.toInt())

    /**
     * Targets the Activity directly. Notification trampolines — a tap hitting a receiver or service
     * that then starts an Activity — are blocked from API 31, which is exactly the pattern SMS
     * clients reach for.
     */
    private fun openThreadIntent(threadId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("smsman://thread/$threadId")
            putExtra(EXTRA_THREAD_ID, threadId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            threadId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * The one place FLAG_MUTABLE is correct: RemoteInput works by having the system write the typed
     * reply into this intent, which an immutable PendingIntent forbids.
     */
    private fun replyAction(threadId: Long, address: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationReplyReceiver::class.java).apply {
            data = Uri.parse("smsman://reply/$threadId")
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(NotificationReplyReceiver.EXTRA_ADDRESS, address)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            threadId.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Action.Builder(R.drawable.ic_tab_messages, "Reply", pending)
            .addRemoteInput(RemoteInput.Builder(KEY_REPLY_TEXT).setLabel("Reply").build())
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun markReadAction(threadId: Long): NotificationCompat.Action {
        val intent = Intent(context, MarkReadReceiver::class.java).apply {
            data = Uri.parse("smsman://markread/$threadId")
            putExtra(EXTRA_THREAD_ID, threadId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            threadId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Action.Builder(R.drawable.ic_tab_messages, "Mark as read", pending)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()
    }
}
