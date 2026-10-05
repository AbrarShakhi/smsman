package com.abrarshakhi.smsman.common.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.smsman.R

enum class TopLevelDestination(
    val route: AppRouteKey,
    @param:StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    AllMessages(
        route = AppRouteKey.AllMessages,
        label = R.string.tab_all_messages,
        selectedIcon = Icons.AutoMirrored.Filled.Message,
        unselectedIcon = Icons.AutoMirrored.Outlined.Message,
    ),
    Favorite(
        route = AppRouteKey.Favorite,
        label = R.string.tab_favorite,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
    ),
    Pinned(
        route = AppRouteKey.Pinned,
        label = R.string.tab_pinned,
        selectedIcon = Icons.Filled.PushPin,
        unselectedIcon = Icons.Outlined.PushPin,
    ),
}

val TOP_LEVEL_ROUTES: Set<NavKey> = TopLevelDestination.entries.mapTo(LinkedHashSet()) { it.route }

val NavKey.isTopLevel: Boolean get() = this in TOP_LEVEL_ROUTES
