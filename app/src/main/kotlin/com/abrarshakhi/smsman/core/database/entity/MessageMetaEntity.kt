package com.abrarshakhi.smsman.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
