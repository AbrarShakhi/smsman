package com.abrarshakhi.smsman.common.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.abrarshakhi.smsman.core.settings.AppSettings
import com.abrarshakhi.smsman.core.settings.ColorSchemeOption
import com.abrarshakhi.smsman.core.settings.ThemeMode

@Composable
fun SmsmanTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        settings.colorScheme == ColorSchemeOption.DYNAMIC &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        settings.colorScheme == ColorSchemeOption.DYNAMIC ->
            if (darkTheme) DarkColors else LightColors

        else -> remember(settings.colorScheme, darkTheme) {
            schemeFromSeed(Color(settings.colorScheme.seed!!), darkTheme)
        }
    }

    val typography = remember(settings.font) { typographyFor(fontFamilyFor(settings.font)) }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}

/**
 * Derives a scheme from a seed colour by shifting its lightness, which keeps all twelve options
 * visually consistent without hand-authoring twelve full palettes. Dynamic colour is preferred
 * where available; this covers the explicit choices and API 30.
 */
private fun schemeFromSeed(seed: Color, dark: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    android.graphics.Color.colorToHSV(seed.toArgb(), hsl)
    fun tone(saturation: Float, value: Float) = Color(
        android.graphics.Color.HSVToColor(floatArrayOf(hsl[0], saturation, value)),
    )

    val s = hsl[1]
    return if (dark) {
        darkColorScheme(
            primary = tone(s * 0.55f, 0.95f),
            onPrimary = tone(s, 0.22f),
            primaryContainer = tone(s * 0.9f, 0.40f),
            onPrimaryContainer = tone(s * 0.35f, 1f),
            secondary = tone(s * 0.4f, 0.85f),
            onSecondary = tone(s, 0.20f),
            secondaryContainer = tone(s * 0.8f, 0.35f),
            onSecondaryContainer = tone(s * 0.3f, 0.98f),
            background = tone(s * 0.12f, 0.10f),
            onBackground = tone(s * 0.06f, 0.93f),
            surface = tone(s * 0.12f, 0.10f),
            onSurface = tone(s * 0.06f, 0.93f),
            surfaceVariant = tone(s * 0.18f, 0.24f),
            onSurfaceVariant = tone(s * 0.12f, 0.80f),
            outline = tone(s * 0.15f, 0.55f),
        )
    } else {
        lightColorScheme(
            primary = tone(s, 0.62f),
            onPrimary = Color.White,
            primaryContainer = tone(s * 0.28f, 1f),
            onPrimaryContainer = tone(s, 0.26f),
            secondary = tone(s * 0.65f, 0.55f),
            onSecondary = Color.White,
            secondaryContainer = tone(s * 0.22f, 1f),
            onSecondaryContainer = tone(s, 0.22f),
            background = tone(s * 0.04f, 1f),
            onBackground = tone(s * 0.25f, 0.12f),
            surface = tone(s * 0.04f, 1f),
            onSurface = tone(s * 0.25f, 0.12f),
            surfaceVariant = tone(s * 0.12f, 0.93f),
            onSurfaceVariant = tone(s * 0.30f, 0.32f),
            outline = tone(s * 0.25f, 0.55f),
        )
    }
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(),
)
