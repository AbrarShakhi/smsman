package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.data.provider.MessagesDataSource
import com.abrarshakhi.smsman.data.provider.SimDataSource
import com.abrarshakhi.smsman.data.provider.TelephonyChangeObserver
import com.abrarshakhi.smsman.model.Message
import com.abrarshakhi.smsman.model.ThreadMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MessageRepository(
    private val messages: MessagesDataSource,
    private val sims: SimDataSource,
    private val contacts: ContactsDataSource,
    private val metadata: MessageMetadataRepository,
    private val changes: TelephonyChangeObserver,
) {

    fun observeThread(threadId: Long): Flow<ThreadMessages> =
        combine(
            changes.changes().map { loadThread(threadId) },
            metadata.observePinnedIdsInThread(threadId),
        ) { thread, pinnedIds ->
            thread.copy(messages = thread.messages.map { it.copy(isPinned = it.id in pinnedIds) })
        }.flowOn(Dispatchers.IO)

    private fun loadThread(threadId: Long): ThreadMessages {
        val loaded = messages.loadMessages(threadId)
        return ThreadMessages(
            messages = loaded,
            sims = sims.activeSims(),
            senderNames = senderNames(loaded),
        )
    }

    private fun senderNames(loaded: List<Message>): Map<String, String> =
        loaded.asSequence()
            .filterNot(Message::isOutgoing)
            .mapNotNull { message -> message.address?.takeIf(String::isNotBlank) }
            .distinct()
            .associateWith { address -> contacts.lookup(address)?.displayName ?: address }
}
