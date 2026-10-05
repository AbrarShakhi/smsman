package com.abrarshakhi.smsman.model

object ProjectLinks {

    const val REPOSITORY = "https://github.com/AbrarShakhi/smsman"
    const val NEW_ISSUE = "$REPOSITORY/issues/new"

    private const val DEFAULT_BRANCH = "main"

    fun file(path: String): String = "$REPOSITORY/blob/$DEFAULT_BRANCH/$path"
}
