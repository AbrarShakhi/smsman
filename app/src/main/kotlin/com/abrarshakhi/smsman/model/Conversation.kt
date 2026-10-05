package com.abrarshakhi.smsman.model

data class Conversation(
    val threadId: Long,
    val addresses: List<String>,
    val displayName: String,
    val snippet: String,
    val date: Long,
    val messageCount: Int,
    val unreadCount: Int,
    val isFavorite: Boolean,
    val hasAttachment: Boolean,
    val photoUri: String?,
) {
    val isGroup: Boolean get() = addresses.size > 1
    val isUnread: Boolean get() = unreadCount > 0

    val avatarColorIndex: Int get() = (threadId % AVATAR_COLOR_SLOTS).toInt()

    companion object {
        const val AVATAR_COLOR_SLOTS = 8
    }
}

data class ContactInfo(
    val displayName: String,
    val photoUri: String?,
    val contactId: Long?,
)

data class ContactSuggestion(
    val name: String,
    val number: String,
    val photoUri: String?,
) {
    val label: String get() = name.ifBlank { number }
}
