package com.abrarshakhi.smsman.domain.model

data class Contact(
    val phoneNumberE164: String,
    val displayName: String?,
    val photoUri: String?,
    val lookupKey: String?,
) {
    val displayLabel: String get() = displayName ?: phoneNumberE164
}
