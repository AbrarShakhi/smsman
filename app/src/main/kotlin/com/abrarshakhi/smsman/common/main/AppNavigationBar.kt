package com.abrarshakhi.smsman.common.main

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.smsman.common.navigation.TopLevelDestination
import com.abrarshakhi.smsman.common.ui.components.AnimatedTabIcon

@Composable
fun AppNavigationBar(
    selectedRoute: NavKey,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = destination.route == selectedRoute
            val label = stringResource(destination.label)
            NavigationBarItem(
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
