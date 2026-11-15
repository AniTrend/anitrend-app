package com.mxt.anitrend.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

internal data class AboutPanelStats(
    val followingCount: String,
    val followersCount: String,
    val favouritesCount: String,
)

@Composable
internal fun AboutPanelStatsRow(
    stats: AboutPanelStats,
    onFollowingClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        AboutPanelStatCell(
            label = stringResource(R.string.following),
            count = stats.followingCount,
            modifier = Modifier.weight(1f),
            onClick = onFollowingClick,
        )
        AboutPanelStatCell(
            label = stringResource(R.string.followers),
            count = stats.followersCount,
            modifier = Modifier.weight(1f),
            onClick = onFollowersClick,
        )
        AboutPanelStatCell(
            label = stringResource(R.string.favourites),
            count = stats.favouritesCount,
            modifier = Modifier.weight(1f),
            onClick = onFavouritesClick,
        )
    }
}

@Composable
private fun AboutPanelStatCell(
    label: String,
    count: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
        modifier
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = dimensionResource(R.dimen.lg_margin))
            .semantics(mergeDescendants = true) {
                contentDescription = label
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.sm_margin)),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(name = "Profile about stats", showBackground = true, widthDp = 360, heightDp = 80)
@Composable
private fun AboutPanelStatsRowPreview() {
    AniTrendTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            AboutPanelStatsRow(
                stats = AboutPanelStats(followingCount = "36", followersCount = "1.2 K", favouritesCount = "0"),
                onFollowingClick = {},
                onFollowersClick = {},
                onFavouritesClick = {},
            )
        }
    }
}
