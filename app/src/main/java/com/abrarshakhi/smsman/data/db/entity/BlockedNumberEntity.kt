package com.abrarshakhi.smsman.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey val phoneNumberE164: String,
    val blockedAt: Long,
    val reason: String?,
)
