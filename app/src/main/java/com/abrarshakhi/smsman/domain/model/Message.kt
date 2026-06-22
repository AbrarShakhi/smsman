package com.abrarshakhi.smsman.domain.model

data class Message(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String?,
    val date: Long,
    val type: MessageType,
    val read: Boolean,
    val seen: Boolean,
    val status: MessageStatus,
    val subscriptionId: Int,
    val errorCode: Int,
    val protocol: MessageProtocol,
    val mmsContentLocation: String?,
    val mmsExpiry: Long?,
    val mmsDownloadState: MmsDownloadState,
    val attachments: List<Attachment> = emptyList(),
)

/**
 * Maps to Telephony.Sms.MESSAGE_TYPE_*. The integer values are stable contract
 * with the system Telephony provider — keep them in sync.
 */
enum class MessageType(val raw: Int) {
    Inbox(1),
    Sent(2),
    Draft(3),
    Outbox(4),
    Failed(5),
    Queued(6);

    companion object {
        fun fromRaw(raw: Int): MessageType = entries.firstOrNull { it.raw == raw } ?: Inbox
    }
}

/**
 * Maps to Telephony.Sms.STATUS_* / per-SmsManager send-result codes.
 * "None" represents the inbox / not-applicable case.
 */
enum class MessageStatus(val raw: Int) {
    None(-1),
    Sending(0),
    Sent(1),
    Delivered(2),
    Read(3),
    Failed(64);

    companion object {
        fun fromRaw(raw: Int): MessageStatus = entries.firstOrNull { it.raw == raw } ?: None
    }
}

enum class MessageProtocol(val raw: Int) {
    Sms(0),
    Mms(1);

    companion object {
        fun fromRaw(raw: Int): MessageProtocol = entries.firstOrNull { it.raw == raw } ?: Sms
    }
}

enum class MmsDownloadState(val raw: Int) {
    NotApplicable(0),
    Pending(1),
    Downloading(2),
    Done(3),
    Failed(4);

    companion object {
        fun fromRaw(raw: Int): MmsDownloadState = entries.firstOrNull { it.raw == raw } ?: NotApplicable
    }
}
