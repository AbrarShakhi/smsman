package com.abrarshakhi.smsman.model

data class MessageHit(
    val message: Message,
    val senderLabel: String,
)

data class SearchResults(
    val people: List<ContactSuggestion> = emptyList(),
    val messages: List<MessageHit> = emptyList(),
) {
    val isEmpty: Boolean get() = people.isEmpty() && messages.isEmpty()
}
