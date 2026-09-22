package com.abrarshakhi.smsman.features.onboarding.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.LocalAppBackStack
import com.abrarshakhi.smsman.common.navigation.switchTapTo
import com.abrarshakhi.smsman.core.permissions.SmsPermissions
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val backStack = LocalAppBackStack.current
    val roleManager: SmsRoleManager = koinInject()

    var permissionsGranted by remember { mutableStateOf(SmsPermissions.allGranted(context)) }
    var isDefaultSmsApp by remember { mutableStateOf(roleManager.isDefaultSmsApp()) }

    // The role can also be changed from system Settings while we are backgrounded, so re-read on
    // every resume rather than trusting only the activity result.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsGranted = SmsPermissions.allGranted(context)
                isDefaultSmsApp = roleManager.isDefaultSmsApp()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsGranted = SmsPermissions.allGranted(context) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        isDefaultSmsApp = result.resultCode == Activity.RESULT_OK || roleManager.isDefaultSmsApp()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Set up SMS Man", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        SetupStep(
            title = "1. Permissions",
            detail = if (permissionsGranted) {
                "Granted"
            } else {
                "Needed to read messages, contacts and SIM details."
            },
            done = permissionsGranted,
            actionLabel = "Grant permissions",
            onAction = { permissionLauncher.launch(SmsPermissions.missing(context).toTypedArray()) },
        )

        Spacer(Modifier.height(12.dp))

        SetupStep(
            title = "2. Default SMS app",
            detail = if (isDefaultSmsApp) {
                "SMS Man is your default SMS app"
            } else {
                "Android only lets the default SMS app receive and store messages."
            },
            done = isDefaultSmsApp,
            actionLabel = "Set as default",
            onAction = { roleManager.requestRoleIntent()?.let(roleLauncher::launch) },
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { backStack.switchTapTo(AppRouteKey.AllMessages) },
            enabled = permissionsGranted && isDefaultSmsApp,
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun SetupStep(
    title: String,
    detail: String,
    done: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (done) "$title  ✓" else title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(detail, style = MaterialTheme.typography.bodyMedium)
            if (!done) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
