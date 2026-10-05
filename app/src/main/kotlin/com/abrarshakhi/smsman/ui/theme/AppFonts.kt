package com.abrarshakhi.smsman.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.FontOption

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

fun fontFamilyFor(option: FontOption): FontFamily {
    val name = option.googleFontName ?: return FontFamily.Default
    val googleFont = GoogleFont(name)
    return FontFamily(
        Font(googleFont, provider, FontWeight.Normal),
        Font(googleFont, provider, FontWeight.Medium),
        Font(googleFont, provider, FontWeight.Bold),
    )
}

fun typographyFor(family: FontFamily): Typography {
    val base = Typography()
    fun TextStyle.withFamily(): TextStyle = copy(fontFamily = family)
    return Typography(
        displayLarge = base.displayLarge.withFamily(),
        displayMedium = base.displayMedium.withFamily(),
        displaySmall = base.displaySmall.withFamily(),
        headlineLarge = base.headlineLarge.withFamily(),
        headlineMedium = base.headlineMedium.withFamily(),
        headlineSmall = base.headlineSmall.withFamily(),
        titleLarge = base.titleLarge.withFamily(),
        titleMedium = base.titleMedium.withFamily(),
        titleSmall = base.titleSmall.withFamily(),
        bodyLarge = base.bodyLarge.withFamily(),
        bodyMedium = base.bodyMedium.withFamily(),
        bodySmall = base.bodySmall.withFamily(),
        labelLarge = base.labelLarge.withFamily(),
        labelMedium = base.labelMedium.withFamily(),
        labelSmall = base.labelSmall.withFamily(),
        displayLargeEmphasized = base.displayLargeEmphasized.withFamily(),
        displayMediumEmphasized = base.displayMediumEmphasized.withFamily(),
        displaySmallEmphasized = base.displaySmallEmphasized.withFamily(),
        headlineLargeEmphasized = base.headlineLargeEmphasized.withFamily(),
        headlineMediumEmphasized = base.headlineMediumEmphasized.withFamily(),
        headlineSmallEmphasized = base.headlineSmallEmphasized.withFamily(),
        titleLargeEmphasized = base.titleLargeEmphasized.withFamily(),
        titleMediumEmphasized = base.titleMediumEmphasized.withFamily(),
        titleSmallEmphasized = base.titleSmallEmphasized.withFamily(),
        bodyLargeEmphasized = base.bodyLargeEmphasized.withFamily(),
        bodyMediumEmphasized = base.bodyMediumEmphasized.withFamily(),
        bodySmallEmphasized = base.bodySmallEmphasized.withFamily(),
        labelLargeEmphasized = base.labelLargeEmphasized.withFamily(),
        labelMediumEmphasized = base.labelMediumEmphasized.withFamily(),
        labelSmallEmphasized = base.labelSmallEmphasized.withFamily(),
    )
}
