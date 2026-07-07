package com.abrarshakhi.smsman.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.abrarshakhi.smsman.domain.model.AppSettings
import com.abrarshakhi.smsman.domain.model.ThemeMode
import com.abrarshakhi.smsman.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.System,
            dynamicColor = prefs[KEY_DYNAMIC] ?: true,
            requestDeliveryReports = prefs[KEY_DELIVERY] ?: true,
            requestReadReceipts = prefs[KEY_READ_RECEIPT] ?: false,
            defaultSubscriptionId = prefs[KEY_SUB_ID]?.takeIf { it >= 0 },
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.appSettingsDataStore.edit { it[KEY_THEME] = mode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[KEY_DYNAMIC] = enabled }
    }

    override suspend fun setRequestDeliveryReports(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[KEY_DELIVERY] = enabled }
    }

    override suspend fun setRequestReadReceipts(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[KEY_READ_RECEIPT] = enabled }
    }

    override suspend fun setDefaultSubscriptionId(subId: Int?) {
        context.appSettingsDataStore.edit {
            if (subId == null) it.remove(KEY_SUB_ID) else it[KEY_SUB_ID] = subId
        }
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC = booleanPreferencesKey("dynamic_color")
        val KEY_DELIVERY = booleanPreferencesKey("request_delivery_reports")
        val KEY_READ_RECEIPT = booleanPreferencesKey("request_read_receipts")
        val KEY_SUB_ID = intPreferencesKey("default_subscription_id")
    }
}
