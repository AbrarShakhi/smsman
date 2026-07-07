package com.abrarshakhi.smsman.framework.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.abrarshakhi.smsman.MainActivity
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.domain.model.Contact
import com.abrarshakhi.smsman.framework.receiver.NotificationDeleteReceiver
import com.abrarshakhi.smsman.framework.receiver.NotificationMarkReadReceiver
import com.abrarshakhi.smsman.framework.receiver.NotificationReplyReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and posts MessagingStyle notifications for incoming SMS/MMS.
 *
 * One notification per thread. Three actions: Reply (RemoteInput direct-reply),
 * Mark as read, Delete. Tapping the notification deep-links to chat://thread/<id>.
 * A long-lived conversation shortcut is published so the OS can offer Bubbles +
 * surface the contact in the Sharesheet.
 */
@Singleton
class MessageNotificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureChannels() = NotificationChannels.ensureCreated(context)

    /**
     * Post a notification for an incoming message. [unreadMessages] is the in-order list of all
     * still-unread messages in the thread, oldest first; MessagingStyle re-renders the whole
     * conversation each time.
     */
    fun postIncoming(
        threadId: Long,
        sender: String,
        senderContact: Contact?,
        unreadMessages: List<UnreadMessage>,
    ) {
        if (unreadMessages.isEmpty()) return
        // Explicit checkSelfPermission is the canonical pattern that lint flow-analyzes through;
        // we re-validate at the call site below as well so the assistive @RequiresPermission
        // annotation on NotificationManagerCompat#notify is satisfied.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notifManager = NotificationManagerCompat.from(context)
        if (!notifManager.areNotificationsEnabled()) return

        val displayName = senderContact?.displayLabel ?: sender
        val shortcut = publishConversationShortcut(threadId, sender, senderContact)

        val senderPerson = buildPerson(sender, senderContact)
        val selfPerson = Person.Builder().setKey("me").setName("You").build()

        val style = NotificationCompat.MessagingStyle(selfPerson)
            .setConversationTitle(if (unreadMessages.size > 1) displayName else null)
            .setGroupConversation(false)
        unreadMessages.forEach { msg ->
            style.addMessage(
                NotificationCompat.MessagingStyle.Message(msg.body, msg.timestamp, senderPerson),
            )
        }

        val builder = NotificationCompat.Builder(context, NotificationChannels.MESSAGES_DEFAULT)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setShortcutId(shortcut.id)
            .setContentIntent(threadDeepLinkIntent(threadId))
            .setDeleteIntent(notificationDismissIntent(threadId))
            .addAction(buildReplyAction(threadId, sender))
            .addAction(buildMarkReadAction(threadId))
            .addAction(buildDeleteAction(threadId, unreadMessages.last().messageId))
            .setWhen(unreadMessages.last().timestamp)
            .setShowWhen(true)

        notifManager.notify(threadId.toNotificationId(), builder.build())
    }

    fun cancelThread(threadId: Long) {
        NotificationManagerCompat.from(context).cancel(threadId.toNotificationId())
    }

    private fun buildReplyAction(threadId: Long, recipient: String): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(NotificationReplyReceiver.KEY_REPLY_TEXT)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
            data = Uri.parse("smsman-reply://thread/$threadId")
            putExtra(NotificationReplyReceiver.EXTRA_THREAD_ID, threadId)
            putExtra(NotificationReplyReceiver.EXTRA_RECIPIENT, recipient)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            threadId.toRequestCode(REPLY),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        return NotificationCompat.Action.Builder(R.drawable.ic_notification, "Reply", pi)
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .build()
    }

    private fun buildMarkReadAction(threadId: Long): NotificationCompat.Action {
        val intent = Intent(context, NotificationMarkReadReceiver::class.java).apply {
            data = Uri.parse("smsman-markread://thread/$threadId")
            putExtra(NotificationMarkReadReceiver.EXTRA_THREAD_ID, threadId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            threadId.toRequestCode(MARK_READ),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(R.drawable.ic_notification, "Mark as read", pi)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()
    }

    private fun buildDeleteAction(threadId: Long, lastMessageId: Long): NotificationCompat.Action {
        val intent = Intent(context, NotificationDeleteReceiver::class.java).apply {
            data = Uri.parse("smsman-delete://thread/$threadId/msg/$lastMessageId")
            putExtra(NotificationDeleteReceiver.EXTRA_THREAD_ID, threadId)
            putExtra(NotificationDeleteReceiver.EXTRA_MESSAGE_ID, lastMessageId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            threadId.toRequestCode(DELETE),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(R.drawable.ic_notification, "Delete", pi)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
            .setShowsUserInterface(false)
            .build()
    }

    private fun threadDeepLinkIntent(threadId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("smsman://thread/$threadId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            threadId.toRequestCode(OPEN),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun notificationDismissIntent(threadId: Long): PendingIntent {
        // Re-use the mark-read receiver — when the user swipes the notification away we treat it
        // as "I've seen these" (mirroring Google Messages' behaviour).
        val intent = Intent(context, NotificationMarkReadReceiver::class.java).apply {
            data = Uri.parse("smsman-dismiss://thread/$threadId")
            putExtra(NotificationMarkReadReceiver.EXTRA_THREAD_ID, threadId)
        }
        return PendingIntent.getBroadcast(
            context,
            threadId.toRequestCode(DISMISS),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildPerson(address: String, contact: Contact?): Person {
        val builder = Person.Builder()
            .setKey(address)
            .setName(contact?.displayLabel ?: address)
        contact?.photoUri?.let {
            runCatching { IconCompat.createWithContentUri(it) }.getOrNull()?.let(builder::setIcon)
        }
        return builder.build()
    }

    private fun publishConversationShortcut(
        threadId: Long,
        address: String,
        contact: Contact?,
    ): ShortcutInfoCompat {
        val displayName = contact?.displayLabel ?: address
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("smsman://thread/$threadId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val shortcut = ShortcutInfoCompat.Builder(context, "thread_$threadId")
            .setShortLabel(displayName)
            .setLongLabel(displayName)
            .setLongLived(true)
            .setIntent(intent)
            .setCategories(setOf("android.shortcut.conversation"))
            .setPerson(buildPerson(address, contact))
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        return shortcut
    }

    private fun Long.toNotificationId(): Int = (this and 0x7FFFFFFF).toInt()

    // Unique-ish request codes so PendingIntent matching doesn't conflate distinct actions.
    private fun Long.toRequestCode(kind: Int): Int =
        ((this and 0x0FFFFFFF).toInt() shl 4) or kind

    data class UnreadMessage(val messageId: Long, val body: String, val timestamp: Long)

    private companion object {
        const val OPEN = 0
        const val REPLY = 1
        const val MARK_READ = 2
        const val DELETE = 3
        const val DISMISS = 4
    }
}
