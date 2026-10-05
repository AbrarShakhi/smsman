package com.abrarshakhi.smsman.ui.conversations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.Conversation
import com.abrarshakhi.smsman.ui.component.ContactAvatar
import com.abrarshakhi.smsman.ui.component.EmptyState
import com.abrarshakhi.smsman.ui.component.ErrorContent
import com.abrarshakhi.smsman.ui.component.HomeTopBar
import com.abrarshakhi.smsman.ui.component.Illustration
import com.abrarshakhi.smsman.ui.component.ListPhase
import com.abrarshakhi.smsman.ui.component.LoadingContent
import com.abrarshakhi.smsman.ui.component.listPhaseOf
import com.abrarshakhi.smsman.ui.util.formatConversationTime
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember {
        derivedStateOf { !listState.lastScrolledForward || !listState.canScrollBackward }
    }
    val motion = MaterialTheme.motionScheme

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
                subtitle = homeSubtitle(state, favoritesOnly),
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (onStartChat != null) {
                val label = stringResource(R.string.action_start_chat)
                SmallExtendedFloatingActionButton(
                    text = { Text(label) },
                    icon = { Icon(Icons.AutoMirrored.Rounded.Message, contentDescription = label) },
                    onClick = onStartChat,
                    expanded = fabExpanded,
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        AnimatedContent(
            targetState = listPhaseOf(state.isLoading, state.error, state.conversations.isEmpty()),
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            modifier = Modifier.padding(padding),
        ) { phase ->
            when (phase) {
                ListPhase.Loading -> LoadingContent()
                ListPhase.Error -> ErrorContent(message = state.error.orEmpty())
                ListPhase.Empty -> if (favoritesOnly) {
                    EmptyState(
                        illustration = Illustration.EmptyFavorites,
                        title = stringResource(R.string.empty_favorites_title),
                        message = stringResource(R.string.empty_favorites_message),
                    )
                } else {
                    EmptyState(
                        illustration = Illustration.EmptyInbox,
                        title = stringResource(R.string.empty_inbox_title),
                        message = stringResource(R.string.empty_inbox_message),
                    )
                }

                ListPhase.Content -> ConversationList(
                    conversations = state.conversations,
                    listState = listState,
                    onOpenConversation = onOpenConversation,
                )
            }
        }
    }
}

@Composable
private fun homeSubtitle(state: ConversationsState, favoritesOnly: Boolean): String? {
    if (state.isLoading || state.error != null) return null
    val count = state.conversations.size
    if (favoritesOnly) {
        return if (count == 0) null else pluralStringResource(R.plurals.favorites_count, count, count)
    }
    val unread = state.conversations.count { it.isUnread }
    return if (unread == 0) {
        stringResource(R.string.home_all_caught_up)
    } else {
        pluralStringResource(R.plurals.home_unread_count, unread, unread)
    }
}

@Composable
private fun ConversationList(
    conversations: List<Conversation>,
    listState: LazyListState,
    onOpenConversation: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = ListBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        itemsIndexed(conversations, key = { _, conversation -> conversation.threadId }) { index, conversation ->
            ConversationRow(
                conversation = conversation,
                shapes = ListItemDefaults.segmentedShapes(index = index, count = conversations.size),
                onClick = { onOpenConversation(conversation.threadId) },
                modifier = Modifier.animateItem(
                    fadeInSpec = motion.defaultEffectsSpec(),
                    placementSpec = motion.defaultSpatialSpec(),
                    fadeOutSpec = motion.fastEffectsSpec(),
                ),
            )
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val unread = conversation.isUnread

    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        leadingContent = {
            ContactAvatar(
                displayName = conversation.displayName,
                colorIndex = conversation.avatarColorIndex,
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (conversation.hasAttachment) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = conversation.snippet.ifBlank { stringResource(R.string.conversation_no_preview) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (unread) FontWeight.SemiBold else null,
                    color = if (unread) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = formatConversationTime(context, conversation.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (unread) colors.primary else colors.onSurfaceVariant,
                )
                when {
                    unread -> UnreadBadge(count = conversation.unreadCount)
                    conversation.isFavorite -> Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = stringResource(R.string.conversation_favorite),
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        },
        colors = ListItemDefaults.segmentedColors(
            containerColor = if (unread) colors.surfaceContainerHighest else colors.surfaceContainer,
        ),
    ) {
        Text(
            text = conversation.displayName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = if (unread) {
                MaterialTheme.typography.titleMediumEmphasized
            } else {
                MaterialTheme.typography.titleMedium
            },
        )
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val description = pluralStringResource(R.plurals.conversation_unread_badge, count, count)
    Badge(modifier = Modifier.clearAndSetSemantics { contentDescription = description }) {
        Text(count.toString())
    }
}

private val ListBottomPadding = 112.dp
