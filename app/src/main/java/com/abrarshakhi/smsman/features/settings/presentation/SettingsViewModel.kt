package com.abrarshakhi.smsman.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.settings.AppSettings
import com.abrarshakhi.smsman.core.settings.ColorSchemeOption
import com.abrarshakhi.smsman.core.settings.FontOption
import com.abrarshakhi.smsman.core.settings.SettingsRepository
import com.abrarshakhi.smsman.core.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun onThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }

    fun onColorScheme(option: ColorSchemeOption) =
        viewModelScope.launch { repository.setColorScheme(option) }

    fun onFont(option: FontOption) = viewModelScope.launch { repository.setFont(option) }
}
