package com.abrarshakhi.smsman.common.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigatorTest {

    @Test
    fun navigateToNestedRoutePushesOntoCurrentStack() {
        val state = navigationState()

        Navigator(state).navigate(AppRouteKey.Chat(threadId = 1))

        assertEquals(
            listOf(AppRouteKey.AllMessages, AppRouteKey.Chat(threadId = 1)),
            state.backStacks.getValue(AppRouteKey.AllMessages).toList(),
        )
        assertEquals(AppRouteKey.Chat(threadId = 1), state.currentRoute)
    }

    @Test
    fun goBackFromDeepLinkedChatReturnsToStartRoute() {
        val state = navigationState(
            startStack = listOf(AppRouteKey.AllMessages, AppRouteKey.Chat(threadId = 7)),
        )

        Navigator(state).goBack()

        assertEquals(AppRouteKey.AllMessages, state.currentRoute)
        assertEquals(
            listOf(AppRouteKey.AllMessages),
            state.backStacks.getValue(AppRouteKey.AllMessages).toList(),
        )
    }

    @Test
    fun goBackAtStartRouteNeverEmptiesTheStack() {
        val state = navigationState()
        val navigator = Navigator(state)

        navigator.goBack()
        navigator.goBack()

        assertEquals(AppRouteKey.AllMessages, state.topLevelRoute)
        assertEquals(
            listOf(AppRouteKey.AllMessages),
            state.backStacks.getValue(AppRouteKey.AllMessages).toList(),
        )
    }

    @Test
    fun navigateToTopLevelRouteSwitchesStack() {
        val state = navigationState()

        Navigator(state).navigate(AppRouteKey.Favorite)

        assertEquals(AppRouteKey.Favorite, state.topLevelRoute)
        assertEquals(AppRouteKey.Favorite, state.currentRoute)
        assertEquals(
            listOf(AppRouteKey.AllMessages),
            state.backStacks.getValue(AppRouteKey.AllMessages).toList(),
        )
    }

    @Test
    fun goBackFromTopLevelRootReturnsToStartRoute() {
        val state = navigationState()
        val navigator = Navigator(state)

        navigator.navigate(AppRouteKey.Pinned)
        navigator.goBack()

        assertEquals(AppRouteKey.AllMessages, state.topLevelRoute)
        assertEquals(AppRouteKey.AllMessages, state.currentRoute)
    }

    @Test
    fun topLevelRoutesRetainTheirOwnStacks() {
        val state = navigationState()
        val navigator = Navigator(state)

        navigator.navigate(AppRouteKey.Favorite)
        navigator.navigate(AppRouteKey.Chat(threadId = 2))
        navigator.navigate(AppRouteKey.AllMessages)

        assertEquals(AppRouteKey.AllMessages, state.currentRoute)

        navigator.navigate(AppRouteKey.Favorite)

        assertEquals(AppRouteKey.Chat(threadId = 2), state.currentRoute)
    }

    private fun navigationState(
        startStack: List<NavKey> = listOf(AppRouteKey.AllMessages),
    ): NavigationState {
        val startRoute: NavKey = AppRouteKey.AllMessages
        val topLevelRoutes = listOf(AppRouteKey.AllMessages, AppRouteKey.Favorite, AppRouteKey.Pinned)
        return NavigationState(
            startRoute = startRoute,
            topLevelRoute = mutableStateOf(startRoute),
            backStacks = topLevelRoutes.associateWith<NavKey, NavBackStack<NavKey>> { route ->
                if (route == startRoute) {
                    NavBackStack(*startStack.toTypedArray())
                } else {
                    NavBackStack(route)
                }
            },
        )
    }
}
