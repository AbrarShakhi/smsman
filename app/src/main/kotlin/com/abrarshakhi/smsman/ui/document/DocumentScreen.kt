package com.abrarshakhi.smsman.ui.document

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.AppDocument
import com.abrarshakhi.smsman.ui.component.BackNavigationIcon
import com.abrarshakhi.smsman.ui.component.ErrorContent
import com.abrarshakhi.smsman.ui.component.ListPhase
import com.abrarshakhi.smsman.ui.component.LoadingContent
import com.abrarshakhi.smsman.ui.util.openUriSafely
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DocumentRoute(
    document: AppDocument,
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
) {
    DocumentScreen(
        viewModel = koinViewModel { parametersOf(document) },
        document = document,
        onBack = onBack,
        onOpenDocument = onOpenDocument,
    )
}

@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel,
    document: AppDocument,
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val motion = MaterialTheme.motionScheme
    val phase = when {
        state.isLoading -> ListPhase.Loading
        state.isUnavailable -> ListPhase.Error
        else -> ListPhase.Content
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(documentTitle(document)) },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                actions = {
                    IconButton(
                        onClick = { uriHandler.openUriSafely(document.webUrl) },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = stringResource(R.string.document_view_online),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            modifier = Modifier.padding(padding),
        ) { current ->
            when (current) {
                ListPhase.Loading -> LoadingContent()
                ListPhase.Error -> ErrorContent(message = stringResource(R.string.document_unavailable))
                ListPhase.Empty, ListPhase.Content -> MarkdownContent(
                    blocks = state.blocks,
                    onLinkClick = { target -> openLink(target, document, onOpenDocument, uriHandler) },
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                )
            }
        }
    }
}

@Composable
fun documentTitle(document: AppDocument): String = when (document) {
    AppDocument.ABOUT -> stringResource(R.string.document_about, stringResource(R.string.app_name))
    AppDocument.TERMS -> stringResource(R.string.document_terms)
    AppDocument.PRIVACY -> stringResource(R.string.document_privacy)
    AppDocument.LICENCE -> stringResource(R.string.document_licence)
    AppDocument.CREDITS -> stringResource(R.string.document_credits)
}

private fun openLink(
    target: String,
    current: AppDocument,
    onOpenDocument: (AppDocument) -> Unit,
    uriHandler: UriHandler,
) {
    val linked = AppDocument.fromLink(target)
    when {
        linked != null -> if (linked != current) onOpenDocument(linked)
        WEB_SCHEMES.any { target.startsWith(it, ignoreCase = true) } -> uriHandler.openUriSafely(target)
    }
}

private val WEB_SCHEMES = listOf("https://", "http://", "mailto:")
