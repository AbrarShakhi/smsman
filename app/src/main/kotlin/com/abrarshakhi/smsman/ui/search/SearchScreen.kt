package com.abrarshakhi.smsman.ui.search

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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.Conversation
import com.abrarshakhi.smsman.model.SearchResults
import com.abrarshakhi.smsman.ui.component.ContactAvatar
import com.abrarshakhi.smsman.ui.component.EmptyState
import com.abrarshakhi.smsman.ui.component.Illustration
import com.abrarshakhi.smsman.ui.component.LoadingContent
import com.abrarshakhi.smsman.ui.component.SectionTitle
import com.abrarshakhi.smsman.ui.util.formatConversationTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onOpenMessage: (threadId: Long, messageId: Long) -> Unit,
    onStartChat: () -> Unit,
) {
    SearchScreen(
        viewModel = koinViewModel(),
        onBack = onBack,
        onOpenMessage = onOpenMessage,
        onStartChat = onStartChat,
    )
}

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onOpenMessage: (threadId: Long, messageId: Long) -> Unit,
    onStartChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query = rememberTextFieldState(initialText = state.query)
    val motion = MaterialTheme.motionScheme

    LaunchedEffect(query, viewModel) {
        snapshotFlow { query.text.toString() }.collect(viewModel::onQueryChange)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { SearchTopBar(query = query, onBack = onBack) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        AnimatedContent(
            targetState = searchPhaseOf(state),
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            modifier = Modifier.padding(padding),
        ) { phase ->
            when (phase) {
                SearchPhase.Idle -> EmptyState(
                    illustration = Illustration.Search,
                    title = stringResource(R.string.search_idle_title),
                    message = stringResource(R.string.search_idle_message),
                )

                SearchPhase.Searching -> LoadingContent()
                SearchPhase.NoResults -> EmptyState(
                    illustration = Illustration.NoResults,
                    title = stringResource(R.string.search_no_results_title),
                    message = stringResource(R.string.search_no_results_message, state.query),
                )

                SearchPhase.Results -> SearchResultList(
                    results = state.results,
                    onOpenMessage = onOpenMessage,
                    onStartChat = onStartChat,
                )
            }
        }
    }
}

private enum class SearchPhase { Idle, Searching, NoResults, Results }

private fun searchPhaseOf(state: SearchState): SearchPhase = when {
    state.query.isBlank() -> SearchPhase.Idle
    !state.results.isEmpty -> SearchPhase.Results
    state.isSearching -> SearchPhase.Searching
    else -> SearchPhase.NoResults
}

@Composable
private fun SearchResultList(
    results: SearchResults,
    onOpenMessage: (threadId: Long, messageId: Long) -> Unit,
    onStartChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val itemColors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        if (results.people.isNotEmpty()) {
            item(key = "people-title") {
                SectionTitle(
                    text = stringResource(R.string.search_people),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                )
            }
            itemsIndexed(results.people, key = { _, person -> "person-${person.number}" }) { index, person ->
                SegmentedListItem(
                    onClick = onStartChat,
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = results.people.size),
                    leadingContent = {
                        ContactAvatar(displayName = person.label, colorIndex = person.number.hashCode(), size = 40.dp)
                    },
                    supportingContent = { Text(person.number) },
                    colors = itemColors,
                ) {
                    Text(text = person.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (results.messages.isNotEmpty()) {
            item(key = "messages-title") {
                SectionTitle(
                    text = stringResource(R.string.search_messages),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                )
            }
            itemsIndexed(results.messages, key = { _, hit -> "message-${hit.message.id}" }) { index, hit ->
                SegmentedListItem(
                    onClick = { onOpenMessage(hit.message.threadId, hit.message.id) },
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = results.messages.size),
                    leadingContent = {
                        ContactAvatar(
                            displayName = hit.senderLabel,
                            colorIndex = (hit.message.threadId % Conversation.AVATAR_COLOR_SLOTS).toInt(),
                            size = 40.dp,
                        )
                    },
                    supportingContent = {
                        Text(text = hit.message.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    },
                    trailingContent = {
                        Text(
                            text = formatConversationTime(context, hit.message.date),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = itemColors,
                ) {
                    Text(
                        text = hit.senderLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                }
            }
        }
    }
}
