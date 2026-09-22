package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart

/** The union authority; watching it with descendants covers sms, mms and threads in one registration. */
private val MMS_SMS_URI: Uri = Uri.parse("content://mms-sms/")

class TelephonyChangeObserver(private val context: Context) {

    /**
     * Emits once per change, after a short debounce.
     *
     * A single inbound message triggers several notifyChange calls (insert into sms, update
     * threads, update canonical_addresses); without debouncing the list re-queries repeatedly and
     * visibly flickers.
     *
     * This is only a freshness signal for a visible UI — it dies with the process. Correctness when
     * the app is not running comes from SmsDeliverReceiver.
     */
    fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(MMS_SMS_URI, true, observer)
        context.contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI, true, observer,
        )
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }
        .onStart { emit(Unit) }
        .debounce(200)
        .conflate()
}
