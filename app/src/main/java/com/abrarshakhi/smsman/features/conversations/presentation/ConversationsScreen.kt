package com.abrarshakhi.smsman.features.conversations.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.LocalAppBackStack
import com.abrarshakhi.smsman.common.navigation.navigateTo
import com.abrarshakhi.smsman.common.ui.components.ContactAvatar
import com.abrarshakhi.smsman.common.util.formatConversationTime
import com.abrarshakhi.smsman.core.model.Conversation

@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel,
    favoritesOnly: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = LocalAppBackStack.current

    when {
        state.isLoading -> Box(modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        state.error != null -> EmptyMessage(
            modifier = modifier,
            text = state.error ?: "",
        )

        state.conversations.isEmpty() -> EmptyMessage(
            modifier = modifier,
            text = if (favoritesOnly) {
                "No favourite conversations yet."
            } else {
                "No conversations yet."
            },
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            // Clear the extended FAB so the last row is never hidden behind it.
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            items(state.conversations, key = { it.threadId }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    onClick = { backStack.navigateTo(AppRouteKey.Chat(conversation.threadId)) },
                )
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(
            displayName = conversation.displayName,
            colorIndex = conversation.avatarColorIndex,
        )
        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = conversation.displayName,
                style = MaterialTheme.typography.bodyLarge,
                // Unread conversations read heavier, as in Messages.
                fontWeight = if (conversation.isUnread) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (conversation.hasAttachment) {
                    Text(
                        text = "📎 ",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = conversation.snippet.ifBlank { "(no preview)" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (conversation.isUnread) FontWeight.Medium else FontWeight.Normal,
                    color = if (conversation.isUnread) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
            Text(
                text = formatConversationTime(context, conversation.date),
                style = MaterialTheme.typography.labelSmall,
                color = if (conversation.isUnread) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (conversation.isFavorite) {
                Spacer(Modifier.size(4.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_tab_favorite),
                    contentDescription = "Favourite",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
