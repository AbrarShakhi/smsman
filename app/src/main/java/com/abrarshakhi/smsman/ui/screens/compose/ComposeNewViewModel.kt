package com.abrarshakhi.smsman.ui.screens.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.Contact
import com.abrarshakhi.smsman.domain.model.SendRequest
import com.abrarshakhi.smsman.domain.model.SendResult
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComposeNewState(
    val query: String = "",
    val suggestions: List<Contact> = emptyList(),
    val recipients: List<Contact> = emptyList(),
    val body: String = "",
    val isSending: Boolean = false,
)

sealed interface ComposeNewIntent {
    data class QueryChanged(val text: String) : ComposeNewIntent
    data class AddRecipient(val contact: Contact) : ComposeNewIntent
    data class AddRawNumber(val number: String) : ComposeNewIntent
    data class RemoveRecipient(val contact: Contact) : ComposeNewIntent
    data class BodyChanged(val text: String) : ComposeNewIntent
    data object Send : ComposeNewIntent
}

sealed interface ComposeNewEffect {
    data class NavigateToChat(val threadId: Long) : ComposeNewEffect
    data class ShowError(val message: String) : ComposeNewEffect
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ComposeNewViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val messageRepository: MessageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ComposeNewState())
    val state: StateFlow<ComposeNewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ComposeNewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ComposeNewEffect> = _effects.asSharedFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(200)
                .distinctUntilChanged()
                .collect { q ->
                    val suggestions = if (q.length >= 2) contactsRepository.search(q) else emptyList()
                    _state.update { it.copy(suggestions = suggestions) }
                }
        }
    }

    fun bind(initialRecipient: String?) {
        if (initialRecipient == null || _state.value.recipients.isNotEmpty()) return
        viewModelScope.launch {
            val contact = contactsRepository.resolve(initialRecipient)
                ?: Contact(phoneNumberE164 = initialRecipient, displayName = null, photoUri = null, lookupKey = null)
            addRecipient(contact)
        }
    }

    fun onIntent(intent: ComposeNewIntent) {
        when (intent) {
            is ComposeNewIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.text) }
                queryFlow.value = intent.text
            }
            is ComposeNewIntent.AddRecipient -> addRecipient(intent.contact)
            is ComposeNewIntent.AddRawNumber -> {
                val n = intent.number.trim()
                if (n.isNotEmpty()) {
                    addRecipient(Contact(phoneNumberE164 = n, displayName = null, photoUri = null, lookupKey = null))
                }
            }
            is ComposeNewIntent.RemoveRecipient -> {
                _state.update { it.copy(recipients = it.recipients - intent.contact) }
            }
            is ComposeNewIntent.BodyChanged -> _state.update { it.copy(body = intent.text) }
            ComposeNewIntent.Send -> send()
        }
    }

    private fun addRecipient(contact: Contact) {
        val current = _state.value
        if (current.recipients.any { it.phoneNumberE164 == contact.phoneNumberE164 }) return
        _state.update { it.copy(recipients = it.recipients + contact, query = "", suggestions = emptyList()) }
        queryFlow.value = ""
    }

    private fun send() {
        val s = _state.value
        if (s.isSending || s.recipients.isEmpty() || s.body.isBlank()) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val result = messageRepository.send(
                SendRequest(
                    threadId = null,
                    recipients = s.recipients.map { it.phoneNumberE164 },
                    body = s.body.trim(),
                    subscriptionId = -1,
                ),
            )
            _state.update { it.copy(isSending = false) }
            when (result) {
                is SendResult.Queued -> _effects.emit(ComposeNewEffect.NavigateToChat(result.threadId))
                is SendResult.Failed -> _effects.emit(ComposeNewEffect.ShowError(result.reason))
            }
        }
    }
}
