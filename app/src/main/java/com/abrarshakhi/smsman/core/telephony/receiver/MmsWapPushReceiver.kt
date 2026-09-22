package com.abrarshakhi.smsman.core.telephony.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Required for SMS-role eligibility, but intentionally inert in v1: decoding an MMS needs the WAP
 * PDU codec, which is out of scope ("show, don't send").
 *
 * Consequence: while this app is the default SMS app, incoming MMS is not persisted. Checked
 * against the device before enabling this — `content://mms` holds 0 rows, so nothing is at risk in
 * practice, but this must be implemented before the app is used anywhere MMS matters.
 */
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.w("MmsWapPushReceiver", "WAP push received but MMS decoding is not implemented: ${intent.action}")
    }
}
