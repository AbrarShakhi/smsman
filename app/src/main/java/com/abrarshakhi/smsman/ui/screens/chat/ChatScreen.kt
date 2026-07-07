package com.abrarshakhi.smsman.ui.screens.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.ui.screens.chat.components.ChatTopBar
import com.abrarshakhi.smsman.ui.screens.chat.components.Messages
import com.abrarshakhi.smsman.ui.screens.chat.components.UserInput
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    threadId: Long,
    onBack: () -> Unit,
    onSearchInThread: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    LaunchedEffect(threadId) { viewModel.bind(threadId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val layoutDirection = LocalLayoutDirection.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChatEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is ChatEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
                ChatEffect.MessageSent -> Unit
                is ChatEffect.CopyToClipboard -> {
                    clipboard.setText(AnnotatedString(effect.text))
                    snackbarHostState.showSnackbar("Copied")
                }
                is ChatEffect.LaunchDialer -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${effect.phoneNumber}"))
                    context.startActivity(intent)
                }
                ChatEffect.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            Box {
                ChatTopBar(
                    contact = state.contact,
                    conversation = state.conversation,
                    onBack = onBack,
                    onSearchInThread = onSearchInThread,
                    onCall = viewModel::onCallClick,
                    onOverflow = { viewModel.onIntent(ChatIntent.OpenOverflowMenu) },
                )
                DropdownMenu(
                    expanded = state.showOverflowMenu,
                    onDismissRequest = { viewModel.onIntent(ChatIntent.CloseOverflowMenu) },
                ) {
                    val isMuted = state.conversation?.muted == true
                    DropdownMenuItem(
                        text = { Text(if (isMuted) "Unmute" else "Mute") },
                        leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) },
                        onClick = { viewModel.onIntent(ChatIntent.SetMute(!isMuted)) },
                    )
                    DropdownMenuItem(
                        text = { Text("Block sender") },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                        onClick = { viewModel.onIntent(ChatIntent.BlockSender) },
                    )
                }
            }
        },
        bottomBar = {
            UserInput(
                text = state.draft,
                onTextChange = { viewModel.onIntent(ChatIntent.DraftChanged(it)) },
                onSend = { viewModel.onIntent(ChatIntent.Send) },
                onScheduleClick = { viewModel.onIntent(ChatIntent.OpenScheduleDialog) },
                enabled = !state.isSending,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val safePadding = PaddingValues(
            start = padding.calculateStartPadding(layoutDirection),
            end = padding.calculateEndPadding(layoutDirection),
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
        )
        Box(modifier = Modifier.fillMaxSize().padding(safePadding)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                else -> Messages(
                    items = state.items,
                    modifier = Modifier.fillMaxSize(),
                    onMessageLongPress = { viewModel.onIntent(ChatIntent.ShowMessageActions(it)) },
                )
            }
        }
    }

    val actionsId = state.actionsForMessageId
    if (actionsId != null) {
        MessageActionsSheet(
            messageId = actionsId,
            state = state,
            onCopy = { viewModel.onIntent(ChatIntent.RequestCopy(actionsId)) },
            onDelete = { viewModel.onIntent(ChatIntent.DeleteMessage(actionsId)) },
            onDismiss = { viewModel.onIntent(ChatIntent.HideMessageActions) },
        )
    }

    if (state.showScheduleDialog) {
        ScheduleSendDialog(
            onSchedule = { millis -> viewModel.onIntent(ChatIntent.ScheduleSend(millis)) },
            onDismiss = { viewModel.onIntent(ChatIntent.CloseScheduleDialog) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSendDialog(
    onSchedule: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        val now = LocalTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = now.hour,
            initialMinute = now.minute,
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val localDate = Instant.ofEpochMilli(selectedDate)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        val scheduledAt = localDate
                            .atTime(timePickerState.hour, timePickerState.minute)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        onSchedule(scheduledAt)
                    },
                ) { Text("Schedule") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Back") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageActionsSheet(
    messageId: Long,
    state: ChatState,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val message = state.items.asSequence()
        .filterIsInstance<ChatItem.Msg>()
        .firstOrNull { it.message.id == messageId }?.message
    val timestampText = remember(message?.date) {
        message?.date?.let {
            DateTimeFormatter.ofPattern("EEE d MMM, HH:mm:ss")
                .format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
        }.orEmpty()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            ListItem(
                modifier = Modifier.clickable(onClick = onCopy),
                headlineContent = { Text("Copy") },
                leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            )
            ListItem(
                modifier = Modifier.clickable(onClick = onDelete),
                headlineContent = { Text("Delete") },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
            )
            if (timestampText.isNotEmpty()) {
                ListItem(
                    headlineContent = { Text("Details") },
                    supportingContent = { Text(timestampText) },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                )
            }
        }
    }
}
