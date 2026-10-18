package com.mxt.anitrend.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/** Centered scrollable tabs matching the legacy TabLayout's tabGravity and label padding. */
@Composable
internal fun ProfileSectionTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val overviewLabel = stringResource(R.string.profile_section_overview)
    val progressLabel = stringResource(R.string.profile_section_media)
    val statusLabel = stringResource(R.string.profile_section_text)
    val labels =
        remember(overviewLabel, progressLabel, statusLabel) {
            arrayOf(overviewLabel, progressLabel, statusLabel)
        }
    val labelStyle =
        MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Normal,
        )
    val textMeasurer = rememberTextMeasurer()
    val labelWidths =
        remember(labels, labelStyle, textMeasurer) {
            IntArray(labels.size) { index ->
                textMeasurer.measure(AnnotatedString(labels[index]), style = labelStyle).size.width
            }
        }
    val density = LocalDensity.current
    val horizontalPadding = dimensionResource(R.dimen.mg_margin)
    val tabWidth =
        with(density) {
            (maxOf(labelWidths[0], labelWidths[1], labelWidths[2]).toDp() + horizontalPadding * 2)
                .coerceAtLeast(48.dp)
        }
    val selectedLabelWidth = with(density) { labelWidths[selectedTabIndex].toDp() }
    val indicatorWidth =
        (selectedLabelWidth - dimensionResource(R.dimen.md_margin)).coerceAtLeast(0.dp)

    BoxWithConstraints(modifier = modifier) {
        val tabHeight = maxHeight
        val edgePadding = ((maxWidth - tabWidth * labels.size.toFloat()) / 2f).coerceAtLeast(0.dp)

        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            edgePadding = edgePadding,
            minTabWidth = 0.dp,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                    width = indicatorWidth,
                )
            },
        ) {
            labels.forEachIndexed { index, label ->
                Box(
                    modifier =
                    Modifier
                        .width(tabWidth)
                        .height(tabHeight)
                        .selectable(
                            selected = selectedTabIndex == index,
                            onClick = { onTabSelected(index) },
                            role = Role.Tab,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        style = labelStyle,
                        color =
                        if (selectedTabIndex == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Preview(name = "Profile section tabs", showBackground = true, widthDp = 360, heightDp = 56)
@Composable
private fun ProfileSectionTabsPreview() {
    AniTrendTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            ProfileSectionTabs(
                selectedTabIndex = 0,
                onTabSelected = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
