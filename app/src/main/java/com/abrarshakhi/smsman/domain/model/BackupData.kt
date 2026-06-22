package com.abrarshakhi.smsman.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val exportedAt: Long,
    val version: Int = 1,
    val conversations: List<BackupConversation>,
    val messages: List<BackupMessage>,
)

@Serializable
data class BackupConversation(
    val threadId: Long,
    val recipientAddresses: String,
    val snippet: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
    val archived: Boolean,
    val pinned: Boolean,
    val blocked: Boolean,
    val muted: Boolean,
)

@Serializable
data class BackupMessage(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String?,
    val date: Long,
    val type: Int,
    val read: Boolean,
    val status: Int,
    val subscriptionId: Int,
    val protocol: Int,
)
