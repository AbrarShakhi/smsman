package com.abrarshakhi.smsman.features.newmessage.presentation

import android.telephony.SubscriptionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.model.ContactSuggestion
import com.abrarshakhi.smsman.core.model.SimInfo
import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.SegmentInfo
import com.abrarshakhi.smsman.core.telephony.SimDataSource
import com.abrarshakhi.smsman.core.telephony.SmsSender
import com.abrarshakhi.smsman.core.telephony.segmentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NewMessageState(
    val recipient: String = "",
    val draft: String = "",
    val suggestions: List<ContactSuggestion> = emptyList(),
    val sims: List<SimInfo> = emptyList(),
    val selectedSubscriptionId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID,
    val error: String? = null,
    val isSending: Boolean = false,
) {
    val isMultiSim: Boolean get() = sims.size > 1
    val segments: SegmentInfo get() = segmentInfo(draft)
    val canSend: Boolean get() = recipient.isNotBlank() && draft.isNotEmpty() && !isSending
    val selectedSim: SimInfo?
        get() = sims.firstOrNull { it.subscriptionId == selectedSubscriptionId }
}

class NewMessageViewModel(
    private val sender: SmsSender,
    private val contacts: ContactsDataSource,
    private val sims: SimDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(NewMessageState())
    val state: StateFlow<NewMessageState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val active = withContext(Dispatchers.IO) { sims.activeSims() }
            _state.value = _state.value.copy(
                sims = active,
                selectedSubscriptionId = active.firstOrNull()?.subscriptionId
                    ?: SubscriptionManager.INVALID_SUBSCRIPTION_ID,
            )
        }
    }

    fun onRecipientChange(text: String) {
        _state.value = _state.value.copy(recipient = text, error = null)
        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) { contacts.search(text) }
            // Drop stale results if the field moved on while the query ran.
            if (_state.value.recipient == text) {
                _state.value = _state.value.copy(suggestions = results)
            }
        }
    }

    fun onSuggestionSelected(suggestion: ContactSuggestion) {
        _state.value = _state.value.copy(
            recipient = suggestion.number,
            suggestions = emptyList(),
        )
    }

    fun onDraftChange(text: String) {
        _state.value = _state.value.copy(draft = text, error = null)
    }

    fun onSimSelected(subscriptionId: Int) {
        _state.value = _state.value.copy(selectedSubscriptionId = subscriptionId)
    }

    /** Reports the thread the message landed in so the caller can open it. */
    fun onSend(onSent: (Long) -> Unit) {
        val current = _state.value
        if (!current.canSend) return
        _state.value = current.copy(isSending = true, error = null)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                sender.send(current.recipient, current.draft, current.selectedSubscriptionId)
            }
            result
                .onSuccess { sent ->
                    _state.value = NewMessageState(
                        sims = current.sims,
                        selectedSubscriptionId = current.selectedSubscriptionId,
                    )
                    onSent(sent.threadId)
                }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        error = throwable.message ?: "Could not send",
                    )
                }
        }
    }
}
