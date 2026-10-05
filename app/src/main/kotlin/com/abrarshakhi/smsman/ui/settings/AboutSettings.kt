package com.abrarshakhi.smsman.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.AppDocument
import com.abrarshakhi.smsman.model.ProjectLinks
import com.abrarshakhi.smsman.ui.component.ListGroup
import com.abrarshakhi.smsman.ui.component.ListGroupItem
import com.abrarshakhi.smsman.ui.document.documentTitle
import com.abrarshakhi.smsman.ui.util.openUriSafely

@Composable
internal fun AboutGroup(onOpenDocument: (AppDocument) -> Unit) {
    ListGroup(title = stringResource(R.string.settings_about)) {
        AppIdentityItem()
        LinkItem(
            icon = Icons.Rounded.Info,
            title = documentTitle(AppDocument.ABOUT),
            supporting = stringResource(R.string.settings_about_app_summary),
            onClick = { onOpenDocument(AppDocument.ABOUT) },
        )
    }
}

@Composable
internal fun OpenSourceGroup() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appName = stringResource(R.string.app_name)
    val shareText = stringResource(R.string.settings_share_text, appName, ProjectLinks.REPOSITORY)

    ListGroup(title = stringResource(R.string.settings_open_source)) {
        LinkItem(
            icon = Icons.Rounded.Code,
            title = stringResource(R.string.settings_source_code),
            supporting = stringResource(R.string.settings_source_code_summary),
            kind = LinkKind.External,
            onClick = { uriHandler.openUriSafely(ProjectLinks.REPOSITORY) },
        )
        LinkItem(
            icon = Icons.Rounded.Star,
            title = stringResource(R.string.settings_star),
            supporting = stringResource(R.string.settings_star_summary),
            kind = LinkKind.External,
            onClick = { uriHandler.openUriSafely(ProjectLinks.REPOSITORY) },
        )
        LinkItem(
            icon = Icons.Rounded.BugReport,
            title = stringResource(R.string.settings_report_issue),
            supporting = stringResource(R.string.settings_report_issue_summary),
            kind = LinkKind.External,
            onClick = { uriHandler.openUriSafely(ProjectLinks.NEW_ISSUE) },
        )
        LinkItem(
            icon = Icons.Rounded.Share,
            title = stringResource(R.string.settings_share, appName),
            supporting = stringResource(R.string.settings_share_summary),
            kind = LinkKind.Action,
            onClick = { context.shareText(shareText) },
        )
    }
}

@Composable
internal fun LegalGroup(onOpenDocument: (AppDocument) -> Unit) {
    ListGroup(title = stringResource(R.string.settings_legal)) {
        LinkItem(
            icon = Icons.Rounded.Gavel,
            title = documentTitle(AppDocument.TERMS),
            supporting = stringResource(R.string.settings_terms_summary),
            onClick = { onOpenDocument(AppDocument.TERMS) },
        )
        LinkItem(
            icon = Icons.Rounded.PrivacyTip,
            title = documentTitle(AppDocument.PRIVACY),
            supporting = stringResource(R.string.settings_privacy_summary),
            onClick = { onOpenDocument(AppDocument.PRIVACY) },
        )
        LinkItem(
            icon = Icons.Rounded.Copyright,
            title = documentTitle(AppDocument.LICENCE),
            supporting = stringResource(R.string.settings_licence_summary),
            onClick = { onOpenDocument(AppDocument.LICENCE) },
        )
        LinkItem(
            icon = Icons.Rounded.VolunteerActivism,
            title = documentTitle(AppDocument.CREDITS),
            supporting = stringResource(R.string.settings_credits_summary),
            onClick = { onOpenDocument(AppDocument.CREDITS) },
        )
    }
}

@Composable
private fun AppIdentityItem() {
    val context = LocalContext.current
    val iconSize = with(LocalDensity.current) { AppIconSize.roundToPx() }
    val icon = remember(context, iconSize) { context.appIcon(iconSize) }
    val version = remember(context) { context.appVersion() }

    ListGroupItem {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(AppIconSize))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )
                Text(
                    text = stringResource(R.string.settings_version, version.name, version.code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_app_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LinkItem(
    icon: ImageVector,
    title: String,
    supporting: String,
    onClick: () -> Unit,
    kind: LinkKind = LinkKind.Document,
) {
    ListGroupItem(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Box(modifier = Modifier.weight(1f)) {
                SettingTitle(title = title, supporting = supporting)
            }
            val indicator = when (kind) {
                LinkKind.Document -> Icons.AutoMirrored.Rounded.KeyboardArrowRight
                LinkKind.External -> Icons.AutoMirrored.Rounded.OpenInNew
                LinkKind.Action -> null
            }
            if (indicator != null) {
                Icon(
                    imageVector = indicator,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private enum class LinkKind { Document, External, Action }

private class AppVersion(val name: String, val code: Long)

private fun Context.appVersion(): AppVersion {
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    return AppVersion(name = info.versionName.orEmpty(), code = info.longVersionCode)
}

private fun Context.appIcon(sizePx: Int): ImageBitmap =
    packageManager.getApplicationIcon(applicationInfo).toBitmap(sizePx, sizePx).asImageBitmap()

private fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, null))
}

private val AppIconSize = 56.dp
