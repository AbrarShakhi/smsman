package com.abrarshakhi.smsman.domain.model

data class SendRequest(
    val threadId: Long?,                   // null → resolve from recipient(s)
    val recipients: List<String>,          // single for SMS; multi-recipient implies MMS (M4)
    val body: String,
    val subscriptionId: Int = -1,          // -1 → SmsManager default
)

sealed interface SendResult {
    data class Queued(val messageId: Long, val threadId: Long, val parts: Int) : SendResult
    data class Failed(val reason: String, val cause: Throwable? = null) : SendResult
}
