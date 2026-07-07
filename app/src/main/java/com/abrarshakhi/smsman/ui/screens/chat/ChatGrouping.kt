package com.abrarshakhi.smsman.ui.screens.chat

import com.abrarshakhi.smsman.domain.model.Message
import com.abrarshakhi.smsman.domain.model.MessageType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Transform a flat message list into a chat-ready item list:
 *   • Messages sorted newest-first (for LazyColumn reverseLayout = true).
 *   • Author-run flags (isFirstInRun / isLastInRun) for tail rendering.
 *   • Day dividers inserted at day boundaries (and at the start of the oldest day).
 *
 * The expected display when rendered with reverseLayout=true:
 *
 *     ┌────────────────────┐  ← top (oldest)
 *     │  --- Apr 28 ---    │
 *     │  alice : "hi"      │
 *     │  alice : "hello"   │
 *     │  --- Apr 29 ---    │
 *     │  me    : "hey"     │
 *     └────────────────────┘  ← bottom (newest)
 */
fun groupMessages(messages: List<Message>): List<ChatItem> {
    if (messages.isEmpty()) return emptyList()
    val sorted = messages.sortedByDescending { it.date }
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    val result = ArrayList<ChatItem>(sorted.size + 4)

    for (idx in sorted.indices) {
        val msg = sorted[idx]
        val msgDay = Instant.ofEpochMilli(msg.date).atZone(zone).toLocalDate()

        val newer = sorted.getOrNull(idx - 1)
        val older = sorted.getOrNull(idx + 1)
        val newerDay = newer?.let { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
        val olderDay = older?.let { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }

        val msgAuthor = authorKey(msg)
        val newerAuthor = newer?.let { authorKey(it) }
        val olderAuthor = older?.let { authorKey(it) }

        val isFirstInRun = olderAuthor != msgAuthor || olderDay != msgDay
        val isLastInRun = newerAuthor != msgAuthor || newerDay != msgDay

        result.add(
            ChatItem.Msg(
                message = msg,
                isFirstInRun = isFirstInRun,
                isLastInRun = isLastInRun,
                showTimeLabel = isLastInRun,
                isMe = msgAuthor == AUTHOR_ME,
            ),
        )

        // Day divider sits ABOVE the first message of each day. With reverseLayout=true, "above"
        // means a higher index in the list. So we insert the divider immediately after the last
        // message of a day, labeled with that day.
        if (olderDay == null || olderDay != msgDay) {
            result.add(
                ChatItem.DayDivider(
                    label = formatDayHeader(msgDay, today),
                    epochDay = msgDay.toEpochDay(),
                ),
            )
        }
    }

    return result
}

private const val AUTHOR_ME = "__me__"

private fun authorKey(message: Message): String =
    when (message.type) {
        MessageType.Sent, MessageType.Outbox, MessageType.Queued, MessageType.Failed -> AUTHOR_ME
        else -> message.address
    }

private val FMT_DAY_IN_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM")
private val FMT_DAY_OTHER_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

private fun formatDayHeader(date: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(date, today)
    val locale = Locale.getDefault()
    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days in 2..6 -> date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        date.year == today.year -> date.format(FMT_DAY_IN_YEAR)
        else -> date.format(FMT_DAY_OTHER_YEAR)
    }
}
