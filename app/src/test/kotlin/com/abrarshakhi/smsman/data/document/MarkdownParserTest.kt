package com.abrarshakhi.smsman.data.document

import com.abrarshakhi.smsman.model.MarkdownBlock
import com.abrarshakhi.smsman.model.MarkdownSpan
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun headingsKeepTheirLevelWithoutClosingHashes() {
        assertEquals(
            listOf(
                MarkdownBlock.Heading(level = 1, content = listOf(MarkdownSpan("Title"))),
                MarkdownBlock.Heading(level = 3, content = listOf(MarkdownSpan("Section"))),
            ),
            MarkdownParser.parse("# Title\n### Section ###"),
        )
    }

    @Test
    fun wrappedParagraphLinesAreJoined() {
        assertEquals(
            listOf(MarkdownBlock.Paragraph(listOf(MarkdownSpan("First line second line.")))),
            MarkdownParser.parse("First line\nsecond line."),
        )
    }

    @Test
    fun blankLinesSeparateParagraphs() {
        assertEquals(
            listOf(
                MarkdownBlock.Paragraph(listOf(MarkdownSpan("One"))),
                MarkdownBlock.Paragraph(listOf(MarkdownSpan("Two"))),
            ),
            MarkdownParser.parse("One\n\nTwo"),
        )
    }

    @Test
    fun trailingSpacesForceALineBreak() {
        assertEquals(
            listOf(MarkdownBlock.Paragraph(listOf(MarkdownSpan("Name\nStreet")))),
            MarkdownParser.parse("Name  \nStreet"),
        )
    }

    @Test
    fun bulletAndNumberedLinesBecomeListItems() {
        assertEquals(
            listOf(
                MarkdownBlock.ListItem(depth = 0, number = null, content = listOf(MarkdownSpan("Alpha"))),
                MarkdownBlock.ListItem(depth = 0, number = null, content = listOf(MarkdownSpan("Beta"))),
                MarkdownBlock.ListItem(depth = 0, number = 1, content = listOf(MarkdownSpan("One"))),
                MarkdownBlock.ListItem(depth = 0, number = 2, content = listOf(MarkdownSpan("Two"))),
            ),
            MarkdownParser.parse("- Alpha\n* Beta\n\n1. One\n2) Two"),
        )
    }

    @Test
    fun indentedItemsAreNested() {
        val depths = MarkdownParser.parse("- Parent\n  - Child\n    - Grandchild\n- Sibling")
            .filterIsInstance<MarkdownBlock.ListItem>()
            .map(MarkdownBlock.ListItem::depth)

        assertEquals(listOf(0, 1, 2, 0), depths)
    }

    @Test
    fun wrappedListItemLinesBelongToTheItem() {
        assertEquals(
            listOf(
                MarkdownBlock.ListItem(depth = 0, number = null, content = listOf(MarkdownSpan("First part second part"))),
                MarkdownBlock.ListItem(depth = 0, number = null, content = listOf(MarkdownSpan("Next"))),
            ),
            MarkdownParser.parse("- First part\n  second part\n- Next"),
        )
    }

    @Test
    fun quotesDividersAndCodeBlocksAreRecognised() {
        assertEquals(
            listOf(
                MarkdownBlock.Quote(listOf(MarkdownSpan("Quoted text"))),
                MarkdownBlock.Divider,
                MarkdownBlock.CodeBlock("val answer = 42\n# not a heading"),
            ),
            MarkdownParser.parse("> Quoted\n> text\n\n---\n\n```kotlin\nval answer = 42\n# not a heading\n```"),
        )
    }

    @Test
    fun emphasisAndCodeSpansCarryTheirStyles() {
        assertEquals(
            listOf(
                MarkdownSpan("Bold", bold = true),
                MarkdownSpan(" and "),
                MarkdownSpan("italic", italic = true),
                MarkdownSpan(" with "),
                MarkdownSpan("code", code = true),
            ),
            MarkdownParser.parseInline("**Bold** and _italic_ with `code`"),
        )
    }

    @Test
    fun linksKeepTheirTargetAndStyles() {
        assertEquals(
            listOf(
                MarkdownSpan("See "),
                MarkdownSpan("the ", link = "PRIVACY.md"),
                MarkdownSpan("policy", bold = true, link = "PRIVACY.md"),
                MarkdownSpan(" or "),
                MarkdownSpan("https://example.com", link = "https://example.com"),
                MarkdownSpan("."),
            ),
            MarkdownParser.parseInline("See [the **policy**](PRIVACY.md) or <https://example.com>."),
        )
    }

    @Test
    fun underscoresInsideWordsAreLiteral() {
        assertEquals(listOf(MarkdownSpan("snake_case_name")), MarkdownParser.parseInline("snake_case_name"))
    }

    @Test
    fun unmatchedDelimitersAreLiteral() {
        assertEquals(listOf(MarkdownSpan("5 * 3 and **open")), MarkdownParser.parseInline("5 * 3 and **open"))
    }

    @Test
    fun escapedCharactersAreLiteral() {
        assertEquals(listOf(MarkdownSpan("*not italic*")), MarkdownParser.parseInline("\\*not italic\\*"))
    }
}
