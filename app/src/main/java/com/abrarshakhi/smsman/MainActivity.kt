package com.abrarshakhi.smsman

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.ui.MainViewModel
import com.abrarshakhi.smsman.ui.nav.AppNavHost
import com.abrarshakhi.smsman.ui.nav.ChatRoute
import com.abrarshakhi.smsman.ui.theme.SMSManTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { viewModel.initialRoute.value == null }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val coldDeepLink = parseDeepLink(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            SMSManTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
                val computedStart by viewModel.initialRoute.collectAsStateWithLifecycle()
                val startKey = coldDeepLink ?: computedStart
                startKey?.let { AppNavHost(startKey = it) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun parseDeepLink(intent: Intent?): Any? {
        val data: Uri = intent?.data ?: return null
        if (data.scheme != "smsman") return null
        return when (data.host) {
            "thread" -> data.lastPathSegment?.toLongOrNull()?.let { ChatRoute(it) }
            else -> null
        }
    }
}
