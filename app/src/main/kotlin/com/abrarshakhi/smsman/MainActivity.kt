package com.abrarshakhi.smsman

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.navigation.AppRouteKey
import com.abrarshakhi.smsman.notification.EXTRA_THREAD_ID
import com.abrarshakhi.smsman.sms.SmsRoleManager
import com.abrarshakhi.smsman.sms.permissionsAndRoleReady
import com.abrarshakhi.smsman.ui.AppRoot
import com.abrarshakhi.smsman.ui.MainAppViewModel
import com.abrarshakhi.smsman.ui.theme.SmsmanTheme
import com.abrarshakhi.smsman.ui.theme.isDark
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

private val LightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

private fun Intent?.threadIdExtra(): Long? =
    this?.getLongExtra(EXTRA_THREAD_ID, -1L)?.takeIf { it >= 0 }

class MainActivity : ComponentActivity() {

    private val roleManager: SmsRoleManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isOnboardingRequired = !permissionsAndRoleReady(this, roleManager)
        val deepLinkRoute = intent.threadIdExtra()?.let(AppRouteKey::Chat)

        setContent {
            val mainAppViewModel: MainAppViewModel = koinViewModel()
            val settings by mainAppViewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = settings.themeMode.isDark()
            splashScreen.setKeepOnScreenCondition { !settings.isLoaded }
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
                )
                onDispose {}
            }
            SmsmanTheme(settings = settings) {
                AppRoot(
                    isOnboardingRequired = isOnboardingRequired,
                    deepLinkRoute = deepLinkRoute,
                )
            }
        }
    }
}
