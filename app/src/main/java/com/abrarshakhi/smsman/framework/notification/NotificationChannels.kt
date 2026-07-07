package com.abrarshakhi.smsman.framework.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationChannels {
    const val GROUP_MESSAGES = "group_messages"

    const val MESSAGES_DEFAULT = "messages_default"
    const val MESSAGES_BLOCKED = "messages_blocked"
    const val MMS_DOWNLOAD = "mms_download"
    const val SEND_FAILURES = "send_failures"
    const val SCHEDULED = "scheduled"

    fun ensureCreated(context: Context) {
        val mgr = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return

        mgr.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_MESSAGES, "Messages"),
        )

        val channels = listOf(
            NotificationChannel(MESSAGES_DEFAULT, "New messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Incoming SMS and MMS from your contacts."
                group = GROUP_MESSAGES
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            },
            NotificationChannel(MESSAGES_BLOCKED, "Blocked senders", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Messages from numbers you've blocked. Silent."
                group = GROUP_MESSAGES
                setShowBadge(false)
            },
            NotificationChannel(MMS_DOWNLOAD, "MMS downloads", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Progress while downloading multimedia messages."
                setShowBadge(false)
            },
            NotificationChannel(SEND_FAILURES, "Send failures", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Outgoing messages that couldn't be delivered."
                setShowBadge(false)
            },
            NotificationChannel(SCHEDULED, "Scheduled messages", NotificationManager.IMPORTANCE_LOW).apply {
                description = "About to send a scheduled message."
                setShowBadge(false)
            },
        )

        mgr.createNotificationChannels(channels)
    }
}
