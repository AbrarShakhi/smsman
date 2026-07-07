package com.abrarshakhi.smsman.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.data.telephony.SmsInboundHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsDeliverReceiver : BroadcastReceiver() {

    @Inject lateinit var inboundHandler: SmsInboundHandler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (parts.isEmpty()) return

        val subId = intent.getIntExtra(
            SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
            SubscriptionManager.INVALID_SUBSCRIPTION_ID,
        )

        // The receiver dies after onReceive returns, so we extend its lifetime up to 10s for the
        // DB writes via goAsync(). For a 1-part SMS this completes in single-digit ms; multipart
        // can take longer if many parts coalesce.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = inboundHandler.handle(parts, subId)
                Log.i(TAG, "Inbound SMS: $result")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to handle inbound SMS", t)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SmsDeliverReceiver"
    }
}
