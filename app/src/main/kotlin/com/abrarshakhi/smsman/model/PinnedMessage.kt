package com.abrarshakhi.smsman.model

data class PinnedMessage(
    val message: Message,
    val senderLabel: String,
    val pinnedAt: Long,
)
