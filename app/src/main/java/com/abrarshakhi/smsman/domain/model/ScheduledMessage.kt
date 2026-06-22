package com.abrarshakhi.smsman.domain.model

data class ScheduledMessage(
    val id: Long,
    val threadId: Long?,
    val recipients: List<String>,
    val body: String,
    val attachments: List<String>,
    val scheduledAt: Long,
    val subscriptionId: Int,
    val workRequestId: String,
    val status: ScheduledStatus,
    val lastErrorMessage: String?,
)

enum class ScheduledStatus { Pending, Sent, Failed, Cancelled }
