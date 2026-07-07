package com.abrarshakhi.smsman.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.SendRequest
import com.abrarshakhi.smsman.domain.model.SendResult
import com.abrarshakhi.smsman.domain.repository.ContactsRepository
import com.abrarshakhi.smsman.domain.repository.ConversationRepository
import com.abrarshakhi.smsman.domain.repository.MessageRepository
import com.abrarshakhi.smsman.domain.repository.ScheduleRequest
import com.abrarshakhi.smsman.domain.repository.ScheduledMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val contactsRepository: ContactsRepository,
    private val scheduledMessageRepository: ScheduledMessageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ChatEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ChatEffect> = _effects.asSharedFlow()

    private val threadIdFlow = MutableStateFlow<Long?>(null)
    private val draftFlow = MutableStateFlow("")

    init {
        observeData()
        observeDraftSave()
    }

    fun bind(threadId: Long) {
        if (threadIdFlow.value == threadId) return
        threadIdFlow.value = threadId
        viewModelScope.launch {
            // Hydrate draft + mark thread read whenever a new thread is bound.
            val conv = conversationRepository.byId(threadId)
            val initialDraft = conv?.draft.orEmpty()
            _state.update { it.copy(threadId = threadId, draft = initialDraft, isLoading = true) }
            draftFlow.value = initialDraft
            messageRepository.markThreadRead(threadId)
        }
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.DraftChanged -> {
                _state.update { it.copy(draft = intent.text) }
                draftFlow.value = intent.text
            }
            ChatIntent.Send -> send()
            is ChatIntent.ScheduleSend -> scheduleSend(intent.scheduledAtMillis)
            ChatIntent.OpenScheduleDialog -> _state.update { it.copy(showScheduleDialog = true) }
            ChatIntent.CloseScheduleDialog -> _state.update { it.copy(showScheduleDialog = false) }
            ChatIntent.OpenOverflowMenu -> _state.update { it.copy(showOverflowMenu = true) }
            ChatIntent.CloseOverflowMenu -> _state.update { it.copy(showOverflowMenu = false) }
            ChatIntent.BlockSender -> blockSender()
            is ChatIntent.SetMute -> {
                val tid = threadIdFlow.value ?: return
                viewModelScope.launch {
                    conversationRepository.setMuted(tid, intent.muted)
                    _state.update { it.copy(showOverflowMenu = false) }
                    val label = if (intent.muted) "Muted" else "Unmuted"
                    _effects.emit(ChatEffect.ShowMessage(label))
                }
            }
            ChatIntent.MarkThreadRead -> {
                val tid = threadIdFlow.value ?: return
                viewModelScope.launch { messageRepository.markThreadRead(tid) }
            }
            is ChatIntent.DeleteMessage -> {
                viewModelScope.launch {
                    messageRepository.delete(setOf(intent.messageId))
                    _state.update { it.copy(actionsForMessageId = null) }
                }
            }
            is ChatIntent.ShowMessageActions -> {
                _state.update { it.copy(actionsForMessageId = intent.messageId) }
            }
            ChatIntent.HideMessageActions -> {
                _state.update { it.copy(actionsForMessageId = null) }
            }
            is ChatIntent.RequestCopy -> {
                viewModelScope.launch {
                    val msg = messageRepository.byId(intent.messageId)
                    val body = msg?.body.orEmpty()
                    if (body.isNotEmpty()) _effects.emit(ChatEffect.CopyToClipboard(body))
                    _state.update { it.copy(actionsForMessageId = null) }
                }
            }
        }
    }

    fun onCallClick() {
        val addr = _state.value.conversation?.recipientAddresses?.firstOrNull() ?: return
        viewModelScope.launch { _effects.emit(ChatEffect.LaunchDialer(addr)) }
    }

    private fun observeData() {
        viewModelScope.launch {
            threadIdFlow
                .filter { it != null && it >= 0L }
                .distinctUntilChanged()
                .flatMapLatest { threadId ->
                    combine(
                        conversationRepository.observeById(threadId!!),
                        messageRepository.observeByThread(threadId),
                    ) { conv, msgs -> Triple(conv, msgs, threadId) }
                }
                .collect { (conv, msgs, threadId) ->
                    val firstAddr = conv?.recipientAddresses?.firstOrNull()
                    val contact = firstAddr?.let { contactsRepository.resolve(it) }
                    val items = groupMessages(msgs)
                    _state.update {
                        it.copy(
                            threadId = threadId,
                            conversation = conv,
                            contact = contact,
                            items = items,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeDraftSave() {
        viewModelScope.launch {
            // Drop the initial value emitted by MutableStateFlow so we don't overwrite the DB
            // with the empty default on startup.
            draftFlow
                .drop(1)
                .debounce(DRAFT_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { text ->
                    val tid = threadIdFlow.value ?: return@collect
                    messageRepository.saveDraft(tid, text.takeIf { it.isNotBlank() })
                }
        }
    }

    private fun send() {
        val tid = threadIdFlow.value ?: return
        val body = _state.value.draft.trim()
        if (body.isEmpty() || _state.value.isSending) return
        val conv = _state.value.conversation ?: return
        val recipient = conv.recipientAddresses.firstOrNull() ?: return
        val subId = conv.subscriptionId ?: -1

        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val result = messageRepository.send(
                SendRequest(
                    threadId = tid,
                    recipients = listOf(recipient),
                    body = body,
                    subscriptionId = subId,
                ),
            )
            _state.update { it.copy(isSending = false) }
            when (result) {
                is SendResult.Queued -> {
                    _state.update { it.copy(draft = "") }
                    draftFlow.value = ""
                    _effects.emit(ChatEffect.MessageSent)
                }
                is SendResult.Failed -> _effects.emit(ChatEffect.ShowError(result.reason))
            }
        }
    }

    private fun blockSender() {
        val tid = threadIdFlow.value ?: return
        viewModelScope.launch {
            conversationRepository.setBlocked(tid, true)
            _state.update { it.copy(showOverflowMenu = false) }
            _effects.emit(ChatEffect.NavigateBack)
        }
    }

    private fun scheduleSend(scheduledAtMillis: Long) {
        val tid = threadIdFlow.value ?: return
        val body = _state.value.draft.trim()
        if (body.isEmpty()) return
        val conv = _state.value.conversation ?: return
        val recipient = conv.recipientAddresses.firstOrNull() ?: return
        val subId = conv.subscriptionId ?: -1

        viewModelScope.launch {
            scheduledMessageRepository.schedule(
                ScheduleRequest(
                    threadId = tid,
                    recipients = listOf(recipient),
                    body = body,
                    scheduledAt = scheduledAtMillis,
                    subscriptionId = subId,
                ),
            )
            _state.update { it.copy(draft = "", showScheduleDialog = false) }
            draftFlow.value = ""
            _effects.emit(ChatEffect.ShowMessage("Message scheduled"))
        }
    }

    private companion object {
        const val DRAFT_DEBOUNCE_MS = 500L
    }
}
