package com.mxt.anitrend.adapter.recycler.shared

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.CustomProgress
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

private const val LOAD_STATE_FOOTER_PROGRESS_TEST_TAG = "load-state-footer-progress"

@Composable
internal fun LoadStateFooter(
    state: LoadStateFooterUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.spacing_small)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            state.showLoading -> {
                // Keep the platform small-progress drawable; the Material 3 indeterminate arc is visibly larger.
                AndroidView(
                    factory = { context ->
                        CustomProgress(
                            context = context,
                            attrs = null,
                            defStyleAttr = android.R.attr.progressBarStyleSmall,
                        ).apply {
                            layoutParams =
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                )
                        }
                    },
                    modifier =
                    Modifier
                        .padding(end = dimensionResource(R.dimen.md_margin))
                        .wrapContentSize()
                        .testTag(LOAD_STATE_FOOTER_PROGRESS_TEST_TAG),
                )
                Text(
                    text = stringResource(R.string.Loading),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.md_margin)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            state.showRetry -> {
                Text(
                    text = stringResource(R.string.button_try_again),
                    modifier =
                    Modifier
                        .clickable(role = Role.Button, onClick = onRetry)
                        .padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(name = "Paging footer loading", showBackground = true)
@Composable
private fun LoadStateFooterLoadingPreview() {
    AniTrendTheme {
        Surface {
            LoadStateFooter(
                state = LoadStateFooterUiState(showLoading = true, showRetry = false),
                onRetry = {},
            )
        }
    }
}

@Preview(
    name = "Paging footer retry dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LoadStateFooterRetryPreview() {
    AniTrendTheme {
        Surface {
            LoadStateFooter(
                state = LoadStateFooterUiState(showLoading = false, showRetry = true),
                onRetry = {},
            )
        }
    }
}
