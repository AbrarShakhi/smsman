package com.abrarshakhi.smsman.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abrarshakhi.smsman.common.main.AppRoot
import com.abrarshakhi.smsman.common.main.MainAppViewModel
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.permissionsAndRoleReady
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.common.ui.theme.SmsmanTheme
import com.abrarshakhi.smsman.core.notification.EXTRA_THREAD_ID
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

private fun android.content.Intent?.threadIdExtra(): Long? =
    this?.getLongExtra(EXTRA_THREAD_ID, -1L)?.takeIf { it >= 0 }

class MainActivity : ComponentActivity() {

    private val roleManager: SmsRoleManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Resolved once, before the back stack is created: the back stack is saveable, so a later
        // change is handled by onboarding calling switchTapTo rather than by re-deriving this.
        val startRoute = when {
            !permissionsAndRoleReady(this, roleManager) -> AppRouteKey.Onboarding
            // Launched by tapping a notification: open that conversation directly.
            else -> intent.threadIdExtra()?.let(AppRouteKey::Chat) ?: AppRouteKey.AllMessages
        }

        setContent {
            val mainAppViewModel: MainAppViewModel = koinViewModel()
            val settings by mainAppViewModel.settings.collectAsStateWithLifecycle()
            SmsmanTheme(settings = settings) {
                AppRoot(startRoute = startRoute, mainAppViewModel = mainAppViewModel)
            }
        }
    }
}
