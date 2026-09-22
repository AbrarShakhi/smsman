package com.abrarshakhi.smsman.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Deterministic palette so a conversation keeps the same colour across restarts, as Messages does.
 * Addresses are frequently alphanumeric carrier shortcodes ("GP Combo"), so the initial has to
 * cope with letters, digits and non-Latin scripts alike.
 */
private val AvatarColors = listOf(
    Color(0xFF1A73E8), Color(0xFF12B5CB), Color(0xFF1E8E3E), Color(0xFFE37400),
    Color(0xFFD93025), Color(0xFF9334E6), Color(0xFF3949AB), Color(0xFF00897B),
)

@Composable
fun ContactAvatar(
    displayName: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Int = 48,
) {
    val color = AvatarColors[colorIndex.mod(AvatarColors.size)]
    val initial = displayName.trim().firstOrNull()?.uppercase() ?: "?"

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
