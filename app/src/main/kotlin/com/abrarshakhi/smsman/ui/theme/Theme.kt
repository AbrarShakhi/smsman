package com.abrarshakhi.smsman.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.abrarshakhi.smsman.model.AppSettings
import com.abrarshakhi.smsman.model.ColorSchemeOption
import com.abrarshakhi.smsman.model.ColorSchemeOption.DYNAMIC
import com.abrarshakhi.smsman.model.ThemeMode
import com.abrarshakhi.smsman.model.isDynamicColorSchemeSupported


@RequiresApi(Build.VERSION_CODES.S)
private fun dynamicColorScheme(isDarkTheme: Boolean, context: Context) =
    if (isDarkTheme) dynamicDarkColorScheme(context)
    else dynamicLightColorScheme(context)

@Composable
private fun WithColorScheme(
    settingsColorScheme: ColorSchemeOption,
    settingsThemeMode: ThemeMode,
    content: @Composable (colorScheme: ColorScheme) -> Unit
) {
    val isDarkTheme = when (settingsThemeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val dynamicColorScheme =
        if (isDynamicColorSchemeSupported() && settingsColorScheme == DYNAMIC) {
            dynamicColorScheme(isDarkTheme, context)
        } else {
            remember(settingsColorScheme, isDarkTheme) {
                schemeFromSeed(Color(settingsColorScheme.seed!!), isDarkTheme)
            }
        }
    content(dynamicColorScheme)
}

@Composable
fun SmsmanTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    WithColorScheme(
        settings.colorScheme, settings.themeMode
    ) { colorScheme ->
        val typography = remember(settings.font) { typographyFor(fontFamilyFor(settings.font)) }
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}

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
