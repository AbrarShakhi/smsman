package com.abrarshakhi.smsman.ui.screens.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val defaultAppLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onIntent(OnboardingIntent.DefaultAppResultReturned)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        viewModel.onIntent(OnboardingIntent.PermissionsResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.LaunchDefaultAppIntent -> defaultAppLauncher.launch(effect.intent)
                is OnboardingEffect.RequestPermissions -> permissionsLauncher.launch(effect.permissions.toTypedArray())
                OnboardingEffect.NavigateToHome -> onComplete()
            }
        }
    }

    Scaffold { padding ->
        OnboardingContent(
            state = state,
            paddingValues = padding,
            onIntent = viewModel::onIntent,
        )
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    paddingValues: PaddingValues,
    onIntent: (OnboardingIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        StepProgress(step = state.currentStep)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "step",
            ) { step ->
                when (step) {
                    OnboardingStep.Welcome -> StepBody(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        title = "Welcome to SMS Man",
                        body = "Your SMS, MMS, and group chats in one beautifully crafted messenger built for Android.",
                    )
                    OnboardingStep.DefaultApp -> StepBody(
                        icon = Icons.Filled.Star,
                        title = if (state.isDefaultSmsApp) "You're set as default" else "Set as default SMS app",
                        body = if (state.isDefaultSmsApp) {
                            "SMS Man is your default SMS app. Tap Continue to grant a few permissions."
                        } else {
                            "To send and receive messages, SMS Man needs to be your default SMS app. The system will ask you to confirm."
                        },
                    )
                    OnboardingStep.Permissions -> StepBody(
                        icon = Icons.Filled.Lock,
                        title = "A few permissions",
                        body = "We use Contacts to show sender names, Phone state to detect your SIMs, and Notifications to alert you when messages arrive.",
                    )
                }
            }
        }

        StepActions(state = state, onIntent = onIntent)
    }
}

@Composable
private fun StepProgress(step: OnboardingStep) {
    val progress = (step.index + 1) / step.total.toFloat()
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    )
}

@Composable
private fun StepBody(icon: ImageVector, title: String, body: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Surface(
            modifier = Modifier.size(128.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StepActions(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    val (primaryLabel, primaryIntent) = when (state.currentStep) {
        OnboardingStep.Welcome -> "Get started" to OnboardingIntent.Advance
        OnboardingStep.DefaultApp -> {
            if (state.isDefaultSmsApp) "Continue" to OnboardingIntent.Advance
            else "Set as default" to OnboardingIntent.RequestDefaultApp
        }
        OnboardingStep.Permissions -> {
            if (state.missingPermissions.isEmpty()) "Finish" to OnboardingIntent.Skip
            else "Grant permissions" to OnboardingIntent.RequestPermissions
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onIntent(primaryIntent) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(),
        ) {
            Text(primaryLabel, style = MaterialTheme.typography.titleMedium)
        }
        if (state.currentStep != OnboardingStep.Welcome) {
            TextButton(
                onClick = { onIntent(OnboardingIntent.Skip) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Skip")
            }
        } else {
            // Reserve the space so the primary button doesn't jump when arriving at the second step.
            Box(modifier = Modifier.size(width = 1.dp, height = 48.dp))
        }
    }
}
