package com.abrarshakhi.smsman.data.provider

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import androidx.core.net.toUri
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlin.time.Duration.Companion.milliseconds

private val MMS_SMS_URI: Uri = "content://mms-sms/".toUri()

class TelephonyChangeObserver(private val context: Context) {

    @OptIn(FlowPreview::class)
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
    }.onStart { emit(Unit) }.debounce(200.milliseconds).conflate()
}
