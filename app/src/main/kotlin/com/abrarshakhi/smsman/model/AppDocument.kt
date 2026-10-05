package com.abrarshakhi.smsman.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppDocument(val repositoryPath: String) {
    ABOUT("docs/ABOUT.md"),
    TERMS("docs/TERMS.md"),
    PRIVACY("docs/PRIVACY.md"),
    LICENCE("LICENSE"),
    CREDITS("docs/CREDITS.md");

    val fileName: String get() = repositoryPath.substringAfterLast('/')

    val assetPath: String get() = "$ASSET_DIRECTORY/$fileName"

    val webUrl: String get() = ProjectLinks.file(repositoryPath)

    companion object {
        const val ASSET_DIRECTORY = "documents"

        private val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*:")

        fun fromLink(link: String): AppDocument? {
            if (link.startsWith('#') || SCHEME.containsMatchIn(link)) return null
            val fileName = link.substringBefore('#').substringBefore('?').substringAfterLast('/')
            return entries.firstOrNull { it.fileName == fileName }
        }
    }
}
