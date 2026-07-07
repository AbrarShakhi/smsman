package com.abrarshakhi.smsman.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DATE_THIS_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")
private val DATE_OTHER_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yy")

/**
 * Conversation-list style relative time, locale-aware:
 *  - Today           → HH:mm
 *  - Yesterday       → "Yesterday"
 *  - Within 6 days   → short day name (Mon, Tue, …)
 *  - This year       → d MMM (e.g. "12 Mar")
 *  - Older           → d MMM yy
 */
fun formatRelativeListTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val zone = ZoneId.systemDefault()
    val msg = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val msgDate = msg.toLocalDate()
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(msgDate, today)
    val locale = Locale.getDefault()

    return when {
        days <= 0L -> msg.format(TIME_FORMATTER)
        days == 1L -> "Yesterday"
        days in 2..6 -> msgDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        msgDate.year == today.year -> msg.format(DATE_THIS_YEAR)
        else -> msg.format(DATE_OTHER_YEAR)
    }
}
