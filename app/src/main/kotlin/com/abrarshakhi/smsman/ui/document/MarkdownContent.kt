package com.abrarshakhi.smsman.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.model.MarkdownBlock
import com.abrarshakhi.smsman.model.MarkdownSpan

@Composable
internal fun MarkdownContent(
    blocks: List<MarkdownBlock>,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    SelectionContainer(modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
            itemsIndexed(items = blocks, contentType = { _, block -> block::class }) { index, block ->
                MarkdownBlockView(block = block, isFirst = index == 0, onLinkClick = onLinkClick)
            }
        }
    }
}

@Composable
private fun MarkdownBlockView(block: MarkdownBlock, isFirst: Boolean, onLinkClick: (String) -> Unit) {
    when (block) {
        is MarkdownBlock.Heading -> MarkdownHeading(block = block, isFirst = isFirst, onLinkClick = onLinkClick)
        is MarkdownBlock.Paragraph -> MarkdownText(
            content = block.content,
            onLinkClick = onLinkClick,
            modifier = Modifier.padding(vertical = 6.dp),
        )

        is MarkdownBlock.ListItem -> MarkdownListItem(block = block, onLinkClick = onLinkClick)
        is MarkdownBlock.Quote -> MarkdownQuote(block = block, onLinkClick = onLinkClick)
        is MarkdownBlock.CodeBlock -> MarkdownCode(block = block)
        MarkdownBlock.Divider -> HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun MarkdownHeading(block: MarkdownBlock.Heading, isFirst: Boolean, onLinkClick: (String) -> Unit) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val style = when (block.level) {
        1 -> typography.headlineSmallEmphasized
        2 -> typography.titleLargeEmphasized
        3 -> typography.titleMediumEmphasized
        else -> typography.titleSmallEmphasized
    }
    val spacing = when (block.level) {
        1, 2 -> 28.dp
        3 -> 20.dp
        else -> 16.dp
    }
    MarkdownText(
        content = block.content,
        onLinkClick = onLinkClick,
        modifier = Modifier
            .padding(top = if (isFirst) 0.dp else spacing, bottom = 8.dp)
            .semantics { heading() },
        style = style,
        color = if (block.level <= 2) colors.onSurface else colors.primary,
    )
}

@Composable
private fun MarkdownListItem(block: MarkdownBlock.ListItem, onLinkClick: (String) -> Unit) {
    val style = MaterialTheme.typography.bodyLarge
    val colors = MaterialTheme.colorScheme
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    Row(modifier = Modifier.padding(start = ListIndent * block.depth, top = 4.dp, bottom = 4.dp)) {
        Box(
            modifier = Modifier
                .widthIn(min = MarkerWidth)
                .height(lineHeight)
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            val number = block.number
            if (number != null) {
                Text(text = "$number.", style = style, color = colors.primary)
            } else {
                Box(
                    modifier = Modifier
                        .size(BulletSize)
                        .then(
                            if (block.depth == 0) {
                                Modifier.background(colors.primary, CircleShape)
                            } else {
                                Modifier.border(BulletBorder, colors.primary, CircleShape)
                            },
                        ),
                )
            }
        }
        MarkdownText(content = block.content, onLinkClick = onLinkClick, modifier = Modifier.weight(1f), style = style)
    }
}

@Composable
private fun MarkdownQuote(block: MarkdownBlock.Quote, onLinkClick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .width(QuoteBarWidth)
                .fillMaxHeight()
                .background(colors.tertiary, CircleShape),
        )
        MarkdownText(
            content = block.content,
            onLinkClick = onLinkClick,
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun MarkdownCode(block: MarkdownBlock.CodeBlock) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = block.code,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            softWrap = false,
        )
    }
}

@Composable
private fun MarkdownText(
    content: List<MarkdownSpan>,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = rememberMarkdownText(content = content, onLinkClick = onLinkClick),
        modifier = modifier,
        style = style,
        color = color,
    )
}

@Composable
private fun rememberMarkdownText(content: List<MarkdownSpan>, onLinkClick: (String) -> Unit): AnnotatedString {
    val colors = MaterialTheme.colorScheme
    val currentOnLinkClick by rememberUpdatedState(onLinkClick)
    return remember(content, colors) {
        buildAnnotatedString {
            content.forEach { span -> appendSpan(span, colors) { target -> currentOnLinkClick(target) } }
        }
    }
}

private fun AnnotatedString.Builder.appendSpan(
    span: MarkdownSpan,
    colors: ColorScheme,
    onLinkClick: (String) -> Unit,
) {
    val style = SpanStyle(
        fontWeight = if (span.bold) FontWeight.Bold else null,
        fontStyle = if (span.italic) FontStyle.Italic else null,
        fontFamily = if (span.code) FontFamily.Monospace else null,
        background = if (span.code) colors.surfaceContainerHighest else Color.Unspecified,
    )
    val target = span.link
    if (target == null) {
        withStyle(style) { append(span.text) }
        return
    }
    val link = LinkAnnotation.Clickable(
        tag = target,
        styles = TextLinkStyles(style = SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline)),
        linkInteractionListener = { onLinkClick(target) },
    )
    withLink(link) { withStyle(style) { append(span.text) } }
}

private val ListIndent = 20.dp
private val MarkerWidth = 24.dp
private val BulletSize = 6.dp
private val BulletBorder = 1.5.dp
private val QuoteBarWidth = 4.dp
