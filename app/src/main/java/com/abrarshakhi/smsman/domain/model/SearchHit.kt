package com.abrarshakhi.smsman.domain.model

data class SearchHit(
    val message: Message,
    val conversation: Conversation,
    val contact: Contact?,
) {
    /** Computed: the matched body with the query window the UI can highlight. */
    val snippet: String get() = message.body.orEmpty()
}
