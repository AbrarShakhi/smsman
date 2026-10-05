package com.abrarshakhi.smsman.features.conversations.presentation

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.ui.components.ContactAvatar
import com.abrarshakhi.smsman.common.ui.components.HomeTopBar
import com.abrarshakhi.smsman.common.util.formatConversationTime
import com.abrarshakhi.smsman.core.model.Conversation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ConversationsRoute(
    favoritesOnly: Boolean,
    onOpenConversation: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartChat: (() -> Unit)? = null,
) {
    ConversationsScreen(
        viewModel = koinViewModel { parametersOf(favoritesOnly) },
        favoritesOnly = favoritesOnly,
        onOpenConversation = onOpenConversation,
        onOpenSearch = onOpenSearch,
        onOpenSettings = onOpenSettings,
        onStartChat = onStartChat,
    )
}

@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel,
    favoritesOnly: Boolean,
    onOpenConversation: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onStartChat: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopBar(
                title = if (favoritesOnly) {
                    stringResource(R.string.tab_favorite)
                } else {
                    stringResource(R.string.tab_all_messages)
                },
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (onStartChat != null) {
                ExtendedFloatingActionButton(
                    onClick = onStartChat,
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null) },
                    text = { Text(stringResource(R.string.action_start_chat)) },
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        val contentModifier = Modifier.padding(padding)

        when {
            state.isLoading -> Box(contentModifier.fillMaxSize(), Alignment.TopStart) {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error != null -> EmptyMessage(
                modifier = contentModifier,
                text = state.error ?: "",
            )

            state.conversations.isEmpty() -> EmptyMessage(
                modifier = contentModifier,
                text = if (favoritesOnly) {
                    stringResource(R.string.no_fac_conv_yet)
                } else {
                    stringResource(R.string.no_conv_yet)
                },
            )

            else -> LazyColumn(
                modifier = contentModifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(state.conversations, key = { it.threadId }) { conversation ->
                    ConversationRow(
                        conversation = conversation,
                        onClick = { onOpenConversation(conversation.threadId) },
                    )
                }
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = {},
                onClickLabel = "open chat",
                onLongClickLabel = "chat option"
            )
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
                fontWeight = if (conversation.isUnread) FontWeight.Bold else FontWeight.Normal,
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
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favourite",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
