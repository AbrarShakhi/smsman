package com.abrarshakhi.smsman.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

/**
 * Receives inbound MMS WAP push notifications once we are the default SMS app.
 * Full handling (decode NotificationInd PDU, fetch from MMSC, write attachments) lands in M4.
 */
@AndroidEntryPoint
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_WAP_PUSH_DELIVER) return
        val mime = intent.type
        val pduBytes = intent.getByteArrayExtra("data")
        Log.i(TAG, "WAP_PUSH_DELIVER mime=$mime pdu=${pduBytes?.size ?: 0}B")
    }

    companion object {
        private const val TAG = "MmsWapPushReceiver"
        private const val ACTION_WAP_PUSH_DELIVER = "android.provider.Telephony.WAP_PUSH_DELIVER"
    }
}
