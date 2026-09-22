package com.abrarshakhi.smsman.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val INCOMING_MESSAGES = "incoming_messages"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(INCOMING_MESSAGES) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                INCOMING_MESSAGES,
                "Incoming messages",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "New SMS messages"
                enableVibration(true)
                setShowBadge(true)
            },
        )
    }
}
