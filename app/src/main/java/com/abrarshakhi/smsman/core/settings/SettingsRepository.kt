package com.abrarshakhi.smsman.core.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
private val KEY_COLOR_SCHEME = stringPreferencesKey("color_scheme")
private val KEY_FONT = stringPreferencesKey("font")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { it.toSettings() }

    suspend fun setThemeMode(mode: ThemeMode) = put(KEY_THEME_MODE, mode.name)

    suspend fun setColorScheme(option: ColorSchemeOption) = put(KEY_COLOR_SCHEME, option.name)

    suspend fun setFont(option: FontOption) = put(KEY_FONT, option.name)

    private suspend fun put(key: Preferences.Key<String>, value: String) {
        context.settingsDataStore.edit { it[key] = value }
    }

    /** Unknown stored values fall back to the default rather than throwing, so a renamed or removed
     *  option cannot leave the app unable to read its own settings. */
    private fun Preferences.toSettings() = AppSettings(
        themeMode = enumOrDefault(this[KEY_THEME_MODE], ThemeMode.SYSTEM),
        colorScheme = enumOrDefault(this[KEY_COLOR_SCHEME], ColorSchemeOption.DYNAMIC),
        font = enumOrDefault(this[KEY_FONT], FontOption.SYSTEM),
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(stored: String?, default: T): T =
        stored?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
