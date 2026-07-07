package com.abrarshakhi.smsman.ui.screens.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.BlockedNumber
import com.abrarshakhi.smsman.domain.repository.BlockedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlockedNumbersState(
    val blockedNumbers: List<BlockedNumber> = emptyList(),
)

@HiltViewModel
class BlockedNumbersViewModel @Inject constructor(
    private val repository: BlockedNumberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BlockedNumbersState())
    val state: StateFlow<BlockedNumbersState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeBlocked().collect { list ->
                _state.update { it.copy(blockedNumbers = list) }
            }
        }
    }

    fun block(phoneNumber: String, reason: String?) = viewModelScope.launch {
        repository.block(phoneNumber, reason)
    }

    fun unblock(phoneNumber: String) = viewModelScope.launch {
        repository.unblock(phoneNumber)
    }
}
