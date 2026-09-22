package com.abrarshakhi.smsman.features.chat.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.common.main.BackNavigationIcon
import com.abrarshakhi.smsman.common.main.ScreenChrome
import com.abrarshakhi.smsman.common.ui.components.ContactAvatar
import com.abrarshakhi.smsman.core.model.Conversation
import com.abrarshakhi.smsman.core.repository.ThreadTitle
import com.abrarshakhi.smsman.core.repository.MessageMetadataRepository
import com.abrarshakhi.smsman.core.repository.ThreadTitleResolver
import androidx.compose.ui.res.painterResource
import com.abrarshakhi.smsman.R
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

fun chatChrome(threadId: Long) = ScreenChrome(
    title = "Chat",
    topBar = { backStack, scrollBehavior ->
        val resolver: ThreadTitleResolver = koinInject()
        val metadata: MessageMetadataRepository = koinInject()
        val scope = rememberCoroutineScope()
        val favorites by metadata.observeFavoriteThreadIds()
            .collectAsState(initial = emptySet())
        val isFavorite = threadId in favorites
        // Resolved here rather than read from ChatViewModel: the top bar is rendered by AppRoot's
        // Scaffold, outside the NavEntry's ViewModel store, so it cannot share that instance.
        val threadTitle by produceState(ThreadTitle("Conversation", null), threadId) {
            value = resolver.resolve(threadId)
        }

        TopAppBar(
            navigationIcon = { BackNavigationIcon(backStack) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ContactAvatar(
                        displayName = threadTitle.title,
                        colorIndex = (threadId % Conversation.AVATAR_COLOR_SLOTS).toInt(),
                        size = 32,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = threadTitle.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        threadTitle.subtitle?.let { subtitle ->
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = { scope.launch { metadata.setFavorite(threadId, !isFavorite) } },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tab_favorite),
                        contentDescription = if (isFavorite) "Remove favourite" else "Add favourite",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            },
            scrollBehavior = scrollBehavior,
        )
    },
)
