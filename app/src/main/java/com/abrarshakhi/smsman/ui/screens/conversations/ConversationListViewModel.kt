package com.abrarshakhi.smsman.ui.screens.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.ConversationFilter
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import com.abrarshakhi.smsman.domain.repository.ConversationRepository
import com.abrarshakhi.smsman.framework.SmsRoleProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val contactsRepository: ContactsRepository,
    private val smsRoleProvider: SmsRoleProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListState())
    val state: StateFlow<ConversationListState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ConversationListEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ConversationListEffect> = _effects.asSharedFlow()

    private val filterFlow = MutableStateFlow(ConversationFilter.All)

    init {
        observeConversations()
        refreshDefaultAppState()
    }

    fun onIntent(intent: ConversationListIntent) {
        when (intent) {
            is ConversationListIntent.SetFilter -> {
                filterFlow.value = intent.filter
                _state.update { it.copy(filter = intent.filter, isLoading = true) }
            }
            is ConversationListIntent.Click -> handleClick(intent.threadId)
            is ConversationListIntent.LongClick -> toggleSelection(intent.threadId)
            ConversationListIntent.ClearSelection -> _state.update { it.copy(selectedIds = emptySet()) }
            ConversationListIntent.DeleteSelected -> deleteSelected()
            ConversationListIntent.ArchiveSelected -> archiveSelected()
            ConversationListIntent.MarkSelectedRead -> markSelectedRead()
            ConversationListIntent.PinSelected -> pinSelected()
            ConversationListIntent.MuteSelected -> muteSelected()
            ConversationListIntent.RequestDefaultApp -> {
                viewModelScope.launch {
                    _effects.emit(ConversationListEffect.RequestDefaultSmsApp(smsRoleProvider.createRoleRequestIntent()))
                }
            }
        }
    }

    fun onResume() {
        refreshDefaultAppState()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            filterFlow
                .flatMapLatest { filter -> conversationRepository.observe(filter) }
                .map { convs ->
                    val firstAddrs = convs.mapNotNull { it.recipientAddresses.firstOrNull() }.toSet()
                    val contacts = contactsRepository.resolveAll(firstAddrs)
                    convs.map { conv ->
                        val key = conv.recipientAddresses.firstOrNull().orEmpty()
                        ConversationRow(conversation = conv, contact = contacts[key])
                    }
                }
                .onEach { rows ->
                    _state.update { it.copy(rows = rows, isLoading = false) }
                }
                .collect()
        }
    }

    private fun refreshDefaultAppState() {
        _state.update { it.copy(isDefaultSmsApp = smsRoleProvider.isDefaultSmsApp()) }
    }

    private fun handleClick(threadId: Long) {
        val selected = _state.value.selectedIds
        if (selected.isNotEmpty()) {
            toggleSelection(threadId)
            return
        }
        viewModelScope.launch { _effects.emit(ConversationListEffect.OpenChat(threadId)) }
    }

    private fun toggleSelection(threadId: Long) {
        _state.update { current ->
            val updated = current.selectedIds.toMutableSet().also {
                if (!it.add(threadId)) it.remove(threadId)
            }
            current.copy(selectedIds = updated)
        }
    }

    private fun deleteSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            conversationRepository.delete(ids)
            _state.update { it.copy(selectedIds = emptySet()) }
            _effects.emit(ConversationListEffect.ShowMessage("Deleted ${ids.size} conversation${if (ids.size == 1) "" else "s"}"))
        }
    }

    private fun archiveSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { conversationRepository.setArchived(it, true) }
            _state.update { it.copy(selectedIds = emptySet()) }
            _effects.emit(ConversationListEffect.ShowMessage("Archived ${ids.size}"))
        }
    }

    private fun markSelectedRead() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { conversationRepository.markRead(it) }
            _state.update { it.copy(selectedIds = emptySet()) }
        }
    }

    private fun pinSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { conversationRepository.setPinned(it, true) }
            _state.update { it.copy(selectedIds = emptySet()) }
            _effects.emit(ConversationListEffect.ShowMessage("Pinned ${ids.size}"))
        }
    }

    private fun muteSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { conversationRepository.setMuted(it, true) }
            _state.update { it.copy(selectedIds = emptySet()) }
            _effects.emit(ConversationListEffect.ShowMessage("Muted ${ids.size}"))
        }
    }
}

// Local terminal collector — keeps the upstream chain alive without resetting it.
private suspend inline fun <T> kotlinx.coroutines.flow.Flow<T>.collect() = collect {}
