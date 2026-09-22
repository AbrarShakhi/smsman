package com.abrarshakhi.smsman.common.util

import android.content.Context
import android.text.format.DateUtils
import java.util.Calendar

/**
 * Messages-style stamps: time today, weekday this week, date this year, else numeric date.
 *
 * [context] is required: DateUtils reads the user's 12/24-hour preference from it and throws on a
 * null context.
 */
fun formatConversationTime(
    context: Context,
    timestamp: Long,
    now: Long = System.currentTimeMillis(),
): String {
    if (timestamp <= 0L) return ""

    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance().apply { timeInMillis = now }

    val sameDay = then.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        then.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

    val flags = when {
        sameDay -> DateUtils.FORMAT_SHOW_TIME
        now - timestamp < 7 * DateUtils.DAY_IN_MILLIS ->
            DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY
        then.get(Calendar.YEAR) == today.get(Calendar.YEAR) ->
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR
        else -> DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NUMERIC_DATE
    }
    return DateUtils.formatDateTime(context, timestamp, flags)
}

/**
 * Day dividers name a *day*, never a clock time — reusing [formatConversationTime] here rendered
 * today's divider as "08:11".
 */
fun formatDayDivider(
    context: Context,
    timestamp: Long,
    now: Long = System.currentTimeMillis(),
): String {
    if (timestamp <= 0L) return ""

    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, -1)
    }

    fun sameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    return when {
        sameDay(then, today) -> "Today"
        sameDay(then, yesterday) -> "Yesterday"
        now - timestamp < 7 * DateUtils.DAY_IN_MILLIS -> DateUtils.formatDateTime(
            context, timestamp, DateUtils.FORMAT_SHOW_WEEKDAY,
        )
        then.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> DateUtils.formatDateTime(
            context,
            timestamp,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR,
        )
        else -> DateUtils.formatDateTime(
            context, timestamp, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NUMERIC_DATE,
        )
    }
}
