package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.util.Log
import androidx.core.net.toUri

private const val TAG = "ConversationsDataSource"

private val CONVERSATIONS_URI: Uri = "content://mms-sms/conversations?simple=true".toUri()

private val CANONICAL_ADDRESSES_URI: Uri = "content://mms-sms/canonical-addresses".toUri()

internal data class ProviderThread(
    val threadId: Long,
    val date: Long,
    val messageCount: Int,
    val snippet: String,
    val recipientIds: List<Long>,
    val hasAttachment: Boolean,
)

class ConversationsDataSource(private val context: Context) {

    private val resolver get() = context.contentResolver

    internal fun loadThreads(limit: Int = 200): List<ProviderThread> {
        val projection = arrayOf(
            BaseColumns._ID,
            Telephony.Threads.DATE,
            Telephony.Threads.MESSAGE_COUNT,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.RECIPIENT_IDS,
            Telephony.Threads.HAS_ATTACHMENT,
        )
        return try {
            resolver.query(
                CONVERSATIONS_URI,
                projection,
                "${Telephony.Threads.MESSAGE_COUNT} > 0",
                null,
                "${Telephony.Threads.DATE} DESC LIMIT $limit",
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            ProviderThread(
                                threadId = cursor.longOr(BaseColumns._ID),
                                date = cursor.longOr(Telephony.Threads.DATE),
                                messageCount = cursor.intOr(Telephony.Threads.MESSAGE_COUNT),
                                snippet = cursor.stringOrNull(Telephony.Threads.SNIPPET).orEmpty(),
                                recipientIds = cursor.stringOrNull(Telephony.Threads.RECIPIENT_IDS)
                                    .orEmpty().split(' ').mapNotNull { it.trim().toLongOrNull() },
                                hasAttachment = cursor.intOr(Telephony.Threads.HAS_ATTACHMENT) != 0,
                            ),
                        )
                    }
                }
            }.orEmpty()
        } catch (e: SQLiteException) {
            Log.e(TAG, "Thread query rejected by provider", e)
            emptyList()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Thread query rejected by provider", e)
            emptyList()
        }
    }

    internal fun loadCanonicalAddresses(): Map<Long, String> = try {
        resolver.query(
            CANONICAL_ADDRESSES_URI,
            arrayOf(BaseColumns._ID, Telephony.CanonicalAddressesColumns.ADDRESS),
            null,
            null,
            null,
        )?.use { cursor ->
            buildMap {
                while (cursor.moveToNext()) {
                    val id = cursor.longOr(BaseColumns._ID, -1L)
                    val address = cursor.stringOrNull(Telephony.CanonicalAddressesColumns.ADDRESS)
                    if (id >= 0 && !address.isNullOrBlank()) put(id, address)
                }
            }
        }.orEmpty()
    } catch (e: Exception) {
        Log.e(TAG, "Canonical address query failed", e)
        emptyMap()
    }

    internal fun loadUnreadCounts(): Map<Long, Int> = try {
        resolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.THREAD_ID),
            "${Telephony.Sms.READ} = 0",
            null,
            null,
        )?.use { cursor ->
            val counts = mutableMapOf<Long, Int>()
            while (cursor.moveToNext()) {
                val threadId = cursor.longOr(Telephony.Sms.THREAD_ID, -1L)
                if (threadId >= 0) counts[threadId] = (counts[threadId] ?: 0) + 1
            }
            counts
        }.orEmpty()
    } catch (e: Exception) {
        Log.e(TAG, "Unread count query failed", e)
        emptyMap()
    }
}
