package com.abrarshakhi.smsman.core.telephony.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class RespondViaMessageService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("RespondViaMessage", "Respond-via-message requested: ${intent?.action}")
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
