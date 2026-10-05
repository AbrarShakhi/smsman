package com.abrarshakhi.smsman.model

sealed interface MarkdownBlock {

    data class Heading(val level: Int, val content: List<MarkdownSpan>) : MarkdownBlock

    data class Paragraph(val content: List<MarkdownSpan>) : MarkdownBlock

    data class ListItem(val depth: Int, val number: Int?, val content: List<MarkdownSpan>) : MarkdownBlock

    data class Quote(val content: List<MarkdownSpan>) : MarkdownBlock

    data class CodeBlock(val code: String) : MarkdownBlock

    data object Divider : MarkdownBlock
}

data class MarkdownSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: String? = null,
)
