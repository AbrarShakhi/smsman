package com.abrarshakhi.smsman.ui.newmessage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.ContactSuggestion
import com.abrarshakhi.smsman.ui.component.BackNavigationIcon
import com.abrarshakhi.smsman.ui.component.ContactAvatar
import com.abrarshakhi.smsman.ui.component.MessageComposer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import org.koin.androidx.compose.koinViewModel

@Composable
fun NewMessageRoute(
    onBack: () -> Unit,
    onMessageSent: (Long) -> Unit,
) {
    NewMessageScreen(
        viewModel = koinViewModel(),
        onBack = onBack,
        onMessageSent = onMessageSent,
    )
}

@Composable
fun NewMessageScreen(
    viewModel: NewMessageViewModel,
    onBack: () -> Unit,
    onMessageSent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnMessageSent by rememberUpdatedState(onMessageSent)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val motion = MaterialTheme.motionScheme

    LaunchedEffect(viewModel, lifecycle) {
        val threadId = viewModel.state
            .flowWithLifecycle(lifecycle)
            .mapNotNull { it.sentThreadId }
            .first()
        currentOnMessageSent(threadId)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_message_title)) },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            RecipientField(
                value = state.recipient,
                onValueChange = viewModel::onRecipientChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            AnimatedContent(
                targetState = state.suggestions.isNotEmpty(),
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
                modifier = Modifier.weight(1f),
            ) { hasSuggestions ->
                if (hasSuggestions) {
                    SuggestionList(suggestions = state.suggestions, onSelect = viewModel::onSuggestionSelected)
                } else {
                    RecipientHint(recipient = state.recipient)
                }
            }
            MessageComposer(
                draft = state.draft,
                onDraftChange = viewModel::onDraftChange,
                canSend = state.canSend,
                onSend = viewModel::onSend,
                segments = state.segments,
                sims = state.sims,
                selectedSim = state.selectedSim,
                onSimSelected = viewModel::onSimSelected,
                error = state.error,
            )
        }
    }
}

@Composable
private fun RecipientField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        leadingIcon = {
            Text(
                text = stringResource(R.string.new_message_to),
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = colors.primary,
            )
        },
        placeholder = { Text(stringResource(R.string.new_message_recipient_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainerHigh,
            unfocusedContainerColor = colors.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun SuggestionList(
    suggestions: List<ContactSuggestion>,
    onSelect: (ContactSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        itemsIndexed(suggestions, key = { _, suggestion -> suggestion.number }) { index, suggestion ->
            SegmentedListItem(
                onClick = { onSelect(suggestion) },
                shapes = ListItemDefaults.segmentedShapes(index = index, count = suggestions.size),
                leadingContent = {
                    ContactAvatar(
                        displayName = suggestion.label,
                        colorIndex = suggestion.number.hashCode(),
                        size = 40.dp,
                    )
                },
                supportingContent = { Text(suggestion.number) },
                colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Text(text = suggestion.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun RecipientHint(recipient: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (recipient.isBlank()) {
                stringResource(R.string.new_message_hint_empty)
            } else {
                stringResource(R.string.new_message_hint_no_match)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
