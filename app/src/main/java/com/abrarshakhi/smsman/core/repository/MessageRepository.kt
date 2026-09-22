package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.model.Message
import com.abrarshakhi.smsman.core.model.SimInfo
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.SimDataSource
import com.abrarshakhi.smsman.core.telephony.TelephonyChangeObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

data class ThreadMessages(
    val messages: List<Message>,
    val sims: List<SimInfo>,
)

class MessageRepository(
    private val messages: MessagesDataSource,
    private val sims: SimDataSource,
    private val metadata: MessageMetadataRepository,
    private val changes: TelephonyChangeObserver,
    private val ioDispatcher: CoroutineDispatcher,
) {

    fun observeThread(threadId: Long): Flow<ThreadMessages> =
        combine(
            changes.changes().map { messages.loadMessages(threadId) to sims.activeSims() },
            metadata.observePinnedIdsInThread(threadId),
        ) { (loaded, activeSims), pinnedIds ->
            ThreadMessages(
                messages = loaded.map { it.copy(isPinned = it.id in pinnedIds) },
                sims = activeSims,
            )
        }.flowOn(ioDispatcher)

    /** Drops pins whose provider rows have since disappeared, e.g. deleted from another app. */
    suspend fun prunePinsAgainst(loaded: List<Message>, pinnedIds: Set<Long>) {
        val present = loaded.mapTo(mutableSetOf()) { it.id }
        metadata.prunePins(pinnedIds.filterNot { it in present })
    }
}
