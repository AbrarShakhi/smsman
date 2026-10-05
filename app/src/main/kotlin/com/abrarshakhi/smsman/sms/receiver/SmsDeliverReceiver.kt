package com.abrarshakhi.smsman.sms.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.notification.MessageNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "SmsDeliverReceiver"

private const val EXTRA_SUBSCRIPTION = "subscription"

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class SmsDeliverReceiver : BroadcastReceiver(), KoinComponent {

    private val notifier: MessageNotifier by inject()
    private val contacts: ContactsDataSource by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isNullOrEmpty()) return

        val subId = intent.getIntExtra(
            EXTRA_SUBSCRIPTION,
            intent.getIntExtra(
                SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID,
            ),
        )

        val pending = goAsync()
        val appContext = context.applicationContext
        receiverScope.launch {
            try {
                persist(appContext, parts, subId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist inbound SMS", e)
            } finally {
                pending.finish()
            }
        }
    }

    private fun persist(context: Context, parts: Array<SmsMessage>, subId: Int) {
        val head = parts.first()
        val address = head.displayOriginatingAddress ?: head.originatingAddress
        val body = parts.joinToString(separator = "") { it.displayMessageBody.orEmpty() }

        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.DATE_SENT, head.timestampMillis)
            put(Telephony.Sms.READ, 0)
            put(Telephony.Sms.SEEN, 0)
            put(Telephony.Sms.PROTOCOL, head.protocolIdentifier)
            put(Telephony.Sms.REPLY_PATH_PRESENT, if (head.isReplyPathPresent) 1 else 0)
            put(Telephony.Sms.SERVICE_CENTER, head.serviceCenterAddress)
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                put(Telephony.Sms.SUBSCRIPTION_ID, subId)
            }
            if (!address.isNullOrBlank()) {
                put(
                    Telephony.Sms.THREAD_ID,
                    Telephony.Threads.getOrCreateThreadId(context, address)
                )
            }
        }

        val uri = context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
        Log.i(TAG, "Persisted inbound SMS (${parts.size} part(s), subId=$subId) as $uri")

        if (!address.isNullOrBlank()) {
            val threadId = Telephony.Threads.getOrCreateThreadId(context, address)
            notifier.notifyIncoming(
                threadId = threadId,
                senderLabel = contacts.lookup(address)?.displayName ?: address,
                address = address,
                body = body,
            )
        }
    }
}
