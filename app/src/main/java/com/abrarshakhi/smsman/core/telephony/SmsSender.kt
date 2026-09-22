package com.abrarshakhi.smsman.core.telephony

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.core.telephony.receiver.SmsDeliveredReceiver
import com.abrarshakhi.smsman.core.telephony.receiver.SmsSentReceiver

private const val TAG = "SmsSender"

const val EXTRA_MESSAGE_ID = "com.abrarshakhi.smsman.MESSAGE_ID"
const val EXTRA_PART_INDEX = "com.abrarshakhi.smsman.PART_INDEX"
const val EXTRA_PART_COUNT = "com.abrarshakhi.smsman.PART_COUNT"

class SmsSender(private val context: Context) {

    /**
     * Writes the outgoing message to the provider, then hands it to the radio.
     *
     * As the default SMS app we own provider writes, so the row is inserted as OUTBOX up front
     * (the UI can show "Sending") and moved to SENT or FAILED by [SmsSentReceiver].
     */
    fun send(address: String, body: String, subscriptionId: Int): Result<Long> = runCatching {
        require(address.isNotBlank()) { "No recipient" }
        require(body.isNotEmpty()) { "Empty message" }

        val threadId = Telephony.Threads.getOrCreateThreadId(context, address)
        val messageId = insertOutbox(address, body, subscriptionId, threadId)

        val smsManager = smsManagerFor(subscriptionId)
        val parts = smsManager.divideMessage(body)

        val sentIntents = ArrayList<PendingIntent>(parts.size)
        val deliveredIntents = ArrayList<PendingIntent>(parts.size)
        parts.indices.forEach { index ->
            sentIntents += statusIntent(SmsSentReceiver::class.java, messageId, index, parts.size)
            deliveredIntents += statusIntent(
                SmsDeliveredReceiver::class.java, messageId, index, parts.size,
            )
        }

        if (parts.size == 1) {
            smsManager.sendTextMessage(
                address, null, body, sentIntents.first(), deliveredIntents.first(),
            )
        } else {
            // The 5-arg overload takes ArrayList, not List - a listOf(...) will not compile here.
            smsManager.sendMultipartTextMessage(
                address, null, parts, sentIntents, deliveredIntents,
            )
        }
        Log.i(TAG, "Sent message $messageId in ${parts.size} part(s) on subId=$subscriptionId")
        messageId
    }.onFailure { Log.e(TAG, "Send failed", it) }

    private fun smsManagerFor(subscriptionId: Int): SmsManager {
        val manager = context.getSystemService(SmsManager::class.java)
        // getDefault() is deprecated and documented as unpredictable on multi-SIM devices, so an
        // explicit subscription is always preferred when we have one.
        return if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            manager
        } else {
            manager.createForSubscriptionId(subscriptionId)
        }
    }

    private fun insertOutbox(
        address: String,
        body: String,
        subscriptionId: Int,
        threadId: Long,
    ): Long {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.THREAD_ID, threadId)
            if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
            }
        }
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            ?: error("Provider refused the outgoing message")
        return uri.lastPathSegment?.toLongOrNull() ?: error("No id for inserted message")
    }

    /**
     * FLAG_IMMUTABLE is mandatory under targetSdk 31+ (construction throws otherwise), and the
     * intent is explicit so it stays clear of the API 34 implicit-PendingIntent restrictions.
     *
     * The request code must be unique per part: PendingIntent equality ignores extras, so reusing
     * one request code across parts makes every part report the last part's result.
     */
    private fun statusIntent(
        receiver: Class<*>,
        messageId: Long,
        partIndex: Int,
        partCount: Int,
    ): PendingIntent {
        val intent = Intent(context, receiver).apply {
            // Distinct data keeps the intents distinguishable even to filterEquals.
            data = Uri.parse("smsman://message/$messageId/part/$partIndex")
            putExtra(EXTRA_MESSAGE_ID, messageId)
            putExtra(EXTRA_PART_INDEX, partIndex)
            putExtra(EXTRA_PART_COUNT, partCount)
        }
        val requestCode = (messageId * 31 + partIndex).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
