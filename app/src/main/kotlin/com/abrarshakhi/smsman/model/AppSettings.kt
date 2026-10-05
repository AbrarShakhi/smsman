package com.abrarshakhi.smsman.model

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ColorSchemeOption(val label: String, val seed: Long?) {
    DYNAMIC("Dynamic", null), BLUE("Blue", 0xFF0B57D0), INDIGO(
        "Indigo", 0xFF3949AB
    ),
    TEAL("Teal", 0xFF00897B), GREEN("Green", 0xFF1E8E3E), LIME("Lime", 0xFF7CB342), AMBER(
        "Amber", 0xFFFFB300
    ),
    ORANGE("Orange", 0xFFE8710A), RED("Red", 0xFFD93025), PINK("Pink", 0xFFD81B60), PURPLE(
        "Purple", 0xFF9334E6
    ),
    BROWN("Brown", 0xFF795548), SLATE("Slate", 0xFF546E7A),
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isDynamicColorSchemeSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun defaultColorSchemeOption(): ColorSchemeOption =
    if (isDynamicColorSchemeSupported()) ColorSchemeOption.DYNAMIC else ColorSchemeOption.BLUE

enum class FontOption(val label: String, val googleFontName: String?) {
    SYSTEM("System default", null), ROBOTO("Roboto", "Roboto"), INTER(
        "Inter", "Inter"
    ),
    OPEN_SANS("Open Sans", "Open Sans"), LATO("Lato", "Lato"), NUNITO(
        "Nunito", "Nunito"
    ),
    POPPINS("Poppins", "Poppins"), MONTSERRAT("Montserrat", "Montserrat"), WORK_SANS(
        "Work Sans", "Work Sans"
    ),
    SOURCE_SANS("Source Sans 3", "Source Sans 3"), MERRIWEATHER(
        "Merriweather", "Merriweather"
    ),
    SPACE_MONO("Space Mono", "Space Mono"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorSchemeOption = defaultColorSchemeOption(),
    val font: FontOption = FontOption.SYSTEM,
    val isLoaded: Boolean = false
)
