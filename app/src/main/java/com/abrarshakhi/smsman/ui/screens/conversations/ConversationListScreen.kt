package com.abrarshakhi.smsman.ui.screens.conversations

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.domain.model.ConversationFilter
import com.abrarshakhi.smsman.ui.components.ContactAvatar
import com.abrarshakhi.smsman.ui.util.formatRelativeListTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (Long) -> Unit,
    onComposeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val defaultAppLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.onResume() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ConversationListEffect.OpenChat -> onConversationClick(effect.threadId)
                is ConversationListEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
                is ConversationListEffect.RequestDefaultSmsApp -> defaultAppLauncher.launch(effect.intent)
            }
        }
    }

    Scaffold(
        topBar = {
            if (state.isSelecting) {
                SelectionTopBar(
                    selectedCount = state.selectedIds.size,
                    onClear = { viewModel.onIntent(ConversationListIntent.ClearSelection) },
                    onMarkRead = { viewModel.onIntent(ConversationListIntent.MarkSelectedRead) },
                    onArchive = { viewModel.onIntent(ConversationListIntent.ArchiveSelected) },
                    onDelete = { viewModel.onIntent(ConversationListIntent.DeleteSelected) },
                    onPin = { viewModel.onIntent(ConversationListIntent.PinSelected) },
                    onMute = { viewModel.onIntent(ConversationListIntent.MuteSelected) },
                )
            } else {
                MainTopBar(
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                )
            }
        },
        floatingActionButton = {
            if (!state.isSelecting) {
                FloatingActionButton(onClick = onComposeClick) {
                    Icon(Icons.Default.Add, contentDescription = "New message")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!state.isDefaultSmsApp) {
                DefaultAppBanner(
                    onSet = { viewModel.onIntent(ConversationListIntent.RequestDefaultApp) },
                )
            }
            FilterTabs(
                current = state.filter,
                onSelect = { viewModel.onIntent(ConversationListIntent.SetFilter(it)) },
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.rows.isEmpty() -> EmptyState(filter = state.filter)
                    else -> ConversationList(
                        rows = state.rows,
                        selectedIds = state.selectedIds,
                        onClick = { viewModel.onIntent(ConversationListIntent.Click(it)) },
                        onLongClick = { viewModel.onIntent(ConversationListIntent.LongClick(it)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "SMS Man",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClear: () -> Unit,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onMute: () -> Unit,
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, contentDescription = "Clear selection")
            }
        },
        actions = {
            IconButton(onClick = onPin) {
                Icon(Icons.Default.PushPin, contentDescription = "Pin")
            }
            IconButton(onClick = onMute) {
                Icon(Icons.Default.NotificationsOff, contentDescription = "Mute")
            }
            IconButton(onClick = onMarkRead) {
                Icon(Icons.Default.MarkEmailRead, contentDescription = "Mark as read")
            }
            IconButton(onClick = onArchive) {
                Icon(Icons.Default.Archive, contentDescription = "Archive")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}

@Composable
private fun DefaultAppBanner(onSet: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Not default SMS app",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Tap to set SMS Man as default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            AssistChip(
                onClick = onSet,
                label = { Text("Set default") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    labelColor = MaterialTheme.colorScheme.onError,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterTabs(
    current: ConversationFilter,
    onSelect: (ConversationFilter) -> Unit,
) {
    val visible = listOf(ConversationFilter.All, ConversationFilter.Unread, ConversationFilter.Archived, ConversationFilter.Blocked)
    val index = visible.indexOf(current).coerceAtLeast(0)
    PrimaryTabRow(selectedTabIndex = index) {
        visible.forEachIndexed { idx, filter ->
            Tab(
                selected = idx == index,
                onClick = { onSelect(filter) },
                text = { Text(filter.label, style = MaterialTheme.typography.labelLarge) },
            )
        }
    }
}

private val ConversationFilter.label: String
    get() = when (this) {
        ConversationFilter.All -> "All"
        ConversationFilter.Unread -> "Unread"
        ConversationFilter.Archived -> "Archived"
        ConversationFilter.Blocked -> "Blocked"
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationList(
    rows: List<ConversationRow>,
    selectedIds: Set<Long>,
    onClick: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(rows, key = { it.conversation.threadId }) { row ->
            ConversationRowItem(
                row = row,
                selected = row.conversation.threadId in selectedIds,
                onClick = { onClick(row.conversation.threadId) },
                onLongClick = { onLongClick(row.conversation.threadId) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRowItem(
    row: ConversationRow,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isUnread = row.conversation.unreadCount > 0
    val bg = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(
            contact = row.contact,
            fallbackLabel = row.conversation.recipientAddresses.firstOrNull().orEmpty(),
            size = 48.dp,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.contact?.displayLabel
                    ?: row.conversation.recipientAddresses.firstOrNull().orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = row.conversation.draft?.let { "Draft: $it" } ?: row.conversation.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    row.conversation.draft != null -> MaterialTheme.colorScheme.error
                    isUnread -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatRelativeListTime(row.conversation.lastMessageAt),
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnread) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isUnread) {
                Spacer(Modifier.height(6.dp))
                Badge { Text(row.conversation.unreadCount.toString()) }
            }
        }
    }
}

@Composable
private fun EmptyState(filter: ConversationFilter) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = when (filter) {
                ConversationFilter.All -> "No conversations yet"
                ConversationFilter.Unread -> "No unread messages"
                ConversationFilter.Archived -> "No archived conversations"
                ConversationFilter.Blocked -> "No blocked numbers"
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = when (filter) {
                ConversationFilter.All -> "Tap + to start a new message."
                ConversationFilter.Unread -> "You're all caught up."
                ConversationFilter.Archived -> "Archived conversations show up here."
                ConversationFilter.Blocked -> "Numbers you block will appear here."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
