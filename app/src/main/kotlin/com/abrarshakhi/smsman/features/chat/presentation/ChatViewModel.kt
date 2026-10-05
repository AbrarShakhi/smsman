package com.abrarshakhi.smsman.features.chat.presentation

import android.telephony.SubscriptionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.model.Conversation
import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.model.SimInfo
import com.abrarshakhi.smsman.core.notification.MessageNotifier
import com.abrarshakhi.smsman.core.repository.MessageMetadataRepository
import com.abrarshakhi.smsman.core.repository.MessageRepository
import com.abrarshakhi.smsman.core.repository.ThreadTitle
import com.abrarshakhi.smsman.core.repository.ThreadTitleResolver
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.SegmentInfo
import com.abrarshakhi.smsman.core.telephony.SmsSender
import com.abrarshakhi.smsman.core.telephony.segmentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

sealed interface ChatItem {
    data class DayDivider(val timestamp: Long) : ChatItem
    data class MessageRow(
        val message: Message,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean,
    ) : ChatItem
}

data class ChatState(
    val title: ThreadTitle = ThreadTitle.Unknown,
    val avatarColorIndex: Int = 0,
    val items: List<ChatItem> = emptyList(),
    val sims: List<SimInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val recipient: String? = null,
    val selectedIds: Set<Long> = emptySet(),
    val isFavorite: Boolean = false,
    val draft: String = "",
    val selectedSubscriptionId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID,
    val sendError: String? = null,
    val isClosed: Boolean = false,
) {
    val isMultiSim: Boolean get() = sims.size > 1

    val segments: SegmentInfo get() = segmentInfo(draft)

    val canSend: Boolean get() = draft.isNotBlank() && !recipient.isNullOrBlank()

    val selectedSim: SimInfo?
        get() = sims.firstOrNull { it.subscriptionId == selectedSubscriptionId }

    val inSelectionMode: Boolean get() = selectedIds.isNotEmpty()

    fun selectedMessages(): List<Message> = items
        .filterIsInstance<ChatItem.MessageRow>()
        .map { it.message }
        .filter { it.id in selectedIds }

    val selectionPinAction: Boolean get() = selectedMessages().any { !it.isPinned }
}

