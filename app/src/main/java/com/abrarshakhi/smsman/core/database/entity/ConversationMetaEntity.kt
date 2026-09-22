package com.abrarshakhi.smsman.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-conversation metadata the Telephony provider cannot express.
 *
 * [threadId] is the provider's `threads._id`. Thread ids are recreated by
 * `Threads.getOrCreateThreadId`, so [recipientKey] (the sorted recipient address set) is kept as a
 * secondary key to re-attach metadata if a thread is renumbered.
 *
 * Carries an explicit [isFavorite] flag rather than using row presence, because this row is
 * expected to accumulate further settings (mute, custom colour) that must outlive un-favouriting.
 */
@Entity(tableName = "conversation_meta")
data class ConversationMetaEntity(
    @PrimaryKey val threadId: Long,
    val isFavorite: Boolean = false,
    val favoritedAt: Long? = null,
    val recipientKey: String? = null,
)
