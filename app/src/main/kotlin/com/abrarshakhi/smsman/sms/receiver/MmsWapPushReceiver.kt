package com.abrarshakhi.smsman.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.w(
            "MmsWapPushReceiver",
            "WAP push received but MMS decoding is not implemented: ${intent.action}"
        )
    }
}
