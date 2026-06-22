package com.abrarshakhi.smsman.domain.model

data class Attachment(
    val id: Long,
    val messageId: Long,
    val mimeType: String,
    val fileUri: String,
    val originalName: String?,
    val sizeBytes: Long,
    val width: Int?,
    val height: Int?,
    val durationMs: Long?,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isVideo: Boolean get() = mimeType.startsWith("video/")
    val isAudio: Boolean get() = mimeType.startsWith("audio/")
    val isContact: Boolean get() = mimeType == "text/x-vcard" || mimeType == "text/vcard"
}
