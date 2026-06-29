package com.abrarshakhi.smsman.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_messages",
    indices = [Index("scheduledAt"), Index("status")],
)
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long?,
    val recipients: String,                // comma-separated
    val body: String,
    val attachmentsJson: String,           // serialized list of attachment URIs
    val scheduledAt: Long,
    val subscriptionId: Int,
    val workRequestId: String,
    val status: Int,                       // 0=Pending, 1=Sent, 2=Failed, 3=Cancelled
    val lastErrorMessage: String?,
)
