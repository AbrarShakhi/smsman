package com.abrarshakhi.smsman.data.document

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.abrarshakhi.smsman.model.AppDocument
import com.abrarshakhi.smsman.model.MarkdownBlock
import com.abrarshakhi.smsman.model.MarkdownSpan
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentRepositoryTest {

    private val repository = DocumentRepository(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun everyDocumentIsBundled(): Unit = runBlocking {
        AppDocument.entries.forEach { document ->
            assertTrue("${document.fileName} is empty", repository.load(document).isNotEmpty())
        }
    }

    @Test
    fun markdownDocumentsStartWithATitle(): Unit = runBlocking {
        AppDocument.entries.filter { it.fileName.endsWith(".md") }.forEach { document ->
            val first = repository.load(document).first()
            assertTrue(
                "${document.fileName} does not start with a title",
                first is MarkdownBlock.Heading && first.level == 1,
            )
        }
    }

    @Test
    fun relativeLinksPointToBundledDocuments(): Unit = runBlocking {
        AppDocument.entries.forEach { document ->
            repository.load(document)
                .flatMap { it.spans }
                .mapNotNull(MarkdownSpan::link)
                .filterNot { link -> EXTERNAL_PREFIXES.any(link::startsWith) }
                .forEach { link ->
                    assertNotNull("Broken link $link in ${document.fileName}", AppDocument.fromLink(link))
                }
        }
    }

    private val MarkdownBlock.spans: List<MarkdownSpan>
        get() = when (this) {
            is MarkdownBlock.Heading -> content
            is MarkdownBlock.Paragraph -> content
            is MarkdownBlock.ListItem -> content
            is MarkdownBlock.Quote -> content
            is MarkdownBlock.CodeBlock, MarkdownBlock.Divider -> emptyList()
        }

    private companion object {
        val EXTERNAL_PREFIXES = listOf("https://", "http://", "mailto:")
    }
}
