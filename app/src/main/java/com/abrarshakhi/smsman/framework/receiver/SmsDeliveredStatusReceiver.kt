package com.abrarshakhi.smsman.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.framework.sender.SmsSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when the carrier confirms delivery (only when the user opted into delivery reports
 * AND the network supports it). The PDU is in extras["pdu"]; we just need the status byte.
 */
@AndroidEntryPoint
class SmsDeliveredStatusReceiver : BroadcastReceiver() {

    @Inject lateinit var messageDao: MessageDao

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(SmsSender.EXTRA_MESSAGE_ID, -1L)
        if (messageId < 0) return

        val pdu = intent.getByteArrayExtra("pdu")
        val format = intent.getStringExtra("format")
        val statusByte = if (pdu != null) {
            runCatching { SmsMessage.createFromPdu(pdu, format).status }.getOrDefault(-1)
        } else -1

        val telephonyStatus = when {
            statusByte in 0..31 -> Telephony.Sms.STATUS_COMPLETE
            statusByte >= 64 -> Telephony.Sms.STATUS_FAILED
            else -> Telephony.Sms.STATUS_PENDING
        }
        Log.i(TAG, "Delivery receipt: id=$messageId statusByte=$statusByte → telephony=$telephonyStatus")

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                messageDao.updateStatus(messageId, telephonyStatus, 0)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to update delivery status for $messageId", t)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SmsDeliveredReceiver"
    }
}
