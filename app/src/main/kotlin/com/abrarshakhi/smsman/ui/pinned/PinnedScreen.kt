package com.abrarshakhi.smsman.ui.pinned

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.Conversation
import com.abrarshakhi.smsman.model.PinnedMessage
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

@Composable
fun PinnedRoute(
    onOpenMessage: (threadId: Long, messageId: Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    PinnedScreen(
        viewModel = koinViewModel(),
        onOpenMessage = onOpenMessage,
        onOpenSearch = onOpenSearch,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun PinnedScreen(
    viewModel: PinnedViewModel,
    onOpenMessage: (threadId: Long, messageId: Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val motion = MaterialTheme.motionScheme
    val count = state.pinned.size

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopBar(
                title = stringResource(R.string.tab_pinned),
                subtitle = if (state.isLoading || count == 0) {
                    null
                } else {
                    pluralStringResource(R.plurals.pinned_count, count, count)
                },
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        AnimatedContent(
            targetState = listPhaseOf(state.isLoading, state.error, state.pinned.isEmpty()),
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            modifier = Modifier.padding(padding),
        ) { phase ->
            when (phase) {
                ListPhase.Loading -> LoadingContent()
                ListPhase.Error -> ErrorContent(message = state.error.orEmpty())
                ListPhase.Empty -> EmptyState(
                    illustration = Illustration.EmptyPinned,
                    title = stringResource(R.string.empty_pinned_title),
                    message = stringResource(R.string.empty_pinned_message),
                )

                ListPhase.Content -> PinnedList(pinned = state.pinned, onOpenMessage = onOpenMessage)
            }
        }
    }
}

@Composable
private fun PinnedList(
    pinned: List<PinnedMessage>,
    onOpenMessage: (threadId: Long, messageId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        itemsIndexed(pinned, key = { _, item -> item.message.id }) { index, item ->
            PinnedRow(
                pinned = item,
                shapes = ListItemDefaults.segmentedShapes(index = index, count = pinned.size),
                onClick = { onOpenMessage(item.message.threadId, item.message.id) },
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
private fun PinnedRow(
    pinned: PinnedMessage,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        leadingContent = {
            ContactAvatar(
                displayName = pinned.senderLabel,
                colorIndex = (pinned.message.threadId % Conversation.AVATAR_COLOR_SLOTS).toInt(),
                size = 40.dp,
            )
        },
        supportingContent = {
            Text(text = pinned.message.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Text(
                text = formatConversationTime(context, pinned.message.date),
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Text(
            text = pinned.senderLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
    }
}
