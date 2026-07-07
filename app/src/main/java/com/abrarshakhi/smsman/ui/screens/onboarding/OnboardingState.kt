package com.abrarshakhi.smsman.ui.screens.onboarding

import android.content.Intent

enum class OnboardingStep(val index: Int, val total: Int = 3) {
    Welcome(0),
    DefaultApp(1),
    Permissions(2),
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val isDefaultSmsApp: Boolean = false,
    val missingPermissions: List<String> = emptyList(),
    val isWorking: Boolean = false,
)

sealed interface OnboardingIntent {
    data object Advance : OnboardingIntent
    data object RequestDefaultApp : OnboardingIntent
    data object DefaultAppResultReturned : OnboardingIntent
    data class PermissionsResult(val granted: Map<String, Boolean>) : OnboardingIntent
    data object RequestPermissions : OnboardingIntent
    data object Skip : OnboardingIntent
}

sealed interface OnboardingEffect {
    data class LaunchDefaultAppIntent(val intent: Intent) : OnboardingEffect
    data class RequestPermissions(val permissions: List<String>) : OnboardingEffect
    data object NavigateToHome : OnboardingEffect
}
