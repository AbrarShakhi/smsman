package com.abrarshakhi.smsman.common.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.core.settings.FontOption

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/**
 * Downloadable fonts resolve asynchronously through Play Services. Each family declares the
 * platform default as its final fallback, so text still renders if the provider is unavailable or
 * the device is offline.
 */
fun fontFamilyFor(option: FontOption): FontFamily {
    val name = option.googleFontName ?: return FontFamily.Default
    val googleFont = GoogleFont(name)
    // Positional: the parameter names on this overload changed across versions.
    return FontFamily(
        Font(googleFont, provider, FontWeight.Normal),
        Font(googleFont, provider, FontWeight.Medium),
        Font(googleFont, provider, FontWeight.Bold),
    )
}

/** Applies [family] across the Material type scale, preserving every other token. */
fun typographyFor(family: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
}
