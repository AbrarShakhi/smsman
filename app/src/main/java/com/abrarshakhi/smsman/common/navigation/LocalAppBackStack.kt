package com.abrarshakhi.smsman.common.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Koin's `navigation<T> {}` builder is `@Composable Scope.(T) -> Unit`, so there is no parameter
 * slot to thread the back stack through to a screen. [AppRoot] provides it here instead; screens
 * read it with `LocalAppBackStack.current` and use the helpers in BackStackController.
 */
val LocalAppBackStack = compositionLocalOf<SnapshotStateList<AppRouteKey>> {
    error("LocalAppBackStack not provided; AppRoot must wrap the content.")
}
