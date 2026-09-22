package com.abrarshakhi.smsman.common.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.back

/** Shared top-bar back affordance for any route pushed onto a tab. */
@Composable
fun BackNavigationIcon(backStack: SnapshotStateList<AppRouteKey>) {
    IconButton(onClick = { backStack.back() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
