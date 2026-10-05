package com.abrarshakhi.smsman.ui

import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.smsman.navigation.TopLevelDestination
import com.abrarshakhi.smsman.ui.component.AnimatedTabIcon

@Composable
fun AppNavigationBar(
    selectedRoute: NavKey,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShortNavigationBar(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = destination.route == selectedRoute
            val label = stringResource(destination.label)
            ShortNavigationBarItem(
                selected = selected,
                onClick = { onSelect(destination) },
                icon = {
                    AnimatedTabIcon(
                        icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        label = label,
                        selected = selected,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}
