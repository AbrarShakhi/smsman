package com.abrarshakhi.smsman.ui.screens.scheduled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.ScheduledMessage
import com.abrarshakhi.smsman.domain.repository.ScheduledMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduledMessagesState(
    val pending: List<ScheduledMessage> = emptyList(),
)

@HiltViewModel
class ScheduledMessagesViewModel @Inject constructor(
    private val repository: ScheduledMessageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledMessagesState())
    val state: StateFlow<ScheduledMessagesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePending().collect { pending ->
                _state.update { it.copy(pending = pending) }
            }
        }
    }

    fun cancel(id: Long) = viewModelScope.launch {
        repository.cancel(id)
    }
}
