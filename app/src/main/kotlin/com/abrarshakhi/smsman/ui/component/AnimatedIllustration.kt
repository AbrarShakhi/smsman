package com.abrarshakhi.smsman.ui.component

import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.R
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.LottieDynamicProperty
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.model.KeyPath

enum class Illustration(@param:RawRes val resId: Int) {
    Welcome(R.raw.illustration_welcome),
    EmptyInbox(R.raw.illustration_empty_inbox),
    EmptyFavorites(R.raw.illustration_empty_favorites),
    EmptyPinned(R.raw.illustration_empty_pinned),
    Search(R.raw.illustration_search),
    NoResults(R.raw.illustration_no_results),
}

@Composable
fun AnimatedIllustration(
    illustration: Illustration,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(illustration.resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    val colorScheme = MaterialTheme.colorScheme
    val dynamicProperties = remember(colorScheme) { themedProperties(colorScheme) }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        dynamicProperties = dynamicProperties,
        modifier = modifier.size(size),
    )
}

@Composable
fun EmptyState(
    illustration: Illustration,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val motion = MaterialTheme.motionScheme
    val visibility = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visibleState = visibility,
            enter = fadeIn(motion.defaultEffectsSpec()) +
                scaleIn(motion.defaultSpatialSpec(), initialScale = INITIAL_SCALE),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = MaxContentWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedIllustration(illustration = illustration)
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (action != null) {
                    Box(modifier = Modifier.padding(top = 12.dp)) { action() }
                }
            }
        }
    }
}

private fun themedProperties(colors: ColorScheme): LottieDynamicProperties {
    val roles = mapOf(
        "primary" to colors.primary,
        "onPrimary" to colors.onPrimary,
        "primaryContainer" to colors.primaryContainer,
        "onPrimaryContainer" to colors.onPrimaryContainer,
        "secondary" to colors.secondary,
        "secondaryContainer" to colors.secondaryContainer,
        "onSecondaryContainer" to colors.onSecondaryContainer,
        "tertiary" to colors.tertiary,
        "tertiaryContainer" to colors.tertiaryContainer,
        "onTertiaryContainer" to colors.onTertiaryContainer,
        "outline" to colors.outline,
    )
    return LottieDynamicProperties(roles.flatMap { (role, color) -> rolePaints(role, color) })
}

private fun rolePaints(role: String, color: Color): List<LottieDynamicProperty<*>> {
    val keyPath = KeyPath("**", role, "**")
    val argb = color.toArgb()
    return listOf(
        LottieDynamicProperty(LottieProperty.COLOR, keyPath, argb),
        LottieDynamicProperty(LottieProperty.STROKE_COLOR, keyPath, argb),
    )
}

private const val INITIAL_SCALE = 0.85f
private val MaxContentWidth = 420.dp