class ChatViewModel(
    private val repository: MessageRepository,
    private val metadata: MessageMetadataRepository,
    private val titleResolver: ThreadTitleResolver,
    private val sender: SmsSender,
    private val messages: MessagesDataSource,
    private val notifier: MessageNotifier,
    private val threadId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChatState(avatarColorIndex = (threadId % Conversation.AVATAR_COLOR_SLOTS).toInt()),
    )
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val title = titleResolver.resolve(threadId)
            _state.value = _state.value.copy(title = title)
        }

        viewModelScope.launch {
            metadata.observeFavoriteThreadIds().collect { favorites ->
                _state.value = _state.value.copy(isFavorite = threadId in favorites)
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) { messages.markThreadRead(threadId) }
            notifier.cancel(threadId)
        }

        viewModelScope.launch {
            repository.observeThread(threadId)
                .catch { throwable ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "Could not read this conversation",
                    )
                }
                .collect { thread ->
                    val previous = _state.value
                    val selected = thread.sims
                        .map { it.subscriptionId }
                        .firstOrNull { it == previous.selectedSubscriptionId }
                        ?: thread.messages.lastOrNull { it.subscriptionId in thread.sims.map(SimInfo::subscriptionId) }
                            ?.subscriptionId
                        ?: thread.sims.firstOrNull()?.subscriptionId
                        ?: SubscriptionManager.INVALID_SUBSCRIPTION_ID

                    val present = thread.messages.mapTo(mutableSetOf()) { it.id }
                    _state.value = previous.copy(
                        selectedIds = previous.selectedIds intersect present,
                        items = buildItems(thread.messages),
                        sims = thread.sims,
                        isLoading = false,
                        error = null,
                        recipient = thread.messages.lastOrNull { !it.address.isNullOrBlank() }?.address,
                        selectedSubscriptionId = selected,
                    )
                }
        }
    }

    private fun buildItems(messages: List<Message>): List<ChatItem> {
        if (messages.isEmpty()) return emptyList()

        val rows = messages.mapIndexed { index, message ->
            val previous = messages.getOrNull(index - 1)
            val next = messages.getOrNull(index + 1)
            ChatItem.MessageRow(
                message = message,
                isFirstInGroup = previous == null || !groups(previous, message),
                isLastInGroup = next == null || !groups(message, next),
            )
        }

        return buildList {
            for (index in rows.indices.reversed()) {
                val row = rows[index]
                add(row)
                val older = rows.getOrNull(index - 1)
                if (older == null || !sameDay(older.message.date, row.message.date)) {
                    add(ChatItem.DayDivider(row.message.date))
                }
            }
        }
    }

    fun onTogglePin(message: Message) {
        viewModelScope.launch {
            if (message.isPinned) {
                metadata.unpin(message.id)
            } else {
                metadata.pin(
                    messageId = message.id,
                    threadId = message.threadId,
                    date = message.date,
                    type = message.type.ordinal,
                    body = message.body,
                )
            }
        }
    }

    fun onToggleSelection(message: Message) {
        val current = _state.value.selectedIds
        _state.value = _state.value.copy(
            selectedIds = if (message.id in current) current - message.id else current + message.id,
        )
    }

    fun onClearSelection() {
        _state.value = _state.value.copy(selectedIds = emptySet())
    }

    fun onPinSelected() {
        val selected = _state.value.selectedMessages()
        val shouldPin = _state.value.selectionPinAction
        viewModelScope.launch {
            selected.forEach { message ->
                if (shouldPin && !message.isPinned) {
                    metadata.pin(
                        messageId = message.id,
                        threadId = message.threadId,
                        date = message.date,
                        type = message.type.ordinal,
                        body = message.body,
                    )
                } else if (!shouldPin && message.isPinned) {
                    metadata.unpin(message.id)
                }
            }
            onClearSelection()
        }
    }

    fun onDeleteSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { messages.deleteMessages(ids) }
            metadata.prunePins(ids.toList())
            onClearSelection()
        }
    }

    fun onToggleFavorite() {
        val next = !_state.value.isFavorite
        viewModelScope.launch { metadata.setFavorite(threadId, next) }
    }

    fun onMarkUnread() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { messages.markThreadUnread(threadId) }
            _state.value = _state.value.copy(isClosed = true)
        }
    }

    fun onDeleteConversation() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { messages.deleteThread(threadId) }
            metadata.forgetThread(threadId)
            notifier.cancel(threadId)
            _state.value = _state.value.copy(isClosed = true)
        }
    }

    fun onDraftChange(text: String) {
        _state.value = _state.value.copy(draft = text, sendError = null)
    }

    fun onSimSelected(subscriptionId: Int) {
        _state.value = _state.value.copy(selectedSubscriptionId = subscriptionId)
    }

    fun onSend() {
        val current = _state.value
        val recipient = current.recipient
        if (!current.canSend || recipient == null) return

        _state.value = current.copy(draft = "", sendError = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                sender.send(recipient, current.draft, current.selectedSubscriptionId)
            }
            result.onFailure { throwable ->
                _state.value = _state.value.copy(
                    draft = current.draft,
                    sendError = throwable.message ?: "Could not send",
                )
            }
        }
    }

    private fun groups(earlier: Message, later: Message): Boolean =
        earlier.isOutgoing == later.isOutgoing &&
            sameDay(earlier.date, later.date) &&
            later.date - earlier.date < GROUPING_WINDOW_MS

    private fun sameDay(a: Long, b: Long): Boolean {
        val first = Calendar.getInstance().apply { timeInMillis = a }
        val second = Calendar.getInstance().apply { timeInMillis = b }
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    private companion object {
        const val GROUPING_WINDOW_MS = 5 * 60 * 1000L
    }
}
