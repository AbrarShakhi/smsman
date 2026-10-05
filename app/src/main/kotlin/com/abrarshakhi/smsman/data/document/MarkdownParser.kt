package com.abrarshakhi.smsman.data.document

import com.abrarshakhi.smsman.model.MarkdownBlock
import com.abrarshakhi.smsman.model.MarkdownSpan

object MarkdownParser {

    fun parse(source: String): List<MarkdownBlock> = BlockParser(source.lines()).parse()

    fun parseInline(text: String): List<MarkdownSpan> = InlineParser(text).parse()
}

private val HEADING = Regex("""^ {0,3}(#{1,6})(?:[ \t]+(.*?))?[ \t]*$""")
private val CLOSING_HASHES = Regex("""[ \t]+#+$""")
private val DIVIDER = Regex("""^ {0,3}([-*_])(?:[ \t]*\1){2,}[ \t]*$""")
private val FENCE = Regex("""^ {0,3}(`{3,}|~{3,}).*$""")
private val QUOTE = Regex("""^ {0,3}>[ \t]?(.*)$""")
private val LIST_ITEM = Regex("""^( *)([-*+]|\d{1,9}[.)])[ \t]+(.*)$""")
private val AUTOLINK = Regex("""^(?:https?://|mailto:)\S+$""")
private const val ESCAPABLE = "\\`*_{}[]()#+-.!<>|"

private class BlockParser(private val lines: List<String>) {

    private val blocks = mutableListOf<MarkdownBlock>()
    private val paragraph = mutableListOf<String>()
    private val listIndents = mutableListOf<Int>()
    private var index = 0

    fun parse(): List<MarkdownBlock> {
        while (index < lines.size) {
            val line = lines[index]
            val fence = FENCE.matchEntire(line)
            val heading = HEADING.matchEntire(line)
            val listItem = LIST_ITEM.matchEntire(line)
            when {
                line.isBlank() -> {
                    flushParagraph()
                    index++
                }

                fence != null -> readFence(fence.groupValues[1])
                heading != null -> addHeading(heading)
                DIVIDER.matches(line) -> addBlock(MarkdownBlock.Divider)
                QUOTE.matches(line) -> readQuote()
                listItem != null -> readListItem(listItem)
                else -> {
                    listIndents.clear()
                    paragraph += line
                    index++
                }
            }
        }
        flushParagraph()
        return blocks
    }

    private fun addHeading(match: MatchResult) {
        val text = match.groupValues[2].replace(CLOSING_HASHES, "").trim()
        addBlock(MarkdownBlock.Heading(level = match.groupValues[1].length, content = MarkdownParser.parseInline(text)))
    }

    private fun addBlock(block: MarkdownBlock) {
        flushParagraph()
        listIndents.clear()
        blocks += block
        index++
    }

    private fun readFence(marker: String) {
        flushParagraph()
        listIndents.clear()
        val code = mutableListOf<String>()
        index++
        while (index < lines.size && !isClosingFence(lines[index], marker)) {
            code += lines[index]
            index++
        }
        index++
        blocks += MarkdownBlock.CodeBlock(code.joinToString("\n"))
    }

    private fun isClosingFence(line: String, marker: String): Boolean {
        val trimmed = line.trim()
        return trimmed.length >= marker.length && trimmed.all { it == marker.first() }
    }

    private fun readQuote() {
        flushParagraph()
        listIndents.clear()
        val content = mutableListOf<String>()
        while (index < lines.size) {
            val match = QUOTE.matchEntire(lines[index]) ?: break
            content += match.groupValues[1]
            index++
        }
        blocks += MarkdownBlock.Quote(MarkdownParser.parseInline(joinLines(content)))
    }

    private fun readListItem(match: MatchResult) {
        flushParagraph()
        val depth = depthFor(indent = match.groupValues[1].length)
        val marker = match.groupValues[2]
        val content = mutableListOf(match.groupValues[3])
        index++
        while (index < lines.size && lines[index].isNotBlank() && !startsBlock(lines[index])) {
            content += lines[index]
            index++
        }
        blocks += MarkdownBlock.ListItem(
            depth = depth,
            number = marker.takeIf { it.last() == '.' || it.last() == ')' }?.dropLast(1)?.toIntOrNull(),
            content = MarkdownParser.parseInline(joinLines(content)),
        )
    }

    private fun depthFor(indent: Int): Int {
        while (listIndents.isNotEmpty() && indent < listIndents.last()) {
            listIndents.removeAt(listIndents.lastIndex)
        }
        if (listIndents.isEmpty() || indent > listIndents.last()) listIndents += indent
        return listIndents.lastIndex
    }

    private fun startsBlock(line: String): Boolean =
        FENCE.matches(line) || HEADING.matches(line) || DIVIDER.matches(line) ||
            QUOTE.matches(line) || LIST_ITEM.matches(line)

    private fun flushParagraph() {
        if (paragraph.isEmpty()) return
        blocks += MarkdownBlock.Paragraph(MarkdownParser.parseInline(joinLines(paragraph)))
        paragraph.clear()
    }

