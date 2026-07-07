package com.abrarshakhi.smsman.framework.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.abrarshakhi.smsman.data.db.dao.MessageDao
import com.abrarshakhi.smsman.framework.sender.SmsSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when SmsManager reports the result of an outbound SMS part. resultCode is one of
 * Activity.RESULT_OK or SmsManager.RESULT_ERROR_*. Updates the originating message row's
 * type (MESSAGE_TYPE_SENT or MESSAGE_TYPE_FAILED) plus errorCode.
 */
@AndroidEntryPoint
class SmsSentStatusReceiver : BroadcastReceiver() {

    @Inject lateinit var messageDao: MessageDao
    @Inject lateinit var smsSender: SmsSender

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(SmsSender.EXTRA_MESSAGE_ID, -1L)
        if (messageId < 0) return

        val ok = resultCode == Activity.RESULT_OK
        val errorCode = if (ok) 0 else resultCode
        val finalType = if (ok) Telephony.Sms.MESSAGE_TYPE_SENT else Telephony.Sms.MESSAGE_TYPE_FAILED
        val statusCode = if (ok) Telephony.Sms.STATUS_COMPLETE else Telephony.Sms.STATUS_FAILED

        Log.i(TAG, "Sent receipt: id=$messageId ok=$ok errorCode=$errorCode")

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                smsSender.updateRowType(messageId, finalType)
                messageDao.updateStatus(messageId, statusCode, errorCode)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to update sent status for $messageId", t)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SmsSentStatusReceiver"
    }
}
