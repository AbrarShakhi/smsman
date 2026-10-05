package com.abrarshakhi.smsman.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.abrarshakhi.smsman.model.AppSettings
import com.abrarshakhi.smsman.model.ColorSchemeOption
import com.abrarshakhi.smsman.model.ThemeMode
import com.abrarshakhi.smsman.model.isDynamicColorSchemeSupported
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SmsmanTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    val typography = remember(settings.font) { typographyFor(fontFamilyFor(settings.font)) }
    DynamicMaterialExpressiveTheme(
        seedColor = seedColorFor(settings.colorScheme),
        motionScheme = MotionScheme.expressive(),
        isDark = settings.themeMode.isDark(),
        style = PaletteStyle.Expressive,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        typography = typography,
        animate = settings.isLoaded,
        content = content,
    )
}

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
private fun seedColorFor(option: ColorSchemeOption): Color {
    val context = LocalContext.current
    if (option == ColorSchemeOption.DYNAMIC && isDynamicColorSchemeSupported()) {
        return Color(context.getColor(android.R.color.system_accent1_500))
    }
    return option.seed?.let(::Color) ?: FallbackSeedColor
}

private val FallbackSeedColor = Color(0xFF0B57D0)
