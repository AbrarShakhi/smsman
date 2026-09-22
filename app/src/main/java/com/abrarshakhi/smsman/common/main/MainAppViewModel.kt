package com.abrarshakhi.smsman.common.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.settings.AppSettings
import com.abrarshakhi.smsman.core.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainAppViewModel(settingsRepository: SettingsRepository) : ViewModel() {

    /** Drives the theme for the whole application, so a settings change applies immediately. */
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
}
