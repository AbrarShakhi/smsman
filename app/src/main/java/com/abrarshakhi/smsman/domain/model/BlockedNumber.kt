package com.abrarshakhi.smsman.domain.model

data class BlockedNumber(
    val phoneNumberE164: String,
    val blockedAt: Long,
    val reason: String?,
)
