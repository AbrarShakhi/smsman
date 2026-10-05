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

        val replyInputs = actions[0].remoteInputs
        assertNotNull("Reply action has no RemoteInput", replyInputs)
        assertEquals(1, replyInputs.size)

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
