package com.abrarshakhi.smsman.core.telephony

import android.content.ContentUris
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

    fun searchMessages(query: String, limit: Int = 200): List<Message> {
        if (query.isBlank()) return emptyList()
        val escaped = query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

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
                "${Telephony.Sms.BODY} LIKE ? ESCAPE '\\'",
                arrayOf("%$escaped%"),
                "${Telephony.Sms.DATE} DESC LIMIT $limit",
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
                            ),
                        )
                    }
                }
            }.orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Message search failed", e)
            emptyList()
        }
    }

    fun markThreadUnread(threadId: Long): Int = try {
        val latestId = resolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(BaseColumns._ID),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT 1",
        )?.use { if (it.moveToFirst()) it.getLong(0) else null }

        if (latestId == null) {
            0
        } else {
            resolver.update(
                ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, latestId),
                ContentValues().apply { put(Telephony.Sms.READ, 0) },
                null,
                null,
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Could not mark thread $threadId unread", e)
        0
    }

    fun deleteMessages(ids: Collection<Long>): Int = try {
        ids.sumOf { id ->
            resolver.delete(
                ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id), null, null,
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Message deletion failed", e)
        0
    }

    fun deleteThread(threadId: Long): Int = try {
        resolver.delete(
            Telephony.Sms.CONTENT_URI,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
        )
    } catch (e: Exception) {
        Log.e(TAG, "Thread deletion failed for $threadId", e)
        0
    }

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
