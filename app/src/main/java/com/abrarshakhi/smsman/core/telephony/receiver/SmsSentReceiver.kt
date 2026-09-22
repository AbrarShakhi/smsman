package com.abrarshakhi.smsman.core.telephony.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.abrarshakhi.smsman.core.telephony.EXTRA_MESSAGE_ID
import com.abrarshakhi.smsman.core.telephony.EXTRA_PART_INDEX

private const val TAG = "SmsSentReceiver"

/**
 * Moves an OUTBOX row to SENT or FAILED. Fires once per message part, so the first failure wins and
 * later successes must not overwrite it.
 */
class SmsSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        if (messageId < 0) return
        val partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0)

        val succeeded = resultCode == Activity.RESULT_OK
        Log.i(TAG, "Part $partIndex of message $messageId -> ${if (succeeded) "OK" else errorName(resultCode)}")

        val uri = ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, messageId)
        val pending = goAsync()
        try {
            if (succeeded) {
                // Only promote to SENT if nothing has already marked it failed.
                val current = currentType(context, uri)
                if (current != Telephony.Sms.MESSAGE_TYPE_FAILED) {
                    update(context, uri, Telephony.Sms.MESSAGE_TYPE_SENT, null)
                }
            } else {
                update(context, uri, Telephony.Sms.MESSAGE_TYPE_FAILED, resultCode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not update message $messageId", e)
        } finally {
            pending.finish()
        }
    }

    private fun currentType(context: Context, uri: android.net.Uri): Int? =
        context.contentResolver.query(uri, arrayOf(Telephony.Sms.TYPE), null, null, null)
            ?.use { if (it.moveToFirst()) it.getInt(0) else null }

    private fun update(context: Context, uri: android.net.Uri, type: Int, errorCode: Int?) {
        val values = ContentValues().apply {
            put(Telephony.Sms.TYPE, type)
            errorCode?.let { put(Telephony.Sms.ERROR_CODE, it) }
        }
        context.contentResolver.update(uri, values, null, null)
    }

    /**
     * Only the nine codes that exist on every supported platform are named. compileSdk 37 adds
     * around thirty more that API 31 cannot produce, so this must never be an exhaustive `when`.
     */
    private fun errorName(code: Int): String = when (code) {
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "GENERIC_FAILURE"
        SmsManager.RESULT_ERROR_RADIO_OFF -> "RADIO_OFF"
        SmsManager.RESULT_ERROR_NULL_PDU -> "NULL_PDU"
        SmsManager.RESULT_ERROR_NO_SERVICE -> "NO_SERVICE"
        SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> "LIMIT_EXCEEDED"
        SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE -> "FDN_CHECK_FAILURE"
        SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED -> "SHORT_CODE_NOT_ALLOWED"
        SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED -> "SHORT_CODE_NEVER_ALLOWED"
        else -> "ERROR_$code"
    }
}
