package com.abrarshakhi.smsman.common.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateList

val LocalAppBackStack = compositionLocalOf<SnapshotStateList<AppRouteKey>> {
    error("LocalAppBackStack not provided; AppRoot must wrap the content.")
}
