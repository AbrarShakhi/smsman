package com.abrarshakhi.smsman.features.pinned.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.LocalAppBackStack
import com.abrarshakhi.smsman.common.navigation.navigateTo
import com.abrarshakhi.smsman.common.ui.components.ContactAvatar
import com.abrarshakhi.smsman.common.util.formatConversationTime
import com.abrarshakhi.smsman.core.model.Conversation
import com.abrarshakhi.smsman.core.repository.PinnedMessage

@Composable
fun PinnedScreen(viewModel: PinnedViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = LocalAppBackStack.current

    when {
        state.isLoading -> Box(modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        state.error != null -> Box(modifier.fillMaxSize(), Alignment.Center) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
        }

        state.pinned.isEmpty() -> Box(modifier.fillMaxSize(), Alignment.Center) {
            Text(
                text = "No pinned messages yet.\nLong-press a message to pin it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        else -> LazyColumn(modifier.fillMaxSize()) {
            items(state.pinned, key = { it.message.id }) { pinned ->
                PinnedRow(
                    pinned = pinned,
                    onClick = {
                        // Opens the thread and highlights the message the pin points at.
                        backStack.navigateTo(
                            AppRouteKey.Chat(
                                threadId = pinned.message.threadId,
                                highlightMessageId = pinned.message.id,
                            ),
                        )
                    },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun PinnedRow(pinned: PinnedMessage, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ContactAvatar(
            displayName = pinned.senderLabel,
            colorIndex = (pinned.message.threadId % Conversation.AVATAR_COLOR_SLOTS).toInt(),
            size = 40,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pinned.senderLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatConversationTime(context, pinned.message.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(2.dp))
            Text(
                text = pinned.message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
