package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.TelephonyChangeObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

data class PinnedMessage(
    val message: Message,
    val senderLabel: String,
    val pinnedAt: Long,
)

class PinnedRepository(
    private val messages: MessagesDataSource,
    private val contacts: ContactsDataSource,
    private val metadata: MessageMetadataRepository,
    private val changes: TelephonyChangeObserver,
    private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * Pins are stored as bare provider ids, so each refresh re-resolves them and drops any whose
     * message has since been deleted elsewhere. Without that, the tab would show rows that no
     * longer exist and the Room table would grow indefinitely.
     */
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
        }.flowOn(ioDispatcher)
}
