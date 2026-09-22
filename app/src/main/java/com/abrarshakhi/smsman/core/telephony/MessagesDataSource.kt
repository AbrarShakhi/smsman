package com.abrarshakhi.smsman.core.telephony

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.core.model.DeliveryStatus
import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.model.MessageType

private const val TAG = "MessagesDataSource"

class MessagesDataSource(private val context: Context) {

    private val resolver get() = context.contentResolver

    /**
     * Newest [limit] messages in a thread, returned oldest-first for display.
     *
     * The provider is queried DESC with the limit in the sort order (the only way to limit a legacy
     * provider) so the *newest* window is taken, then reversed.
     */
    fun loadMessages(threadId: Long, limit: Int = 500): List<Message> {
        val projection = arrayOf(
            BaseColumns._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBSCRIPTION_ID,
        )
        return try {
            resolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} DESC LIMIT $limit",
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            Message(
                                id = cursor.longOr(BaseColumns._ID),
                                threadId = cursor.longOr(Telephony.Sms.THREAD_ID, threadId),
                                address = cursor.stringOrNull(Telephony.Sms.ADDRESS),
                                body = cursor.stringOrNull(Telephony.Sms.BODY).orEmpty(),
                                date = cursor.longOr(Telephony.Sms.DATE),
                                dateSent = cursor.longOr(Telephony.Sms.DATE_SENT),
                                isRead = cursor.intOr(Telephony.Sms.READ) != 0,
                                type = MessageType.fromProvider(cursor.intOr(Telephony.Sms.TYPE)),
                                status = DeliveryStatus.fromProvider(
                                    cursor.intOr(Telephony.Sms.STATUS, -1),
                                ),
                                subscriptionId = cursor.intOr(
                                    Telephony.Sms.SUBSCRIPTION_ID,
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID,
                                ),
                            ),
                        )
                    }
                }.asReversed()
            }.orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Message query failed for thread $threadId", e)
            emptyList()
        }
    }

    /**
     * Resolves specific message ids, for the Pinned tab. Ids that no longer exist are simply
     * absent from the result — another SMS app can delete a message we hold a pin for — so callers
     * must diff against what they asked for and prune.
     */
    fun loadMessagesByIds(ids: List<Long>): List<Message> {
        if (ids.isEmpty()) return emptyList()
        val projection = arrayOf(
            BaseColumns._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBSCRIPTION_ID,
        )
        val placeholders = ids.joinToString(",") { "?" }
        return try {
            resolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${BaseColumns._ID} IN ($placeholders)",
                ids.map(Long::toString).toTypedArray(),
                null,
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            Message(
                                id = cursor.longOr(BaseColumns._ID),
                                threadId = cursor.longOr(Telephony.Sms.THREAD_ID),
                                address = cursor.stringOrNull(Telephony.Sms.ADDRESS),
                                body = cursor.stringOrNull(Telephony.Sms.BODY).orEmpty(),
                                date = cursor.longOr(Telephony.Sms.DATE),
                                dateSent = cursor.longOr(Telephony.Sms.DATE_SENT),
                                isRead = cursor.intOr(Telephony.Sms.READ) != 0,
                                type = MessageType.fromProvider(cursor.intOr(Telephony.Sms.TYPE)),
                                status = DeliveryStatus.fromProvider(
                                    cursor.intOr(Telephony.Sms.STATUS, -1),
                                ),
                                subscriptionId = cursor.intOr(
                                    Telephony.Sms.SUBSCRIPTION_ID,
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID,
                                ),
                                isPinned = true,
                            ),
                        )
                    }
                }
            }.orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Pinned message lookup failed", e)
            emptyList()
        }
    }

    /**
     * Clears unread state for a thread. Only the default SMS app may write here, which this app is.
     *
     * `read` drives the unread badge; `seen` suppresses the system's own unread treatment. Both are
     * set, and the update is scoped to rows that are actually unread so it is a no-op when nothing
     * changed and does not spuriously trigger the content observer.
     */
    fun markThreadRead(threadId: Long): Int = try {
        val values = ContentValues().apply {
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
        }
        resolver.update(
            Telephony.Sms.CONTENT_URI,
            values,
            "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
            arrayOf(threadId.toString()),
        )
    } catch (e: Exception) {
        Log.e(TAG, "Could not mark thread $threadId read", e)
        0
    }

    /** Distinct addresses seen in a thread; used to title the chat without a canonical-address hop. */
    fun threadAddresses(threadId: Long, limit: Int = 50): List<String> = try {
        resolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT $limit",
        )?.use { cursor ->
            buildSet {
                while (cursor.moveToNext()) {
                    cursor.stringOrNull(Telephony.Sms.ADDRESS)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { add(it) }
                }
            }.toList()
        }.orEmpty()
    } catch (e: Exception) {
        Log.e(TAG, "Address query failed for thread $threadId", e)
        emptyList()
    }
}
