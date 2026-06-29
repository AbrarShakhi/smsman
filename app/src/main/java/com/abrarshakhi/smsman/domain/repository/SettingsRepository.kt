package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.AppSettings
import com.abrarshakhi.smsman.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setRequestDeliveryReports(enabled: Boolean)
    suspend fun setRequestReadReceipts(enabled: Boolean)
    suspend fun setDefaultSubscriptionId(subId: Int?)
}
