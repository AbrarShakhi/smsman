package com.abrarshakhi.smsman.core.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Seed colours for the generated Material 3 schemes. `DYNAMIC` defers to the wallpaper-derived
 * palette on API 31+ and is the default, matching Messages' Material You behaviour.
 */
enum class ColorSchemeOption(val label: String, val seed: Long?) {
    DYNAMIC("Dynamic (wallpaper)", null),
    BLUE("Blue", 0xFF0B57D0),
    INDIGO("Indigo", 0xFF3949AB),
    TEAL("Teal", 0xFF00897B),
    GREEN("Green", 0xFF1E8E3E),
    LIME("Lime", 0xFF7CB342),
    AMBER("Amber", 0xFFFFB300),
    ORANGE("Orange", 0xFFE8710A),
    RED("Red", 0xFFD93025),
    PINK("Pink", 0xFFD81B60),
    PURPLE("Purple", 0xFF9334E6),
    BROWN("Brown", 0xFF795548),
    SLATE("Slate", 0xFF546E7A),
}

/**
 * Font families offered in settings. These are downloadable Google Fonts, resolved asynchronously
 * through the Play Services font provider; [SYSTEM] uses the platform default and always resolves
 * offline, so it is the fallback for every other entry.
 */
enum class FontOption(val label: String, val googleFontName: String?) {
    SYSTEM("System default", null),
    ROBOTO("Roboto", "Roboto"),
    INTER("Inter", "Inter"),
    OPEN_SANS("Open Sans", "Open Sans"),
    LATO("Lato", "Lato"),
    NUNITO("Nunito", "Nunito"),
    POPPINS("Poppins", "Poppins"),
    MONTSERRAT("Montserrat", "Montserrat"),
    WORK_SANS("Work Sans", "Work Sans"),
    SOURCE_SANS("Source Sans 3", "Source Sans 3"),
    MERRIWEATHER("Merriweather", "Merriweather"),
    SPACE_MONO("Space Mono", "Space Mono"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorSchemeOption = ColorSchemeOption.DYNAMIC,
    val font: FontOption = FontOption.SYSTEM,
)
