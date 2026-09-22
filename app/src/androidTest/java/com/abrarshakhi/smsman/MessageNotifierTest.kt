package com.abrarshakhi.smsman

import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.abrarshakhi.smsman.core.notification.MessageNotifier
import com.abrarshakhi.smsman.core.notification.NotificationChannels
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Builds and posts a real notification. Worth doing on-device because the PendingIntent mutability
 * rules throw at construction under targetSdk 31+, so a wrong flag is a crash, not a warning.
 */
@RunWith(AndroidJUnit4::class)
class MessageNotifierTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val threadId = 987_654L

    @After
    fun tearDown() = manager.cancel(threadId.toInt())

    @Test
    fun channelIsCreated() {
        NotificationChannels.ensureCreated(context)
        assertNotNull(manager.getNotificationChannel(NotificationChannels.INCOMING_MESSAGES))
    }

    @Test
    fun incomingNotificationPostsWithReplyAndMarkRead() {
        MessageNotifier(context).notifyIncoming(
            threadId = threadId,
            senderLabel = "Test Sender",
            address = "+8801521778285",
            body = "hello from the test",
        )

        val posted = manager.activeNotifications.firstOrNull { it.id == threadId.toInt() }
        assertNotNull("Notification was not posted", posted)

        val actions = posted!!.notification.actions
        assertEquals("Expected reply and mark-as-read actions", 2, actions.size)

        // The reply action must carry a RemoteInput, which is what requires FLAG_MUTABLE.
        val replyInputs = actions[0].remoteInputs
        assertNotNull("Reply action has no RemoteInput", replyInputs)
        assertEquals(1, replyInputs.size)

        // Mark-as-read must NOT take a RemoteInput; it stays immutable.
        assertTrue(actions[1].remoteInputs.isNullOrEmpty())
    }

    @Test
    fun cancelRemovesTheNotification() {
        val notifier = MessageNotifier(context)
        notifier.notifyIncoming(threadId, "Test Sender", "+8801521778285", "body")
        assertTrue(manager.activeNotifications.any { it.id == threadId.toInt() })

        notifier.cancel(threadId)
        Thread.sleep(300)
        assertTrue(manager.activeNotifications.none { it.id == threadId.toInt() })
    }
}
