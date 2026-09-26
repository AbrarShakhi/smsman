package com.abrarshakhi.smsman.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.json.Json

fun SnapshotStateList<AppRouteKey>.currentRoute() = lastOrNull()

fun SnapshotStateList<AppRouteKey>.switchTapTo(destination: AppRouteKey) {
    clear()
    add(destination)
}

fun SnapshotStateList<AppRouteKey>.back() {
    removeLastOrNull()
}

fun SnapshotStateList<AppRouteKey>.backOrHome() {
    if (size > 1) {
        removeLastOrNull()
    } else if (lastOrNull() != AppRouteKey.HomeTab) {
        switchTapTo(AppRouteKey.AllMessages)
    }
}


fun SnapshotStateList<AppRouteKey>.navigateTo(destination: AppRouteKey) {
    add(destination)
}

val AppRouteBackStackSaver: Saver<SnapshotStateList<AppRouteKey>, Any> = listSaver(save = { stack ->
    stack.map {
        Json.encodeToString<AppRouteKey>(it)
    }
}, restore = { saved ->
    val routes = saved.mapNotNull { encoded ->
        runCatching {
            Json.decodeFromString<AppRouteKey>(encoded)
        }.getOrNull()
    }

    mutableStateListOf<AppRouteKey>().apply {
        addAll(
            routes.ifEmpty {
                listOf(AppRouteKey.AllMessages)
            })
    }
})

@Composable
fun rememberAppBackStack(
    start: AppRouteKey = AppRouteKey.AllMessages
): SnapshotStateList<AppRouteKey> = rememberSaveable(saver = AppRouteBackStackSaver) {
    mutableStateListOf(start)
}
