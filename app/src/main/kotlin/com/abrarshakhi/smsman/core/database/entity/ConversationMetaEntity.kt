package com.abrarshakhi.smsman.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation_meta")
data class ConversationMetaEntity(
    @PrimaryKey val threadId: Long,
    val isFavorite: Boolean = false,
    val favoritedAt: Long? = null,
    val recipientKey: String? = null,
)
