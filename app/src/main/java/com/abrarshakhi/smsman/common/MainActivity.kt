package com.abrarshakhi.smsman.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abrarshakhi.smsman.common.main.AppRoot
import com.abrarshakhi.smsman.common.main.MainAppViewModel
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.ui.theme.SmsmanTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmsmanTheme {
                val mainAppViewModel: MainAppViewModel = koinViewModel()
                AppRoot(startRoute = AppRouteKey.AllMessages, mainAppViewModel)
            }
        }
    }
}