package com.abrarshakhi.smsman.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A pinned message. Row presence *is* the pin — unpinning deletes the row — because a pin is the
 * only reason this row exists and that keeps the Pinned tab a plain table read.
 *
 * [fingerprint] exists because `sms._id` is only stable while the row lives: a backup/restore or an
 * SMS import renumbers rows, which would silently re-attach every pin to the wrong message.
 * Resolve by [messageId] first and fall back to the fingerprint when it misses.
 */
@Entity(
    tableName = "message_meta",
    indices = [Index("threadId"), Index("fingerprint")],
)
data class MessageMetaEntity(
    @PrimaryKey val messageId: Long,
    val threadId: Long,
    val fingerprint: String,
    val pinnedAt: Long,
)
