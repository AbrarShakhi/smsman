package com.abrarshakhi.smsman.data.document

import android.content.Context
import com.abrarshakhi.smsman.model.AppDocument
import com.abrarshakhi.smsman.model.MarkdownBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentRepository(private val context: Context) {

    suspend fun load(document: AppDocument): List<MarkdownBlock> = withContext(Dispatchers.IO) {
        val source = context.assets.open(document.assetPath).bufferedReader().use { it.readText() }
        MarkdownParser.parse(source)
    }
}
