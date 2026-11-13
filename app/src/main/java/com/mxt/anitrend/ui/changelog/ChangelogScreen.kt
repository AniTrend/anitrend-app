package com.mxt.anitrend.ui.changelog

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/** Renders the Changelog destination while the Fragment owns asset loading and Navigation 2. */
@Composable
internal fun ChangelogScreen(
    versionName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    markdownContent: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.spacing_sm)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_emoji_announce),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.spacing_xl)),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
            Text(
                text = stringResource(R.string.text_what_is_new),
                modifier =
                Modifier
                    .padding(top = dimensionResource(R.dimen.spacing_xs))
                    .semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = versionName,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.md_margin)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xs)))
            markdownContent()
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_sm)))
            FilledTonalButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.Dismiss))
            }
        }
    }
}

/** Retains the shared Android Markdown renderer until its Compose equivalent is migrated. */
@Composable
internal fun ChangelogMarkdown(markdown: String?) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            RichMarkdownTextView(context).apply {
                if (markdown != null) richMarkDown(markdown)
            }
        },
    )
}

@Preview(name = "Changelog light", showBackground = true)
@Composable
private fun ChangelogLightPreview() {
    AniTrendTheme {
        ChangelogScreen(
            versionName = "v1.13.1",
            onDismiss = {},
            markdownContent = {
                Text(
                    text = "AniTrend v2.0 Coming Soon\n\nWhat's Changed\n\nAccessibility and update fixes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }
}

@Preview(
    name = "Changelog dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChangelogDarkPreview() {
    AniTrendTheme {
        ChangelogScreen(
            versionName = "v1.13.1",
            onDismiss = {},
            markdownContent = {
                Text(
                    text = "AniTrend v2.0 Coming Soon\n\nWhat's Changed\n\nAccessibility and update fixes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }
}
