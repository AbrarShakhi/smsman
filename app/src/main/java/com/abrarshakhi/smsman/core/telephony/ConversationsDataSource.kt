package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.util.Log

private const val TAG = "ConversationsDataSource"

/**
 * `?simple=true` makes MmsSmsProvider return the `threads` table directly (one row per thread with
 * snippet, date and message_count) instead of the far more expensive SMS-MMS union.
 *
 * It is **undocumented** — it appears nowhere in the Android SDK sources and is a MmsSmsProvider
 * implementation detail that mainstream SMS clients depend on. Verified working on this device, but
 * every use is guarded and falls back.
 */
private val CONVERSATIONS_URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")

/** No public constant exists for this; `CanonicalAddressesColumns` ships without a CONTENT_URI. */
private val CANONICAL_ADDRESSES_URI: Uri = Uri.parse("content://mms-sms/canonical-addresses")

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

    /**
     * Threads newest first. [limit] is appended to the sort order because MmsSmsProvider is a
     * legacy provider that overrides the 5-arg query(), so ContentResolver's QUERY_ARG_SQL_LIMIT is
     * never forwarded to it.
     */
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
                // Empty stub threads exist on real devices (6 of 81 here) and would render blank.
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
                                    .orEmpty()
                                    .split(' ')
                                    .mapNotNull { it.trim().toLongOrNull() },
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

    /**
     * `recipient_ids` are ids into this table, not addresses. It is small (182 rows here against
     * 673 messages), so it is read whole rather than queried per row.
     */
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

    /**
     * Unread counts per thread. `ThreadsColumns` has only a 0/1 `read` flag, so the count has to
     * come from the message table.
     *
     * Deliberately grouped in Kotlin rather than injecting `read=0) GROUP BY (thread_id` into the
     * selection: that injection is what other clients do, but it is a SQL-shaped hack against a
     * provider that may parameterise properly, and the unread set is tiny anyway.
     */
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
                // Orphan SMS rows with a null thread_id exist on this device; skip them.
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
