package com.abrarshakhi.smsman.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.model.SimInfo
import com.abrarshakhi.smsman.core.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

/** A rendered row: either a day divider or a message with its grouping decided. */
sealed interface ChatItem {
    data class DayDivider(val timestamp: Long) : ChatItem
    data class MessageRow(
        val message: Message,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean,
    ) : ChatItem
}

data class ChatState(
    val items: List<ChatItem> = emptyList(),
    val sims: List<SimInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    /** Only worth showing a SIM indicator when more than one SIM is actually present. */
    val isMultiSim: Boolean get() = sims.size > 1
}

class ChatViewModel(
    private val repository: MessageRepository,
    private val threadId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeThread(threadId)
                .catch { throwable ->
                    _state.value = ChatState(
                        isLoading = false,
                        error = throwable.message ?: "Could not read this conversation",
                    )
                }
                .collect { thread ->
                    _state.value = ChatState(
                        items = buildItems(thread.messages),
                        sims = thread.sims,
                        isLoading = false,
                    )
                }
        }
    }

    /**
     * Input is oldest-first. Output is **newest-first**, because the list renders with
     * `reverseLayout = true` so that it opens pinned to the latest message.
     *
     * A divider is emitted after the last message of a day (in newest-first order), which places it
     * visually above the first message of that day.
     */
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

    /** Same sender and close in time, matching how Messages visually merges a run of bubbles. */
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
