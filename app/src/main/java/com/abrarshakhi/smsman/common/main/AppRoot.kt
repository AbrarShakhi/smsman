package com.abrarshakhi.smsman.common.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.abrarshakhi.smsman.common.navigation.AppNavigation
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.currentRoute
import com.abrarshakhi.smsman.common.navigation.rememberAppBackStack

@Composable
fun AppRoot(startRoute: AppRouteKey = AppRouteKey.AllMessages, mainAppViewModel: MainAppViewModel) {
    val backStack = rememberAppBackStack(startRoute)
    val current = backStack.currentRoute()
    val currentChrome = current?.chrome()

    val scrollBehaviorTop = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(current) {
        scrollBehaviorTop.state.contentOffset = 0f
        scrollBehaviorTop.state.heightOffset = 0f
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehaviorTop.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { currentChrome?.topBar?.invoke(backStack, scrollBehaviorTop) },
        bottomBar = { currentChrome?.bottomBar?.invoke(backStack) },
        floatingActionButton = { currentChrome?.fab?.invoke(backStack) }
    ) { innerPadding ->
        AppNavigation(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            mainAppViewModel = mainAppViewModel
        )
    }
}
