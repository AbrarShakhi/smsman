package com.abrarshakhi.smsman.data.telephony

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.abrarshakhi.smsman.framework.SmsRoleProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Orchestrates the Telephony ↔ Room sync:
 *
 *   • On [start], if we are the default SMS app, perform an initial bulk import.
 *   • Register a ContentObserver on the MmsSms root URI so changes outside our app
 *     (system marks read, another tool deletes, etc.) re-trigger an import.
 *   • Debounce observer fires so a burst of changes coalesces into a single re-import.
 *
 * Inbound SMS delivered to our [SmsInboundHandler] writes directly to Room without going
 * through here — the observer is the safety-net path.
 */
@OptIn(FlowPreview::class)
@Singleton
class TelephonySyncCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importer: TelephonyImporter,
    private val smsRoleProvider: SmsRoleProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val changeSignal = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            changeSignal.tryEmit(Unit)
        }
    }

    @Volatile private var started = false

    fun start() {
        if (started) return
        started = true

        context.contentResolver.registerContentObserver(MMS_SMS_URI, true, observer)

        scope.launch {
            changeSignal
                .debounce(DEBOUNCE.milliseconds)
                .onEach { syncIfDefault() }
                .collect()
        }

        // Kick off the initial import on start. Safe to call even if we're not yet default
        // (it short-circuits inside).
        scope.launch { syncIfDefault() }
    }

    fun requestRefresh() {
        changeSignal.tryEmit(Unit)
    }

    private suspend fun syncIfDefault() {
        if (!smsRoleProvider.isDefaultSmsApp()) return
        try {
            val result = importer.importRecent()
            Log.i(TAG, "Imported ${result.messageCount} messages across ${result.threadCount} threads")
        } catch (t: Throwable) {
            // Never crash the app process from background sync.
            Log.e(TAG, "Telephony import failed", t)
        }
    }

    private companion object {
        const val TAG = "TelephonySync"
        const val DEBOUNCE = 400L
        val MMS_SMS_URI: Uri = Telephony.MmsSms.CONTENT_URI
    }
}

// Helper to make the .onEach chain terminate; kotlinx.coroutines.flow.collect needs to be imported.
private suspend inline fun <T> kotlinx.coroutines.flow.Flow<T>.collect() = collect {}
