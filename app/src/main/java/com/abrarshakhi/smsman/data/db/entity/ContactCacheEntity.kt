package com.abrarshakhi.smsman.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_cache")
data class ContactCacheEntity(
    @PrimaryKey val phoneNumberE164: String,
    val displayName: String?,
    val photoUri: String?,
    val lookupKey: String?,
    val lastSyncedAt: Long,
)
