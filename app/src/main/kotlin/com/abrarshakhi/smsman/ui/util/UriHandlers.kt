package com.abrarshakhi.smsman.ui.util

import android.util.Log
import androidx.compose.ui.platform.UriHandler

private const val TAG = "UriHandlers"

fun UriHandler.openUriSafely(uri: String) {
    try {
        openUri(uri)
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "No activity can open $uri", e)
    }
}
