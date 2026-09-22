package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.model.ContactSuggestion
import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

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

class SearchRepository(
    private val messages: MessagesDataSource,
    private val contacts: ContactsDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun search(query: String): SearchResults = withContext(ioDispatcher) {
        if (query.isBlank()) return@withContext SearchResults()

        val hits = messages.searchMessages(query).map { message ->
            val address = message.address
            MessageHit(
                message = message,
                senderLabel = address?.let { contacts.lookup(it)?.displayName ?: it } ?: "(unknown)",
            )
        }
        SearchResults(people = contacts.search(query), messages = hits)
    }
}
