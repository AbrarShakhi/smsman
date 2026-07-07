package com.abrarshakhi.smsman.ui.screens.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.domain.model.Message
import com.abrarshakhi.smsman.domain.model.MessageProtocol
import com.abrarshakhi.smsman.domain.model.MessageStatus
import com.abrarshakhi.smsman.domain.model.MessageType
import com.abrarshakhi.smsman.ui.screens.chat.ChatItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    item: ChatItem.Msg,
    modifier: Modifier = Modifier,
    onLongPress: (Long) -> Unit = {},
) {
    val message = item.message
    val isMe = item.isMe

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 1.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
        ) {
            Surface(
                color = bubbleColor(isMe),
                shape = bubbleShape(isMe = isMe, isLastInRun = item.isLastInRun),
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { onLongPress(message.id) },
                ),
            ) {
                val isMmsNoBody = message.protocol == MessageProtocol.Mms && message.body.isNullOrBlank()
                Text(
                    text = if (isMmsNoBody) "MMS" else message.body.orEmpty(),
                    style = if (isMmsNoBody) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyLarge,
                    color = bubbleTextColor(isMe),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
            if (item.showTimeLabel) {
                Spacer(Modifier.size(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTime(message.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    if (isMe) {
                        statusIcon(message)?.let { (icon, tint) ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = tint,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayHeader(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun bubbleColor(isMe: Boolean) =
    if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

@Composable
private fun bubbleTextColor(isMe: Boolean) =
    if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

private fun bubbleShape(isMe: Boolean, isLastInRun: Boolean): RoundedCornerShape {
    val full = 18.dp
    val tail = 4.dp
    return if (isMe) {
        RoundedCornerShape(
            topStart = full,
            topEnd = full,
            bottomEnd = if (isLastInRun) tail else full,
            bottomStart = full,
        )
    } else {
        RoundedCornerShape(
            topStart = full,
            topEnd = full,
            bottomEnd = full,
            bottomStart = if (isLastInRun) tail else full,
        )
    }
}

private fun formatTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)

@Composable
private fun statusIcon(message: Message): Pair<ImageVector, androidx.compose.ui.graphics.Color>? {
    val scheme = MaterialTheme.colorScheme
    return when {
        message.type == MessageType.Outbox || message.type == MessageType.Queued ->
            Icons.Default.Schedule to scheme.onSurfaceVariant
        message.type == MessageType.Failed ->
            Icons.Default.ErrorOutline to scheme.error
        message.status == MessageStatus.Delivered || message.status == MessageStatus.Read ->
            Icons.Default.DoneAll to scheme.primary
        message.type == MessageType.Sent ->
            Icons.Default.Check to scheme.onSurfaceVariant
        else -> null
    }
}
