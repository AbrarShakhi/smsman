package com.abrarshakhi.smsman.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["threadId"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("threadId"),
        Index("date"),
        Index("address"),
        Index("type"),
        Index("read"),
    ],
)
data class MessageEntity(
    @PrimaryKey val id: Long,
    val threadId: Long,
    val address: String,
    val body: String?,
    val date: Long,
    val type: Int,                         // Telephony.Sms.MESSAGE_TYPE_*
    val read: Boolean,
    val seen: Boolean,
    val status: Int,                       // delivery status
    val subscriptionId: Int,
    val errorCode: Int,
    val protocol: Int,                     // 0 = SMS, 1 = MMS
    val mmsContentLocation: String?,
    val mmsExpiry: Long?,
    val mmsDownloadState: Int,
)
