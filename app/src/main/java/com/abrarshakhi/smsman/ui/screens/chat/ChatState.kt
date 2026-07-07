package com.abrarshakhi.smsman.ui.screens.chat

import com.abrarshakhi.smsman.domain.model.Contact
import com.abrarshakhi.smsman.domain.model.Conversation
import com.abrarshakhi.smsman.domain.model.Message

data class ChatState(
    val threadId: Long = -1L,
    val conversation: Conversation? = null,
    val contact: Contact? = null,
    val items: List<ChatItem> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val actionsForMessageId: Long? = null,
    val showScheduleDialog: Boolean = false,
    val showOverflowMenu: Boolean = false,
)

sealed interface ChatItem {
    val key: String

    data class Msg(
        val message: Message,
        val isFirstInRun: Boolean,
        val isLastInRun: Boolean,
        val showTimeLabel: Boolean,
        val isMe: Boolean,
    ) : ChatItem {
        override val key: String get() = "m-${message.id}"
    }

    data class DayDivider(val label: String, val epochDay: Long) : ChatItem {
        override val key: String get() = "d-$epochDay"
    }
}

sealed interface ChatIntent {
    data class DraftChanged(val text: String) : ChatIntent
    data object Send : ChatIntent
    data class ScheduleSend(val scheduledAtMillis: Long) : ChatIntent
    data object OpenScheduleDialog : ChatIntent
    data object CloseScheduleDialog : ChatIntent
    data object OpenOverflowMenu : ChatIntent
    data object CloseOverflowMenu : ChatIntent
    data object BlockSender : ChatIntent
    data class SetMute(val muted: Boolean) : ChatIntent
    data class DeleteMessage(val messageId: Long) : ChatIntent
    data class ShowMessageActions(val messageId: Long) : ChatIntent
    data object HideMessageActions : ChatIntent
    data class RequestCopy(val messageId: Long) : ChatIntent
    data object MarkThreadRead : ChatIntent
}

sealed interface ChatEffect {
    data class ShowError(val message: String) : ChatEffect
    data object MessageSent : ChatEffect
    data class CopyToClipboard(val text: String, val label: String = "Message") : ChatEffect
    data class LaunchDialer(val phoneNumber: String) : ChatEffect
    data class ShowMessage(val text: String) : ChatEffect
    data object NavigateBack : ChatEffect
}
