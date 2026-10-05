package com.abrarshakhi.smsman.common.util

import android.os.Build
import com.abrarshakhi.smsman.core.settings.ColorSchemeOption

fun isDynamicColorSchemeSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun defaultColorSchemeOption() = if (isDynamicColorSchemeSupported()) ColorSchemeOption.DYNAMIC
else ColorSchemeOption.BLUE