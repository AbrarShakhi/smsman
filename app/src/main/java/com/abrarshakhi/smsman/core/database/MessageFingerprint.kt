package com.abrarshakhi.smsman.core.database

import java.security.MessageDigest

/** Content-derived identity for a provider message row; see [MessageMetaEntity] for why. */
object MessageFingerprint {

    fun of(threadId: Long, date: Long, type: Int, body: String): String {
        val payload = "$threadId|$date|$type|$body"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString(separator = "") { "%02x".format(it) }
    }
}
