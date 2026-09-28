package com.abrarshakhi.smsman.common.util

import android.os.Build

fun isDynamicColorSchemeSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