    private fun joinLines(source: List<String>): String = buildString {
        source.forEachIndexed { position, line ->
            val isLast = position == source.lastIndex
            val hardBreak = !isLast && (line.endsWith("  ") || line.trimEnd().endsWith('\\'))
            val text = line.trim()
            append(if (hardBreak) text.removeSuffix("\\").trimEnd() else text)
            if (!isLast) append(if (hardBreak) '\n' else ' ')
        }
    }
}

private class InlineParser(private val source: String) {

    private val spans = mutableListOf<MarkdownSpan>()

    fun parse(): List<MarkdownSpan> {
        parseRange(start = 0, end = source.length, link = null, bold = false, italic = false)
        return spans.merged()
    }

    private fun parseRange(start: Int, end: Int, link: String?, bold: Boolean, italic: Boolean) {
        var isBold = bold
        var isItalic = italic
        val buffer = StringBuilder()

        fun flush() {
            if (buffer.isEmpty()) return
            spans += MarkdownSpan(text = buffer.toString(), bold = isBold, italic = isItalic, link = link)
            buffer.clear()
        }

        var position = start
        while (position < end) {
            val char = source[position]
            when {
                char == '\\' && position + 1 < end && source[position + 1] in ESCAPABLE -> {
                    buffer.append(source[position + 1])
                    position += 2
                }

                char == '`' -> {
                    val close = source.indexOf('`', position + 1)
                    if (close > position + 1 && close < end) {
                        flush()
                        spans += MarkdownSpan(
                            text = source.substring(position + 1, close),
                            bold = isBold,
                            italic = isItalic,
                            code = true,
                            link = link,
                        )
                        position = close + 1
                    } else {
                        buffer.append(char)
                        position++
                    }
                }

                char == '[' && link == null -> {
                    val match = linkAt(position, end)
                    if (match != null) {
                        flush()
                        parseRange(position + 1, match.textEnd, match.url, isBold, isItalic)
                        position = match.end
                    } else {
                        buffer.append(char)
                        position++
                    }
                }

                char == '<' && link == null -> {
                    val close = source.indexOf('>', position + 1)
                    val target = if (close in position + 1 until end) source.substring(position + 1, close) else null
                    if (target != null && AUTOLINK.matches(target)) {
                        flush()
                        spans += MarkdownSpan(
                            text = target.removePrefix("mailto:"),
                            bold = isBold,
                            italic = isItalic,
                            link = target,
                        )
                        position = close + 1
                    } else {
                        buffer.append(char)
                        position++
                    }
                }

                char == '*' || char == '_' -> {
                    val run = if (position + 1 < end && source[position + 1] == char) 2 else 1
                    val active = if (run == 2) isBold else isItalic
                    val toggles = if (active) canClose(position, run, end) else canOpen(position, run, end)
                    if (toggles) {
                        flush()
                        if (run == 2) isBold = !isBold else isItalic = !isItalic
                    } else {
                        buffer.append(source, position, position + run)
                    }
                    position += run
                }

                else -> {
                    buffer.append(char)
                    position++
                }
            }
        }
        flush()
    }

    private fun canOpen(position: Int, run: Int, end: Int): Boolean {
        val next = position + run
        if (next >= end || source[next].isWhitespace()) return false
        val char = source[position]
        if (char == '_' && source.getOrNull(position - 1)?.isLetterOrDigit() == true) return false
        return hasClosing(from = next, char = char, run = run, end = end)
    }

    private fun canClose(position: Int, run: Int, end: Int): Boolean {
        val previous = source.getOrNull(position - 1) ?: return false
        if (previous.isWhitespace()) return false
        val next = position + run
        return source[position] != '_' || next >= end || !source[next].isLetterOrDigit()
    }

    private fun hasClosing(from: Int, char: Char, run: Int, end: Int): Boolean {
        var position = from + 1
        while (position + run <= end) {
            val matchesRun = (0 until run).all { source[position + it] == char }
            val isExactRun = run == 2 || (source.getOrNull(position + 1) != char && source[position - 1] != char)
            if (matchesRun && isExactRun && canClose(position, run, end)) return true
            position++
        }
        return false
    }

    private fun linkAt(open: Int, end: Int): LinkMatch? {
        var depth = 0
        var position = open
        while (position < end) {
            when (source[position]) {
                '\\' -> position++
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0) break
                }
            }
            position++
        }
        val textEnd = position
        if (textEnd >= end || textEnd + 1 >= end || source[textEnd + 1] != '(') return null
        val close = source.indexOf(')', textEnd + 2)
        if (close !in textEnd + 2 until end) return null
        val url = source.substring(textEnd + 2, close).trim().substringBefore(' ')
        if (url.isEmpty()) return null
        return LinkMatch(textEnd = textEnd, url = url, end = close + 1)
    }

    private class LinkMatch(val textEnd: Int, val url: String, val end: Int)
}

private fun List<MarkdownSpan>.merged(): List<MarkdownSpan> {
    val result = mutableListOf<MarkdownSpan>()
    for (span in this) {
        val last = result.lastOrNull()
        if (last != null && last.copy(text = span.text) == span) {
            result[result.lastIndex] = last.copy(text = last.text + span.text)
        } else {
            result += span
        }
    }
    return result
}
