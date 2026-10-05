package com.abrarshakhi.smsman.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.data.repository.ConversationRepository
import com.abrarshakhi.smsman.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ConversationsState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ConversationsViewModel(
    private val repository: ConversationRepository,
    private val favoritesOnly: Boolean,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationsState())
    val state: StateFlow<ConversationsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeConversations(favoritesOnly).catch { throwable ->
                    _state.value = ConversationsState(
                        isLoading = false,
                        error = throwable.message ?: "Could not read messages",
                    )
                }.collect { conversations ->
                    _state.value =
                        ConversationsState(conversations = conversations, isLoading = false)
                }
        }
    }
}
