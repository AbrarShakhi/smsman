package com.abrarshakhi.smsman.features.chat.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.util.formatDayDivider
import com.abrarshakhi.smsman.core.model.DeliveryStatus
import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.model.MessageType
import com.abrarshakhi.smsman.core.model.SimInfo
import com.abrarshakhi.smsman.core.telephony.SegmentInfo
import java.text.DateFormat
import java.util.Date

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    highlightMessageId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> Box(modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        state.error != null -> Box(modifier.fillMaxSize(), Alignment.Center) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
        }

        else -> Column(modifier.fillMaxSize()) {
            if (state.inSelectionMode) {
                SelectionBar(
                    count = state.selectedIds.size,
                    pinAction = state.selectionPinAction,
                    onPin = viewModel::onPinSelected,
                    onDelete = viewModel::onDeleteSelected,
                    onClose = viewModel::onClearSelection,
                )
            }

            // Expanded detail is per-message and deliberately survives rotation.
            var expandedId by rememberSaveable { mutableLongStateOf(-1L) }
            val listState = rememberLazyListState()

            // Arriving from the Pinned tab: jump to the pinned message and open its details.
            LaunchedEffect(highlightMessageId, state.items) {
                if (highlightMessageId == null) return@LaunchedEffect
                val index = state.items.indexOfFirst {
                    it is ChatItem.MessageRow && it.message.id == highlightMessageId
                }
                if (index >= 0) {
                    listState.scrollToItem(index)
                    expandedId = highlightMessageId
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                // Newest-first data + reverseLayout opens the thread at the latest message.
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(
                    items = state.items,
                    expandedId = expandedId,
                    isMultiSim = state.isMultiSim,
                    sims = state.sims,
                    selectedIds = state.selectedIds,
                    inSelectionMode = state.inSelectionMode,
                    onToggle = { id -> expandedId = if (expandedId == id) -1L else id },
                    onToggleSelection = viewModel::onToggleSelection,
                )
            }

            state.sendError?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            ComposeBar(
                draft = state.draft,
                canSend = state.canSend,
                segments = state.segments,
                sims = state.sims,
                selectedSim = state.selectedSim,
                isMultiSim = state.isMultiSim,
                onDraftChange = viewModel::onDraftChange,
                onSimSelected = viewModel::onSimSelected,
                onSend = viewModel::onSend,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(
    items: List<ChatItem>,
    expandedId: Long,
    isMultiSim: Boolean,
    sims: List<SimInfo>,
    selectedIds: Set<Long>,
    inSelectionMode: Boolean,
    onToggle: (Long) -> Unit,
    onToggleSelection: (Message) -> Unit,
) {
    items.forEach { item ->
        when (item) {
            is ChatItem.DayDivider -> item(key = "divider-${item.timestamp}") {
                DayDivider(item.timestamp)
            }

            is ChatItem.MessageRow -> item(key = "message-${item.message.id}") {
                MessageBubble(
                    row = item,
                    isExpanded = expandedId == item.message.id,
                    isMultiSim = isMultiSim,
                    sims = sims,
                    isSelected = item.message.id in selectedIds,
                    // Once selection is active a plain tap extends it, as in Messages.
                    onClick = {
                        if (inSelectionMode) onToggleSelection(item.message)
                        else onToggle(item.message.id)
                    },
                    onLongClick = { onToggleSelection(item.message) },
                )
            }
        }
    }
}

@Composable
private fun DayDivider(timestamp: Long) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(Modifier.weight(1f))
        Text(
            text = formatDayDivider(context, timestamp),
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.weight(1f))
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MessageBubble(
    row: ChatItem.MessageRow,
    isExpanded: Boolean,
    isMultiSim: Boolean,
    sims: List<SimInfo>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val message = row.message
    val outgoing = message.isOutgoing
    val corner = 18.dp
    val tail = 4.dp

    // The corner adjacent to the tail is tightened, and only the last bubble of a run gets one.
    val shape = if (outgoing) {
        RoundedCornerShape(corner, corner, if (row.isLastInGroup) tail else corner, corner)
    } else {
        RoundedCornerShape(corner, corner, corner, if (row.isLastInGroup) tail else corner)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent,
            )
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = if (row.isFirstInGroup) 8.dp else 2.dp,
                bottom = 2.dp,
            ),
        horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (message.isPinned) {
                Icon(
                    painter = painterResource(R.drawable.ic_tab_pinned),
                    contentDescription = "Pinned",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
            }
            Surface(
                shape = shape,
                color = if (outgoing) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            ) {
                Text(
                    text = message.body,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (outgoing) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }

        }

        // Which SIM carried this message, shown once per visual group to avoid repetition.
        if (isMultiSim && row.isLastInGroup) {
            val sim = sims.firstOrNull { it.subscriptionId == message.subscriptionId }
            Text(
                text = sim?.let { "SIM ${it.slotIndex + 1} · ${it.label}" } ?: "Unknown SIM",
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Everything an SMS app knows about the message, revealed on tap so the default view stays clean.
        AnimatedVisibility(visible = isExpanded) {
            MessageDetails(message = message, isMultiSim = isMultiSim, sims = sims)
        }
    }
}

@Composable
private fun MessageDetails(message: Message, isMultiSim: Boolean, sims: List<SimInfo>) {
    val sim = remember(message.subscriptionId, sims) {
        sims.firstOrNull { it.subscriptionId == message.subscriptionId }
    }
    val parts = buildList {
        add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(message.date)))
        if (isMultiSim) {
            // A removed SIM still appears in old rows, so fall back rather than showing nothing.
            add(sim?.let { "SIM ${it.slotIndex + 1} · ${it.label}" } ?: "Unknown SIM")
        }
        statusLabel(message)?.let(::add)
    }

    Text(
        text = parts.joinToString(" · "),
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (message.isOutgoing) TextAlign.End else TextAlign.Start,
    )
}

private fun statusLabel(message: Message): String? = when {
    !message.isOutgoing -> null
    message.type == MessageType.FAILED -> "Failed"
    message.type == MessageType.QUEUED || message.type == MessageType.OUTBOX -> "Sending"
    message.type == MessageType.DRAFT -> "Draft"
    message.status == DeliveryStatus.COMPLETE -> "Delivered"
    message.status == DeliveryStatus.PENDING -> "Sent"
    message.status == DeliveryStatus.FAILED -> "Not delivered"
    else -> "Sent"
}

@Composable
private fun ComposeBar(
    draft: String,
    canSend: Boolean,
    segments: SegmentInfo,
    sims: List<SimInfo>,
    selectedSim: SimInfo?,
    isMultiSim: Boolean,
    onDraftChange: (String) -> Unit,
    onSimSelected: (Int) -> Unit,
    onSend: () -> Unit,
) {
    var simMenuOpen by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        // No imePadding() here: AppRoot's Scaffold uses WindowInsets.safeDrawing, which
        // already includes the IME inset, so adding it again lifted the bar by twice the
        // keyboard height.
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Only surfaced once it matters: a second segment costs another message.
            if (segments.segments > 1) {
                Text(
                    text = "${segments.segments} messages · ${segments.remainingInSegment} left" +
                        if (segments.isUnicode) " · Unicode" else "",
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isMultiSim) {
                    Box {
                        AssistChip(
                            onClick = { simMenuOpen = true },
                            label = { Text(selectedSim?.let { "SIM ${it.slotIndex + 1}" } ?: "SIM") },
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        DropdownMenu(
                            expanded = simMenuOpen,
                            onDismissRequest = { simMenuOpen = false },
                        ) {
                            sims.forEach { sim ->
                                DropdownMenuItem(
                                    text = { Text("SIM ${sim.slotIndex + 1} · ${sim.label}") },
                                    onClick = {
                                        onSimSelected(sim.subscriptionId)
                                        simMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Text message") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 5,
                )

                FilledIconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    pinAction: Boolean,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Clear selection")
            }
            Text(
                text = "$count selected",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onPin) {
                Icon(
                    painter = painterResource(R.drawable.ic_tab_pinned),
                    contentDescription = if (pinAction) "Pin" else "Unpin",
                )
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (count == 1) "Delete message?" else "Delete $count messages?") },
            text = { Text("This permanently removes them from this device.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}
