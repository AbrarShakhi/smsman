package com.abrarshakhi.smsman.model

data class ThreadTitle(
    val title: String,
    val subtitle: String?,
) {
    companion object {
        val Unknown = ThreadTitle(title = "Conversation", subtitle = null)
    }
}
