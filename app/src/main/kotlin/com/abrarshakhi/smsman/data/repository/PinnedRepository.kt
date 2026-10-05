package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.data.provider.MessagesDataSource
import com.abrarshakhi.smsman.data.provider.TelephonyChangeObserver
import com.abrarshakhi.smsman.model.PinnedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class PinnedRepository(
    private val messages: MessagesDataSource,
    private val contacts: ContactsDataSource,
    private val metadata: MessageMetadataRepository,
    private val changes: TelephonyChangeObserver,
) {
    fun observePinned(): Flow<List<PinnedMessage>> =
        combine(
            metadata.observeAllPinned(),
            changes.changes(),
        ) { pins, _ ->
            if (pins.isEmpty()) return@combine emptyList()

            val requested = pins.map { it.messageId }
            val found = messages.loadMessagesByIds(requested).associateBy { it.id }

            val missing = requested.filterNot { it in found }
            if (missing.isNotEmpty()) metadata.prunePins(missing)

            pins.mapNotNull { pin ->
                val message = found[pin.messageId] ?: return@mapNotNull null
                val address = message.address
                PinnedMessage(
                    message = message,
                    senderLabel = address
                        ?.let { contacts.lookup(it)?.displayName ?: it }
                        ?: "(unknown)",
                    pinnedAt = pin.pinnedAt,
                )
            }
        }.flowOn(Dispatchers.IO)
}
