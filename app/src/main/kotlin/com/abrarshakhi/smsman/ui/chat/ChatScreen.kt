package com.abrarshakhi.smsman.ui.chat

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.DeliveryStatus
import com.abrarshakhi.smsman.model.Message
import com.abrarshakhi.smsman.model.MessageType
import com.abrarshakhi.smsman.model.SimInfo
import com.abrarshakhi.smsman.ui.component.EmptyState
import com.abrarshakhi.smsman.ui.component.ErrorContent
import com.abrarshakhi.smsman.ui.component.Illustration
import com.abrarshakhi.smsman.ui.component.ListPhase
import com.abrarshakhi.smsman.ui.component.LoadingContent
import com.abrarshakhi.smsman.ui.component.MessageComposer
import com.abrarshakhi.smsman.ui.component.SwipeToGoBack
import com.abrarshakhi.smsman.ui.component.listPhaseOf
import com.abrarshakhi.smsman.ui.util.formatDayDivider
import com.abrarshakhi.smsman.ui.util.formatMessageTime
import com.abrarshakhi.smsman.ui.util.formatMessageTimestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatRoute(
    threadId: Long,
    highlightMessageId: Long?,
    onBack: () -> Unit,
) {
    ChatScreen(
        viewModel = koinViewModel { parametersOf(threadId) },
        highlightMessageId = highlightMessageId,
        onBack = onBack,
    )
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    highlightMessageId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnBack by rememberUpdatedState(onBack)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val motion = MaterialTheme.motionScheme

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.state.flowWithLifecycle(lifecycle).first { it.isClosed }
        currentOnBack()
    }

    BackHandler(enabled = state.inSelectionMode, onBack = viewModel::onClearSelection)

    SwipeToGoBack(modifier = modifier.fillMaxSize(), enabled = !state.inSelectionMode) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                AnimatedContent(
                    targetState = state.selectedIds.size.takeIf { it > 0 },
                    contentKey = { count -> count != null },
                    transitionSpec = {
                        fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
                    },
                ) { selectedCount ->
                    if (selectedCount == null) {
                        ConversationTopBar(
                            title = state.title,
                            avatarColorIndex = state.avatarColorIndex,
                            isFavorite = state.isFavorite,
                            onBack = onBack,
                            onToggleFavorite = viewModel::onToggleFavorite,
                            onMarkUnread = viewModel::onMarkUnread,
                            onDeleteConversation = viewModel::onDeleteConversation,
                            scrollBehavior = scrollBehavior,
                        )
                    } else {
                        SelectionTopBar(
                            count = selectedCount,
                            pinAction = state.selectionPinAction,
                            onClearSelection = viewModel::onClearSelection,
                            onTogglePin = viewModel::onPinSelected,
                            onDelete = viewModel::onDeleteSelected,
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                AnimatedContent(
                    targetState = listPhaseOf(state.isLoading, state.error, state.items.isEmpty()),
                    transitionSpec = {
                        fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
                    },
                    modifier = Modifier.weight(1f),
                ) { phase ->
                    when (phase) {
                        ListPhase.Loading -> LoadingContent()
                        ListPhase.Error -> ErrorContent(message = state.error.orEmpty())
                        ListPhase.Empty -> EmptyState(
                            illustration = Illustration.EmptyInbox,
                            title = stringResource(R.string.chat_empty_title),
                            message = stringResource(R.string.chat_empty_message),
                        )

                        ListPhase.Content -> MessageList(
                            items = state.items,
                            sims = state.sims,
                            selectedIds = state.selectedIds,
                            highlightMessageId = highlightMessageId,
                            onToggleSelection = viewModel::onToggleSelection,
                        )
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
                    error = state.sendError,
                )
            }
        }
    }
}

