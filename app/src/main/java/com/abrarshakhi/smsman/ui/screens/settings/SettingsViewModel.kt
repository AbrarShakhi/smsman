package com.abrarshakhi.smsman.ui.screens.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.AppSettings
import com.abrarshakhi.smsman.domain.model.Sim
import com.abrarshakhi.smsman.domain.model.ThemeMode
import com.abrarshakhi.smsman.domain.repository.SettingsRepository
import com.abrarshakhi.smsman.domain.repository.SimRepository
import com.abrarshakhi.smsman.framework.SmsRoleProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEffect {
    data class RequestDefaultSmsApp(val intent: Intent) : SettingsEffect
}

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val isDefaultSmsApp: Boolean = true,
    val sims: List<Sim> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val smsRoleProvider: SmsRoleProvider,
    private val simRepository: SimRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SettingsEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { s ->
                _state.update { it.copy(settings = s) }
            }
        }
        refreshDefaultAppState()
        loadSims()
    }

    private fun loadSims() {
        _state.update { it.copy(sims = simRepository.listSims()) }
    }

    fun refreshDefaultAppState() {
        _state.update { it.copy(isDefaultSmsApp = smsRoleProvider.isDefaultSmsApp()) }
    }

    fun requestDefaultApp() = viewModelScope.launch {
        _effects.emit(SettingsEffect.RequestDefaultSmsApp(smsRoleProvider.createRoleRequestIntent()))
    }

    fun setDefaultSim(subscriptionId: Int?) = viewModelScope.launch {
        settingsRepository.setDefaultSubscriptionId(subscriptionId)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDynamicColor(enabled)
    }

    fun setRequestDeliveryReports(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setRequestDeliveryReports(enabled)
    }

    fun setRequestReadReceipts(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setRequestReadReceipts(enabled)
    }
}
