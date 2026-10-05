package com.abrarshakhi.smsman.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AnimatedTabIcon(
    icon: ImageVector,
    label: String,
    selected: Boolean,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(selected) {
        if (selected) {
            rotation.snapTo(0f)
            rotation.animateTo(
                targetValue = 360f, animationSpec = tween(
                    durationMillis = 500, easing = FastOutSlowInEasing
                )
            )
        }
    }
    Icon(
        imageVector = icon, contentDescription = label, modifier = Modifier.graphicsLayer {
            rotationY = rotation.value
            scaleX = 1f
            scaleY = 1f
        })
}
