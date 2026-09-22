package com.abrarshakhi.smsman.common.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fallback palette for devices without dynamic color (API 30, or dynamic color turned off).
// Seeded on the Material 3 baseline blue that Google Messages uses.

private val Blue40 = Color(0xFF0B57D0)
private val Blue80 = Color(0xFFA8C7FA)
private val Blue90 = Color(0xFFD3E3FD)
private val Blue10 = Color(0xFF041E49)
private val Blue20 = Color(0xFF062E6F)
private val Blue30 = Color(0xFF0842A0)

private val Teal40 = Color(0xFF00639B)
private val Teal80 = Color(0xFF8ECDFF)
private val Teal90 = Color(0xFFCCE5FF)
private val Teal10 = Color(0xFF001D33)
private val Teal30 = Color(0xFF004A77)

private val Neutral10 = Color(0xFF1A1C1E)
private val Neutral90 = Color(0xFFE2E2E5)
private val Neutral99 = Color(0xFFFDFCFF)

val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Teal80,
    onSecondary = Teal10,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)
