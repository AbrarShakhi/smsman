package com.abrarshakhi.smsman.core.telephony.receiver

import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.abrarshakhi.smsman.core.telephony.EXTRA_MESSAGE_ID

private const val TAG = "SmsDeliveredReceiver"

/**
 * Delivery reports depend on carrier support and frequently never arrive, so nothing in the UI may
 * block on one — a message is usable at SENT and this only upgrades it to delivered.
 */
class SmsDeliveredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        if (messageId < 0) return

        val status = readStatus(intent)
        Log.i(TAG, "Delivery report for message $messageId -> status=$status")

        val pending = goAsync()
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.STATUS, status)
            }
            context.contentResolver.update(
                ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, messageId),
                values, null, null,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not update delivery status for $messageId", e)
        } finally {
            pending.finish()
        }
    }

    /** The status report arrives as a raw PDU in the "pdu" extra. */
    private fun readStatus(intent: Intent): Int = try {
        val pdu = intent.getByteArrayExtra("pdu")
        val format = intent.getStringExtra("format")
        if (pdu == null) {
            Telephony.Sms.STATUS_COMPLETE
        } else {
            SmsMessage.createFromPdu(pdu, format)?.status ?: Telephony.Sms.STATUS_COMPLETE
        }
    } catch (e: Exception) {
        Log.w(TAG, "Could not parse delivery PDU; assuming complete", e)
        Telephony.Sms.STATUS_COMPLETE
    }
}
