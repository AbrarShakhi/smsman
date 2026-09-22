package com.abrarshakhi.smsman.core.telephony.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Fourth component required for SMS-role eligibility: lets the dialer offer "reply with message"
 * when declining a call. Registered now so the role can be held; wiring the quick-reply flow comes
 * with the send path.
 */
class RespondViaMessageService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("RespondViaMessage", "Respond-via-message requested: ${intent?.action}")
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
