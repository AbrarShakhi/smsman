package com.abrarshakhi.smsman.domain.model

data class Conversation(
    val threadId: Long,
    val recipientAddresses: List<String>,
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

enum class ConversationFilter { All, Unread, Archived, Blocked }
