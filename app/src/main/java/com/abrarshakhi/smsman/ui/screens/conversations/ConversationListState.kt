package com.abrarshakhi.smsman.ui.screens.conversations

import android.content.Intent
import com.abrarshakhi.smsman.domain.model.Contact
import com.abrarshakhi.smsman.domain.model.Conversation
import com.abrarshakhi.smsman.domain.model.ConversationFilter

data class ConversationRow(
    val conversation: Conversation,
    val contact: Contact?,
)

data class ConversationListState(
    val isLoading: Boolean = true,
    val filter: ConversationFilter = ConversationFilter.All,
    val rows: List<ConversationRow> = emptyList(),
    val isDefaultSmsApp: Boolean = true,
    val selectedIds: Set<Long> = emptySet(),
) {
    val isSelecting: Boolean get() = selectedIds.isNotEmpty()
}

sealed interface ConversationListIntent {
    data class SetFilter(val filter: ConversationFilter) : ConversationListIntent
    data class Click(val threadId: Long) : ConversationListIntent
    data class LongClick(val threadId: Long) : ConversationListIntent
    data object ClearSelection : ConversationListIntent
    data object DeleteSelected : ConversationListIntent
    data object ArchiveSelected : ConversationListIntent
    data object MarkSelectedRead : ConversationListIntent
    data object PinSelected : ConversationListIntent
    data object MuteSelected : ConversationListIntent
    data object RequestDefaultApp : ConversationListIntent
}

sealed interface ConversationListEffect {
    data class OpenChat(val threadId: Long) : ConversationListEffect
    data class ShowMessage(val text: String) : ConversationListEffect
    data class RequestDefaultSmsApp(val intent: Intent) : ConversationListEffect
}
