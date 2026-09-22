package com.abrarshakhi.smsman.common.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.currentRoute
import com.abrarshakhi.smsman.common.navigation.switchTapTo

private data class HomeTabItem(
    val route: AppRouteKey.HomeTab,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
)

private val homeTabs = listOf(
    HomeTabItem(AppRouteKey.AllMessages, R.drawable.ic_tab_messages, R.string.tab_all_messages),
    HomeTabItem(AppRouteKey.Favorite, R.drawable.ic_tab_favorite, R.string.tab_favorite),
    HomeTabItem(AppRouteKey.Pinned, R.drawable.ic_tab_pinned, R.string.tab_pinned),
)

/**
 * Shared by all three [AppRouteKey.HomeTab] chromes. Tabs replace the back stack rather than
 * stacking onto it, so switching tabs never deepens history.
 */
@Composable
fun AppBottomBar(backStack: SnapshotStateList<AppRouteKey>) {
    val current = backStack.currentRoute()
    ShortNavigationBar {
        homeTabs.forEach { tab ->
            val label = stringResource(tab.label)
            ShortNavigationBarItem(
                selected = current == tab.route,
                onClick = { if (current != tab.route) backStack.switchTapTo(tab.route) },
                icon = { Icon(painterResource(tab.icon), contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}
