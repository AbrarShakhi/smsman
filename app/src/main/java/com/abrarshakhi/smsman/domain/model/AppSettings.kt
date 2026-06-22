package com.abrarshakhi.smsman.domain.model

enum class ThemeMode { System, Light, Dark, Black }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val requestDeliveryReports: Boolean = true,
    val requestReadReceipts: Boolean = false,
    val defaultSubscriptionId: Int? = null,
)
