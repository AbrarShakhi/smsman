package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.model.Conversation
import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.ConversationsDataSource
import com.abrarshakhi.smsman.core.telephony.PhoneNumbers
import com.abrarshakhi.smsman.core.telephony.TelephonyChangeObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Joins the Telephony provider (source of truth for messages) with our Room metadata
 * (favourite/pin, which the provider has no columns for).
 *
 * One provider read per change signal, independent of message count: threads + canonical addresses
 * + unread counts, then contacts resolved from an in-memory cache.
 */
class ConversationRepository(
    private val conversations: ConversationsDataSource,
    private val contacts: ContactsDataSource,
    private val metadata: MessageMetadataRepository,
    private val changes: TelephonyChangeObserver,
    private val ioDispatcher: CoroutineDispatcher,
) {

    fun observeConversations(favoritesOnly: Boolean): Flow<List<Conversation>> =
        combine(
            changes.changes().map { load() },
            metadata.observeFavoriteThreadIds(),
        ) { loaded, favoriteIds ->
            loaded
                .map { it.copy(isFavorite = it.threadId in favoriteIds) }
                .filter { !favoritesOnly || it.isFavorite }
        }.flowOn(ioDispatcher)

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
                isFavorite = false, // filled in by the metadata combine above
                hasAttachment = thread.hasAttachment,
                // Only meaningful for a 1:1 thread; group threads fall back to an initial.
                photoUri = resolved.singleOrNull()?.second?.photoUri,
            )
        }
    }
}
