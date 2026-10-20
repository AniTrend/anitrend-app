package com.mxt.anitrend.ui.profile

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/**
 * In-screen profile section selection chips: a FilterChip per existing section
 * with a checkmark on the selected chip. These are section selectors, never a
 * global navigation tab strip.
 */
@Composable
internal fun ProfileSectionTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels =
        listOf(
            stringResource(R.string.profile_section_overview),
            stringResource(R.string.profile_section_media),
            stringResource(R.string.profile_section_text),
        )
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                label = { Text(text = label) },
                leadingIcon =
                if (selectedTabIndex == index) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_circle_white_24dp),
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Preview(name = "Profile section chips", showBackground = true, widthDp = 360, heightDp = 56)
@Composable
private fun ProfileSectionTabsPreview() {
    AniTrendTheme("light") {
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
