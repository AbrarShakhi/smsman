package com.abrarshakhi.smsman.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index("lastMessageAt"),
        Index("archived"),
        Index("pinned"),
        Index("blocked"),
    ],
)
data class ConversationEntity(
    @PrimaryKey val threadId: Long,
    val recipientAddresses: String,        // comma-separated; resolved at render
    val snippet: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
    val archived: Boolean,
    val pinned: Boolean,
    val pinnedAt: Long?,
    val blocked: Boolean,
    val subscriptionId: Int?,
    val draft: String?,
    val muted: Boolean,
)
