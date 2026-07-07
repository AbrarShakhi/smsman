package com.abrarshakhi.smsman.framework.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

/**
 * Required by the SMS-default-handler role so the OS can route ACTION_RESPOND_VIA_MESSAGE
 * (e.g. quick-reply from the dialer during an incoming call) to our app.
 *
 * We don't surface a UI for this in v1 — the OS shows its own quick-reply sheet —
 * but the service component must exist or the role request fails on some devices.
 */
@AndroidEntryPoint
class RespondViaMessageService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "RESPOND_VIA_MESSAGE received: ${intent?.dataString}")
        stopSelf(startId)
        return START_NOT_STICKY
    }

    companion object {
        private const val TAG = "RespondViaMsgService"
    }
}
