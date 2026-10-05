package com.abrarshakhi.smsman.ui.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.sms.SmsPermissions
import com.abrarshakhi.smsman.sms.SmsRoleManager
import com.abrarshakhi.smsman.ui.component.AnimatedIllustration
import com.abrarshakhi.smsman.ui.component.Illustration
import com.abrarshakhi.smsman.ui.component.MorphShape
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val roleManager: SmsRoleManager = koinInject()

    var permissionsGranted by remember { mutableStateOf(SmsPermissions.allGranted(context)) }
    var isDefaultSmsApp by remember { mutableStateOf(roleManager.isDefaultSmsApp()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionsGranted = SmsPermissions.allGranted(context)
        isDefaultSmsApp = roleManager.isDefaultSmsApp()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsGranted = SmsPermissions.allGranted(context) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        isDefaultSmsApp = result.resultCode == Activity.RESULT_OK || roleManager.isDefaultSmsApp()
    }

    val entrance = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            ContinueBar(enabled = permissionsGranted && isDefaultSmsApp, onContinue = onContinue)
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StaggeredReveal(state = entrance, order = 0) {
                    AnimatedIllustration(illustration = Illustration.Welcome, size = IllustrationSize)
                }
                Spacer(Modifier.height(16.dp))
                StaggeredReveal(state = entrance, order = 1) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.onboarding_title, stringResource(R.string.app_name)),
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.onboarding_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                StaggeredReveal(state = entrance, order = 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                        SetupStep(
                            index = 0,
                            icon = Icons.Rounded.Shield,
                            title = stringResource(R.string.onboarding_step_permissions_title),
                            detail = stringResource(R.string.onboarding_step_permissions_detail),
                            done = permissionsGranted,
                            actionLabel = stringResource(R.string.onboarding_step_permissions_action),
                            onAction = { permissionLauncher.launch(SmsPermissions.missing(context).toTypedArray()) },
                        )
                        SetupStep(
                            index = 1,
                            icon = Icons.Rounded.Sms,
                            title = stringResource(R.string.onboarding_step_sms_title),
                            detail = stringResource(R.string.onboarding_step_sms_detail),
                            done = isDefaultSmsApp,
                            actionLabel = stringResource(R.string.onboarding_step_sms_action),
                            onAction = { roleManager.requestRoleIntent()?.let(roleLauncher::launch) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredReveal(
    state: MutableTransitionState<Boolean>,
    order: Int,
    content: @Composable () -> Unit,
) {
    val delay = order * STAGGER_MILLIS
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(REVEAL_MILLIS, delayMillis = delay)) +
            slideInVertically(tween(REVEAL_MILLIS, delayMillis = delay, easing = FastOutSlowInEasing)) { it / 3 },
    ) {
        content()
    }
}

@Composable
private fun SetupStep(
    index: Int,
    icon: ImageVector,
    title: String,
    detail: String,
    done: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = STEP_COUNT),
        leadingContent = { StepBadge(icon = icon, done = done) },
        supportingContent = { Text(detail) },
        trailingContent = {
            AnimatedContent(
                targetState = done,
                transitionSpec = {
                    (scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec())) togetherWith
                        (scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()))
                },
            ) { isDone ->
                if (isDone) {
                    Text(
                        text = stringResource(R.string.onboarding_step_done),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    FilledTonalButton(onClick = onAction, shapes = ButtonDefaults.shapes()) {
                        Text(actionLabel)
                    }
                }
            }
        },
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepBadge(icon: ImageVector, done: Boolean) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val morph = remember { Morph(MaterialShapes.Cookie9Sided, MaterialShapes.Circle) }
    val progress by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = motion.defaultSpatialSpec(),
    )
    val container by animateColorAsState(
        targetValue = if (done) colors.primary else colors.primaryContainer,
        animationSpec = motion.defaultEffectsSpec(),
    )
    val content by animateColorAsState(
        targetValue = if (done) colors.onPrimary else colors.onPrimaryContainer,
        animationSpec = motion.defaultEffectsSpec(),
    )

    Box(
        modifier = Modifier
            .size(BadgeSize)
            .clip(MorphShape(morph, progress.coerceIn(0f, 1f)))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = done,
            transitionSpec = {
                (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(motion.fastEffectsSpec())) togetherWith
                    (scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()))
            },
        ) { isDone ->
            Icon(
                imageVector = if (isDone) Icons.Rounded.Check else icon,
                contentDescription = null,
                tint = content,
            )
        }
    }
}

@Composable
private fun ContinueBar(enabled: Boolean, onContinue: () -> Unit) {
    val height = ButtonDefaults.MediumContainerHeight
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Button(
            onClick = onContinue,
            shapes = ButtonDefaults.shapesFor(height),
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = height),
            contentPadding = ButtonDefaults.contentPaddingFor(height),
        ) {
            Text(stringResource(R.string.onboarding_continue), style = ButtonDefaults.textStyleFor(height))
            Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)),
            )
        }
    }
}

private const val STEP_COUNT = 2
private const val STAGGER_MILLIS = 110
private const val REVEAL_MILLIS = 520
private val IllustrationSize = 220.dp
private val BadgeSize = 44.dp
