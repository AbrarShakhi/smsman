package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.data.provider.MessagesDataSource
import com.abrarshakhi.smsman.model.MessageHit
import com.abrarshakhi.smsman.model.SearchResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(
    private val messages: MessagesDataSource,
    private val contacts: ContactsDataSource,
) {

    suspend fun search(query: String): SearchResults = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext SearchResults()

        val hits = messages.searchMessages(query).map { message ->
            val address = message.address
            MessageHit(
                message = message,
                senderLabel = address?.let { contacts.lookup(it)?.displayName ?: it }
                    ?: "(unknown)",
            )
        }
        SearchResults(people = contacts.search(query), messages = hits)
    }
}