@Composable
private fun MessageList(
    items: List<ChatItem>,
    sims: List<SimInfo>,
    selectedIds: Set<Long>,
    highlightMessageId: Long?,
    onToggleSelection: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val motion = MaterialTheme.motionScheme
    val currentItems by rememberUpdatedState(items)
    val simsById = remember(sims) { sims.associateBy(SimInfo::subscriptionId) }
    val showSim = sims.size > 1
    val inSelectionMode = selectedIds.isNotEmpty()

    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var highlightHandled by rememberSaveable { mutableStateOf(false) }
    var flashingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(highlightMessageId, items) {
        if (highlightMessageId == null || highlightHandled) return@LaunchedEffect
        val index = items.indexOfFirst { it is ChatItem.MessageRow && it.message.id == highlightMessageId }
        if (index < 0) return@LaunchedEffect
        highlightHandled = true
        expandedId = highlightMessageId
        flashingId = highlightMessageId
        listState.scrollToItem(index)
    }

    LaunchedEffect(flashingId) {
        if (flashingId == null) return@LaunchedEffect
        delay(HIGHLIGHT_MILLIS)
        flashingId = null
    }

    LaunchedEffect(listState) {
        snapshotFlow { (currentItems.firstOrNull() as? ChatItem.MessageRow)?.message }
            .filterNotNull()
            .distinctUntilChangedBy(Message::id)
            .drop(1)
            .collect { newest ->
                if (newest.isOutgoing || listState.firstVisibleItemIndex <= 1) {
                    listState.animateScrollToItem(0)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        items(items = items, key = ChatItem::key, contentType = { item -> item::class }) { item ->
            val itemModifier = Modifier.animateItem(
                fadeInSpec = motion.defaultEffectsSpec(),
                placementSpec = motion.defaultSpatialSpec(),
                fadeOutSpec = motion.fastEffectsSpec(),
            )
            when (item) {
                is ChatItem.DayDivider -> DayDivider(timestamp = item.timestamp, modifier = itemModifier)
                is ChatItem.MessageRow -> {
                    val message = item.message
                    MessageRow(
                        row = item,
                        sim = simsById[message.subscriptionId],
                        showSim = showSim,
                        isSelected = message.id in selectedIds,
                        isExpanded = message.id == expandedId,
                        isHighlighted = message.id == flashingId,
                        onClick = {
                            if (inSelectionMode) {
                                onToggleSelection(message)
                            } else {
                                expandedId = if (expandedId == message.id) null else message.id
                            }
                        },
                        onLongClick = { onToggleSelection(message) },
                        modifier = itemModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayDivider(timestamp: Long, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(
                text = formatDayDivider(context, timestamp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageRow(
    row: ChatItem.MessageRow,
    sim: SimInfo?,
    showSim: Boolean,
    isSelected: Boolean,
    isExpanded: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = row.message
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val accent = if (message.isOutgoing) colors.primary else colors.tertiary
    val status = outgoingStatusOf(message)
    val entrance by rememberEntranceProgress(message)
    val selection by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = motion.defaultEffectsSpec(),
    )
    val highlight by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0f,
        animationSpec = tween(if (isHighlighted) HIGHLIGHT_IN_MILLIS else HIGHLIGHT_OUT_MILLIS),
    )
    val selectedColor = colors.secondaryContainer
    val highlightColor = colors.tertiaryContainer
    val enter = expandVertically(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec())
    val exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec())

    Column(modifier = modifier.fillMaxWidth()) {
        if (row.isFirstInGroup) {
            SenderHeader(
                name = senderLabel(row),
                color = accent,
                time = formatMessageTime(LocalContext.current, message.date),
                simLabel = if (showSim) simShortLabel(sim) else null,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .drawBehind {
                    if (highlight > 0f) drawRect(highlightColor.copy(alpha = highlight))
                    if (selection > 0f) drawRect(selectedColor.copy(alpha = selection))
                }
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .height(IntrinsicSize.Min)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(AccentWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleY = entrance
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                    .background(accent, CircleShape),
            )
            Text(
                text = message.body,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
                    .graphicsLayer {
                        translationX = (1f - entrance) * -EntranceShift.toPx()
                        translationY = (1f - entrance) * EntranceShift.toPx()
                    },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
            )
            if (message.isPinned) {
                Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = stringResource(R.string.chat_pinned),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp),
                    tint = colors.tertiary,
                )
            }
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
                exit = scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                    tint = colors.primary,
                )
            }
        }
        AnimatedVisibility(
            visible = status != null && (row.isLastInGroup || status.isError),
            enter = enter,
            exit = exit,
        ) {
            if (status != null) DeliveryFooter(status = status)
        }
        AnimatedVisibility(visible = isExpanded, enter = enter, exit = exit) {
            MessageDetails(message = message, sim = sim, showSim = showSim)
        }
    }
}

@Composable
private fun SenderHeader(
    name: String,
    color: Color,
    time: String,
    simLabel: String?,
) {
    Row(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = color,
            maxLines = 1,
        )
        Text(
            text = listOfNotNull(time, simLabel).joinToString(SEPARATOR),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun DeliveryFooter(status: OutgoingStatus) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = status,
        transitionSpec = {
            (slideInVertically(motion.fastSpatialSpec()) { it } + fadeIn(motion.fastEffectsSpec()))
                .togetherWith(slideOutVertically(motion.fastSpatialSpec()) { -it } + fadeOut(motion.fastEffectsSpec()))
        },
        modifier = Modifier.padding(start = DetailIndent, top = 2.dp, bottom = 2.dp),
    ) { current ->
        val tint = if (current.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(current.icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = tint)
            Text(
                text = stringResource(current.label),
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
        }
    }
}

@Composable
private fun MessageDetails(message: Message, sim: SimInfo?, showSim: Boolean) {
    val context = LocalContext.current
    val parts = buildList {
        add(formatMessageTimestamp(context, message.date))
        if (showSim) {
            add(
                sim?.let { stringResource(R.string.chat_sim_detail, it.slotIndex + 1, it.label) }
                    ?: stringResource(R.string.chat_sim_unknown),
            )
        }
    }
    Text(
        text = parts.joinToString(SEPARATOR),
        modifier = Modifier.padding(start = DetailIndent, top = 2.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun rememberEntranceProgress(message: Message): State<Float> {
    val motion = MaterialTheme.motionScheme
    val progress = remember(message.id) {
        val age = System.currentTimeMillis() - message.date
        Animatable(if (age in 0..FRESH_MESSAGE_MILLIS) 0f else 1f)
    }
    LaunchedEffect(progress) {
        if (progress.value < 1f) progress.animateTo(1f, motion.slowSpatialSpec())
    }
    return progress.asState()
}

@Composable
private fun senderLabel(row: ChatItem.MessageRow): String = when {
    row.message.isOutgoing -> stringResource(R.string.chat_sender_me)
    row.senderName != null -> row.senderName
    else -> stringResource(R.string.chat_sender_unknown)
}

@Composable
private fun simShortLabel(sim: SimInfo?): String =
    sim?.let { stringResource(R.string.chat_sim, it.slotIndex + 1) } ?: stringResource(R.string.chat_sim_unknown)

private enum class OutgoingStatus(
    val icon: ImageVector,
    @param:StringRes val label: Int,
    val isError: Boolean = false,
) {
    Sending(Icons.Rounded.Schedule, R.string.chat_status_sending),
    Sent(Icons.Rounded.Done, R.string.chat_status_sent),
    Delivered(Icons.Rounded.DoneAll, R.string.chat_status_delivered),
    NotDelivered(Icons.Rounded.ErrorOutline, R.string.chat_status_not_delivered, isError = true),
    Failed(Icons.Rounded.ErrorOutline, R.string.chat_status_failed, isError = true),
    Draft(Icons.Rounded.Edit, R.string.chat_status_draft),
}

private fun outgoingStatusOf(message: Message): OutgoingStatus? = when {
    !message.isOutgoing -> null
    message.type == MessageType.FAILED -> OutgoingStatus.Failed
    message.type == MessageType.QUEUED || message.type == MessageType.OUTBOX -> OutgoingStatus.Sending
    message.type == MessageType.DRAFT -> OutgoingStatus.Draft
    message.status == DeliveryStatus.COMPLETE -> OutgoingStatus.Delivered
    message.status == DeliveryStatus.FAILED -> OutgoingStatus.NotDelivered
    else -> OutgoingStatus.Sent
}

private const val SEPARATOR = " · "
private const val FRESH_MESSAGE_MILLIS = 2_000L
private const val HIGHLIGHT_MILLIS = 1_600L
private const val HIGHLIGHT_IN_MILLIS = 250
private const val HIGHLIGHT_OUT_MILLIS = 900
private val AccentWidth = 3.dp
private val EntranceShift = 24.dp
private val DetailIndent = 23.dp
