package com.abrarshakhi.smsman.features.pinned.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.repository.PinnedMessage
import com.abrarshakhi.smsman.core.repository.PinnedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class PinnedState(
    val pinned: List<PinnedMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class PinnedViewModel(repository: PinnedRepository) : ViewModel() {

    private val _state = MutableStateFlow(PinnedState())
    val state: StateFlow<PinnedState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePinned()
                .catch { throwable ->
                    _state.value = PinnedState(
                        isLoading = false,
                        error = throwable.message ?: "Could not read pinned messages",
                    )
                }
                .collect { pinned ->
                    _state.value = PinnedState(pinned = pinned, isLoading = false)
                }
        }
    }
}
