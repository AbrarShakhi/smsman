package com.abrarshakhi.smsman.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.data.datastore.OnboardingTracker
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

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val smsRoleProvider: SmsRoleProvider,
    private val onboardingTracker: OnboardingTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<OnboardingEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<OnboardingEffect> = _effects.asSharedFlow()

    init {
        refreshSystemState()
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Advance -> advance()
            OnboardingIntent.RequestDefaultApp -> requestDefaultApp()
            OnboardingIntent.DefaultAppResultReturned -> onDefaultAppResultReturned()
            OnboardingIntent.RequestPermissions -> requestPermissions()
            is OnboardingIntent.PermissionsResult -> onPermissionsResult()
            OnboardingIntent.Skip -> complete()
        }
    }

    private fun refreshSystemState() {
        _state.update {
            it.copy(
                isDefaultSmsApp = smsRoleProvider.isDefaultSmsApp(),
                missingPermissions = smsRoleProvider.missingRuntimePermissions(),
            )
        }
    }

    private fun advance() {
        refreshSystemState()
        val next = when (state.value.currentStep) {
            OnboardingStep.Welcome -> OnboardingStep.DefaultApp
            OnboardingStep.DefaultApp -> OnboardingStep.Permissions
            OnboardingStep.Permissions -> {
                complete()
                return
            }
        }
        _state.update { it.copy(currentStep = next) }
    }

    private fun requestDefaultApp() {
        if (smsRoleProvider.isDefaultSmsApp()) {
            advance()
            return
        }
        viewModelScope.launch {
            _effects.emit(OnboardingEffect.LaunchDefaultAppIntent(smsRoleProvider.createRoleRequestIntent()))
        }
    }

    private fun onDefaultAppResultReturned() {
        refreshSystemState()
        // Advance regardless — if user denied, they can re-tap or skip; we don't lock them out.
        if (state.value.isDefaultSmsApp) advance()
    }

    private fun requestPermissions() {
        val missing = smsRoleProvider.missingRuntimePermissions()
        if (missing.isEmpty()) {
            complete()
            return
        }
        viewModelScope.launch {
            _effects.emit(OnboardingEffect.RequestPermissions(missing))
        }
    }

    private fun onPermissionsResult() {
        refreshSystemState()
        // Complete only if all required permissions were granted. If any are still missing,
        // stay on the Permissions step so the user can retry or explicitly tap Skip.
        if (state.value.missingPermissions.isEmpty()) {
            complete()
        }
    }

    private fun complete() {
        viewModelScope.launch {
            onboardingTracker.markCompleted()
            _effects.emit(OnboardingEffect.NavigateToHome)
        }
    }
}
