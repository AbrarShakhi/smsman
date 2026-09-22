package com.abrarshakhi.smsman.core.model

import android.provider.Telephony

enum class MessageType {
    INBOX, SENT, DRAFT, OUTBOX, FAILED, QUEUED, UNKNOWN;

    val isOutgoing: Boolean get() = this != INBOX && this != UNKNOWN

    companion object {
        fun fromProvider(value: Int): MessageType = when (value) {
            Telephony.Sms.MESSAGE_TYPE_INBOX -> INBOX
            Telephony.Sms.MESSAGE_TYPE_SENT -> SENT
            Telephony.Sms.MESSAGE_TYPE_DRAFT -> DRAFT
            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> OUTBOX
            Telephony.Sms.MESSAGE_TYPE_FAILED -> FAILED
            Telephony.Sms.MESSAGE_TYPE_QUEUED -> QUEUED
            else -> UNKNOWN
        }
    }
}

/**
 * Provider STATUS_* values are GSM TP-Status bits (-1 / 0 / 32 / 64), deliberately not contiguous,
 * so they must never be treated as enum ordinals.
 */
enum class DeliveryStatus {
    NONE, COMPLETE, PENDING, FAILED;

    companion object {
        fun fromProvider(value: Int): DeliveryStatus = when (value) {
            Telephony.Sms.STATUS_COMPLETE -> COMPLETE
            Telephony.Sms.STATUS_PENDING -> PENDING
            Telephony.Sms.STATUS_FAILED -> FAILED
            else -> NONE
        }
    }
}

data class Message(
    val id: Long,
    val threadId: Long,
    val address: String?,
    val body: String,
    val date: Long,
    val dateSent: Long,
    val isRead: Boolean,
    val type: MessageType,
    val status: DeliveryStatus,
    val subscriptionId: Int,
    val isPinned: Boolean = false,
) {
    val isOutgoing: Boolean get() = type.isOutgoing
}

/**
 * A SIM as the user sees it. [slotIndex] rather than [subscriptionId] drives anything visual:
 * subscription ids are globally monotonic (live values on the test device are 2 and 3, not 0 and 1)
 * and both SIMs there report an identical icon tint, so tint cannot distinguish them either.
 */
data class SimInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
) {
    val label: String get() = displayName.ifBlank { carrierName.ifBlank { "SIM ${slotIndex + 1}" } }
}
