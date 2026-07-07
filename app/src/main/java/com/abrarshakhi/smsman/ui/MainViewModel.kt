package com.abrarshakhi.smsman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.data.datastore.OnboardingTracker
import com.abrarshakhi.smsman.domain.model.AppSettings
import com.abrarshakhi.smsman.domain.repository.SettingsRepository
import com.abrarshakhi.smsman.ui.nav.ConversationListRoute
import com.abrarshakhi.smsman.ui.nav.OnboardingRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives two cross-cutting concerns for the activity:
 *   • [initialRoute] — the start key for the NavHost (Onboarding vs ConversationList).
 *   • [settings] — current AppSettings flow; the theme reads from this so toggle changes apply instantly.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    onboardingTracker: OnboardingTracker,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _initialRoute = MutableStateFlow<Any?>(null)
    val initialRoute: StateFlow<Any?> = _initialRoute.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    init {
        viewModelScope.launch {
            val completed = onboardingTracker.observeCompleted().first()
            _initialRoute.value = if (completed) ConversationListRoute else OnboardingRoute
        }
    }
}
