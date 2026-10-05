package com.abrarshakhi.smsman.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon

@Composable
fun ContactAvatar(
    displayName: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val colors = MaterialTheme.colorScheme.avatarColors(colorIndex)
    val shape = avatarShape(colorIndex)
    val initial = remember(displayName) { displayName.trim().firstOrNull { it.isLetter() }?.uppercase() }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(colors.container),
        contentAlignment = Alignment.Center,
    ) {
        if (initial != null) {
            Text(
                text = initial,
                color = colors.content,
                style = if (size >= LargeAvatarSize) {
                    MaterialTheme.typography.titleLargeEmphasized
                } else {
                    MaterialTheme.typography.titleMediumEmphasized
                },
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(size * ICON_FRACTION),
            )
        }
    }
}

private class AvatarColors(val container: Color, val content: Color)

private fun ColorScheme.avatarColors(index: Int): AvatarColors = when (index.mod(AVATAR_PALETTE_SIZE)) {
    0 -> AvatarColors(primaryContainer, onPrimaryContainer)
    1 -> AvatarColors(tertiaryContainer, onTertiaryContainer)
    else -> AvatarColors(secondaryContainer, onSecondaryContainer)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun avatarShape(index: Int): Shape = AvatarPolygons[index.mod(AvatarPolygons.size)].toShape()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val AvatarPolygons: List<RoundedPolygon> = listOf(
    MaterialShapes.Circle,
    MaterialShapes.Cookie6Sided,
    MaterialShapes.Clover4Leaf,
    MaterialShapes.Cookie9Sided,
    MaterialShapes.Sunny,
    MaterialShapes.Cookie4Sided,
    MaterialShapes.Puffy,
    MaterialShapes.SoftBurst,
)

private const val AVATAR_PALETTE_SIZE = 3
private const val ICON_FRACTION = 0.55f
private val LargeAvatarSize = 48.dp
