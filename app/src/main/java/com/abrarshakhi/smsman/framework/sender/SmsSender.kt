package com.abrarshakhi.smsman.framework.sender

import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.framework.receiver.SmsDeliveredStatusReceiver
import com.abrarshakhi.smsman.framework.receiver.SmsSentStatusReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [SmsManager] for outbound SMS:
 *
 *   1. Splits the body into parts via [SmsManager.divideMessage].
 *   2. Inserts an OUTBOX row into the Telephony provider and our local Room (the row id is used
 *      as the PendingIntent request code so callbacks can identify the message).
 *   3. Hands off to SmsManager with one sent PendingIntent and one delivery PendingIntent
 *      duplicated per part — multipart status accuracy is a known limitation in v1; the latest
 *      received result wins.
 *
 * The actual row writes are owned by the repository — this class is pure framework integration.
 */
@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Insert a Telephony OUTBOX row for the outgoing message. Returns the new system _id.
     */
    fun insertOutbox(
        address: String,
        body: String,
        timestamp: Long,
        threadId: Long,
        subscriptionId: Int,
    ): Long {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestamp)
            put(Telephony.Sms.DATE_SENT, 0)
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.THREAD_ID, threadId)
            put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
        }
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            ?: error("Telephony OUTBOX insert returned null")
        return ContentUris.parseId(uri)
    }

    /**
     * Update the Telephony row to a terminal type (sent / failed).
     */
    fun updateRowType(messageId: Long, type: Int) {
        val values = ContentValues().apply { put(Telephony.Sms.TYPE, type) }
        context.contentResolver.update(
            Telephony.Sms.CONTENT_URI,
            values,
            "${Telephony.Sms._ID} = ?",
            arrayOf(messageId.toString()),
        )
    }

    /**
     * Send [body] to [address] via the SmsManager bound to [subscriptionId]. Returns the part count.
     * The repository must have already created the OUTBOX row whose [messageId] is supplied so
     * receivers can resolve the originating message. When [requestDeliveryReport] is false the
     * delivery PendingIntent is omitted so the carrier doesn't ask for a report.
     */
    fun send(
        address: String,
        body: String,
        subscriptionId: Int,
        messageId: Long,
        requestDeliveryReport: Boolean,
    ): Int {
        val smsManager = smsManagerFor(subscriptionId)
        val parts = smsManager.divideMessage(body) ?: arrayListOf(body)

        val sentPi = createPendingIntent(SmsSentStatusReceiver::class.java, messageId, ACTION_SENT)
        val deliveredPi = if (requestDeliveryReport) {
            createPendingIntent(SmsDeliveredStatusReceiver::class.java, messageId, ACTION_DELIVERED)
        } else null

        if (parts.size == 1) {
            smsManager.sendTextMessage(address, null, body, sentPi, deliveredPi)
        } else {
            val sentList = ArrayList<PendingIntent>(parts.size).apply { repeat(parts.size) { add(sentPi) } }
            val deliveredList = deliveredPi?.let { pi ->
                // `repeat` provides an Int loop index as `it`, so name the captured intent
                // explicitly to avoid shadowing.
                ArrayList<PendingIntent>(parts.size).apply { repeat(parts.size) { add(pi) } }
            }
            smsManager.sendMultipartTextMessage(address, null, parts, sentList, deliveredList)
        }
        Log.i(TAG, "Dispatched messageId=$messageId parts=${parts.size} subId=$subscriptionId delivery=$requestDeliveryReport")
        return parts.size
    }

    @Suppress("DEPRECATION")
    private fun smsManagerFor(subscriptionId: Int): SmsManager {
        val service = context.getSystemService(SmsManager::class.java)
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID || subscriptionId < 0) {
            return service
        }
        // createForSubscriptionId is API 31+; on API 30 fall back to the static accessor which
        // has been available since API 22.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            service.createForSubscriptionId(subscriptionId)
        } else {
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        }
    }

    private fun createPendingIntent(
        target: Class<*>,
        messageId: Long,
        action: String,
    ): PendingIntent {
        val intent = Intent(context, target).apply {
            setAction(action)
            // Unique data URI per message so PendingIntent matching doesn't collapse distinct messages
            // (Intent equality ignores extras but considers action + data + component).
            data = Uri.parse("smsman://msg/$messageId")
            putExtra(EXTRA_MESSAGE_ID, messageId)
        }
        return PendingIntent.getBroadcast(
            context,
            messageId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_SENT = "com.abrarshakhi.smsman.SMS_SENT"
        const val ACTION_DELIVERED = "com.abrarshakhi.smsman.SMS_DELIVERED"
        const val EXTRA_MESSAGE_ID = "message_id"
        private const val TAG = "SmsSender"
    }
}
