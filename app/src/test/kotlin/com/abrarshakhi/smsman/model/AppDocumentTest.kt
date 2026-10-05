package com.abrarshakhi.smsman.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppDocumentTest {

    @Test
    fun relativeLinksResolveToDocuments() {
        assertEquals(AppDocument.TERMS, AppDocument.fromLink("TERMS.md"))
        assertEquals(AppDocument.LICENCE, AppDocument.fromLink("../LICENSE"))
        assertEquals(AppDocument.PRIVACY, AppDocument.fromLink("./PRIVACY.md#contact"))
        assertEquals(AppDocument.ABOUT, AppDocument.fromLink("docs/ABOUT.md"))
    }

    @Test
    fun externalAndUnknownLinksDoNotResolve() {
        assertNull(AppDocument.fromLink("https://github.com/AbrarShakhi/smsman/blob/main/docs/TERMS.md"))
        assertNull(AppDocument.fromLink("mailto:support@example.com"))
        assertNull(AppDocument.fromLink("#summary"))
        assertNull(AppDocument.fromLink("../README.md"))
    }

    @Test
    fun documentsAreAddressedByFileName() {
        assertEquals("documents/PRIVACY.md", AppDocument.PRIVACY.assetPath)
        assertEquals("documents/LICENSE", AppDocument.LICENCE.assetPath)
        assertEquals("https://github.com/AbrarShakhi/smsman/blob/main/docs/PRIVACY.md", AppDocument.PRIVACY.webUrl)
    }
}
