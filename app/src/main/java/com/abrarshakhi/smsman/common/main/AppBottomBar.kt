package com.abrarshakhi.smsman.common.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HomeMax
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HomeMax
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.currentRoute
import com.abrarshakhi.smsman.common.navigation.switchTapTo
import com.abrarshakhi.smsman.common.ui.components.AnimatedTabIcon

private data class HomeTabItem(
    val route: AppRouteKey.HomeTab,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    @param:StringRes val label: Int,
)

private val homeTabs = listOf(
    HomeTabItem(
        route = AppRouteKey.AllMessages,
        selectedIcon = Icons.AutoMirrored.Filled.Message,
        unSelectedIcon = Icons.AutoMirrored.Outlined.Message, label = R.string.tab_all_messages,
    ),
    HomeTabItem(
        route = AppRouteKey.Favorite,
        selectedIcon = Icons.Filled.Favorite,
        unSelectedIcon = Icons.Outlined.FavoriteBorder, label = R.string.tab_favorite,
    ),
    HomeTabItem(
        route = AppRouteKey.Pinned,
        selectedIcon = Icons.Filled.PushPin,
        unSelectedIcon = Icons.Outlined.PushPin, label = R.string.tab_pinned,
    ),
)

@Composable
fun AppBottomBar(backStack: SnapshotStateList<AppRouteKey>) {
    val current = backStack.currentRoute()
    NavigationBar {
        homeTabs.forEach { tab ->
            val label = stringResource(tab.label)
            val selected = current == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (current != tab.route) backStack.switchTapTo(tab.route)
                },
                icon = {
                    AnimatedTabIcon(
                        icon = if (selected) tab.selectedIcon else tab.unSelectedIcon,
                        label = label,
                        selected = selected,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}
