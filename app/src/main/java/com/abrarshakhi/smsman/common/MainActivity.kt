package com.abrarshakhi.smsman.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abrarshakhi.smsman.common.main.AppRoot
import com.abrarshakhi.smsman.common.main.MainAppViewModel
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.permissionsAndRoleReady
import com.abrarshakhi.smsman.common.ui.theme.SmsmanTheme
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val roleManager: SmsRoleManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Resolved once, before the back stack is created: the back stack is saveable, so a later
        // change is handled by onboarding calling switchTapTo rather than by re-deriving this.
        val startRoute = if (permissionsAndRoleReady(this, roleManager)) {
            AppRouteKey.AllMessages
        } else {
            AppRouteKey.Onboarding
        }

        setContent {
            SmsmanTheme {
                val mainAppViewModel: MainAppViewModel = koinViewModel()
                AppRoot(startRoute = startRoute, mainAppViewModel = mainAppViewModel)
            }
        }
    }
}
