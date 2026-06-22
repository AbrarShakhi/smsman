package com.abrarshakhi.smsman.domain.model

data class Sim(
    val subscriptionId: Int,
    val carrierName: String,
    val displayName: String,
    val phoneNumber: String?,
    val slotIndex: Int,
    val iconTint: Long,
)
