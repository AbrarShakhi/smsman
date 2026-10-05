package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.data.provider.ConversationsDataSource
import com.abrarshakhi.smsman.data.provider.PhoneNumbers
import com.abrarshakhi.smsman.data.provider.TelephonyChangeObserver
import com.abrarshakhi.smsman.model.Conversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ConversationRepository(
    private val conversations: ConversationsDataSource,
    private val contacts: ContactsDataSource,
    private val metadata: MessageMetadataRepository,
    private val changes: TelephonyChangeObserver,
) {
    fun observeConversations(favoritesOnly: Boolean): Flow<List<Conversation>> = combine(
        changes.changes().map { load() },
        metadata.observeFavoriteThreadIds(),
    ) { loaded, favoriteIds ->
        loaded.map { it.copy(isFavorite = it.threadId in favoriteIds) }
            .filter { !favoritesOnly || it.isFavorite }
    }.flowOn(Dispatchers.IO)

    private fun load(): List<Conversation> {
        val threads = conversations.loadThreads()
        if (threads.isEmpty()) return emptyList()

        val canonical = conversations.loadCanonicalAddresses()
        val unread = conversations.loadUnreadCounts()

        return threads.map { thread ->
            val addresses = PhoneNumbers.distinct(thread.recipientIds.mapNotNull(canonical::get))
            val resolved = addresses.map { address -> address to contacts.lookup(address) }

            Conversation(
                threadId = thread.threadId,
                addresses = addresses,
                displayName = resolved.joinToString(", ") { (address, contact) ->
                    contact?.displayName ?: address
                }.ifBlank { "(unknown)" },
                snippet = thread.snippet,
                date = thread.date,
                messageCount = thread.messageCount,
                unreadCount = unread[thread.threadId] ?: 0,
                isFavorite = false,
                hasAttachment = thread.hasAttachment,
                photoUri = resolved.singleOrNull()?.second?.photoUri,
            )
        }
    }
}
