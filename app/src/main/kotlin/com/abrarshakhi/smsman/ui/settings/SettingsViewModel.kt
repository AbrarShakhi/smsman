package com.abrarshakhi.smsman.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.data.settings.SettingsRepository
import com.abrarshakhi.smsman.model.AppSettings
import com.abrarshakhi.smsman.model.ColorSchemeOption
import com.abrarshakhi.smsman.model.FontOption
import com.abrarshakhi.smsman.model.ThemeMode
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
