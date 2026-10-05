package com.abrarshakhi.smsman.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.smsman.navigation.AppRouteKey
import com.abrarshakhi.smsman.navigation.Navigator
import com.abrarshakhi.smsman.navigation.TOP_LEVEL_ROUTES
import com.abrarshakhi.smsman.navigation.appEntryProvider
import com.abrarshakhi.smsman.navigation.isTopLevel
import com.abrarshakhi.smsman.navigation.rememberNavigationState
import com.abrarshakhi.smsman.ui.onboarding.OnboardingScreen

@Composable
fun AppRoot(
    isOnboardingRequired: Boolean,
    deepLinkRoute: NavKey?,
) {
    var showOnboarding by rememberSaveable { mutableStateOf(isOnboardingRequired) }

    if (showOnboarding) {
        OnboardingScreen(onContinue = { showOnboarding = false })
    } else {
        AppShell(deepLinkRoute = deepLinkRoute)
    }
}

@Composable
private fun AppShell(deepLinkRoute: NavKey?) {
    val navigationState = rememberNavigationState(
        startRoute = AppRouteKey.AllMessages,
        topLevelRoutes = TOP_LEVEL_ROUTES,
        deepLinkRoute = deepLinkRoute,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val entryProvider = remember(navigator) { appEntryProvider(navigator) }
    val motion = MaterialTheme.motionScheme

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = navigationState.currentRoute.isTopLevel,
                enter = expandVertically(motion.defaultSpatialSpec()) +
                    fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.fastSpatialSpec()) +
                    fadeOut(motion.fastEffectsSpec()),
            ) {
                AppNavigationBar(
                    selectedRoute = navigationState.topLevelRoute,
                    onSelect = { navigator.navigate(it.route) },
                )
            }
        },
    ) { padding ->
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entryProvider),
            onBack = navigator::goBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        )
    }
}
