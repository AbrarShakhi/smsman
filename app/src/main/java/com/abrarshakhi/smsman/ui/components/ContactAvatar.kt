package com.abrarshakhi.smsman.ui.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.abrarshakhi.smsman.domain.model.Contact
import kotlin.math.absoluteValue

@Composable
fun ContactAvatar(
    contact: Contact?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val photoUri = contact?.photoUri
    val initials = (contact?.displayName ?: fallbackLabel).extractInitials()
    val seed = contact?.phoneNumberE164 ?: fallbackLabel
    val container = pickAvatarTint(seed)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container.background),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = contact.displayName,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = container.onBackground,
            )
        }
    }
}

private data class AvatarTint(val background: Color, val onBackground: Color)

@Composable
private fun pickAvatarTint(seed: String): AvatarTint {
    // Five M3-friendly tint pairs derived from the active scheme — never collides with surface,
    // always readable on top of itself.
    val scheme = MaterialTheme.colorScheme
    val choices = listOf(
        AvatarTint(scheme.primaryContainer, scheme.onPrimaryContainer),
        AvatarTint(scheme.secondaryContainer, scheme.onSecondaryContainer),
        AvatarTint(scheme.tertiaryContainer, scheme.onTertiaryContainer),
        AvatarTint(scheme.errorContainer, scheme.onErrorContainer),
        AvatarTint(scheme.surfaceVariant, scheme.onSurfaceVariant),
    )
    return choices[(seed.hashCode().absoluteValue) % choices.size]
}

private fun String.extractInitials(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return "?"
    val parts = trimmed.split(' ', '\t').filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull() ?: '?'}${parts[1].firstOrNull() ?: ' '}".trim().uppercase()
        else -> trimmed.firstOrNull { it.isLetterOrDigit() }?.toString()?.uppercase() ?: "?"
    }
}
