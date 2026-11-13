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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.util.Locale

private const val PROFILE_STATS_PLACEHOLDER = ".."

@Composable
internal fun ProfileStatsRow(
    stats: UserStatisticsRecord?,
    onAnimeTotalClick: () -> Unit,
    onAnimeTimeClick: (String) -> Unit,
    onMangaTotalClick: () -> Unit,
    onMangaChaptersClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animeLabel = stringResource(R.string.anime)
    val animeTimeLabel = stringResource(R.string.anime_time)
    val mangaLabel = stringResource(R.string.manga)
    val chaptersLabel = stringResource(R.string.chapters)
    val animeTime = formatAnimeTime(stats?.anime?.minutesWatched)
    val mangaChapters = formatMangaChapters(stats?.manga?.chaptersRead)

    Row(modifier = modifier.fillMaxWidth()) {
        ProfileStatsCell(
            label = animeLabel,
            value = stats?.let { formatCount(it.anime.count) } ?: PROFILE_STATS_PLACEHOLDER,
            modifier = Modifier.weight(1f),
            onClick = onAnimeTotalClick,
        )
        ProfileStatsCell(
            label = animeTimeLabel,
            value = animeTime,
            modifier = Modifier.weight(1f),
            onClick = { if (stats != null) onAnimeTimeClick(animeTime) },
        )
        ProfileStatsCell(
            label = mangaLabel,
            value = stats?.let { formatCount(it.manga.count) } ?: PROFILE_STATS_PLACEHOLDER,
            modifier = Modifier.weight(1f),
            onClick = onMangaTotalClick,
        )
        ProfileStatsCell(
            label = chaptersLabel,
            value = mangaChapters,
            modifier = Modifier.weight(1f),
            onClick = { if (stats != null) onMangaChaptersClick(mangaChapters) },
        )
    }
}

@Composable
private fun ProfileStatsCell(
    label: String,
    value: String,
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
            ).padding(vertical = dimensionResource(R.dimen.lg_margin))
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
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun formatAnimeTime(minutesWatched: Int?): String {
    if (minutesWatched == null || minutesWatched < 1) return PROFILE_STATS_PLACEHOLDER

    var itemTime = minutesWatched / 60f
    if (itemTime > 60) {
        itemTime /= 24
        if (itemTime > 365) {
            return stringResource(R.string.anime_time_years, itemTime / 365)
        }
        return stringResource(R.string.anime_time_days, itemTime)
    }
    return stringResource(R.string.anime_time_hours, itemTime)
}

private fun formatMangaChapters(chaptersRead: Int?): String {
    if (chaptersRead == null || chaptersRead < 1) return PROFILE_STATS_PLACEHOLDER
    if (chaptersRead > 1000) {
        return String.format(Locale.getDefault(), "%.1f K", chaptersRead / 1000f)
    }
    return String.format(Locale.getDefault(), "%d", chaptersRead)
}

private fun formatCount(count: Int): String = if (count >= 1000) {
    String.format(Locale.getDefault(), "%.1f K", count / 1000f)
} else {
    String.format(Locale.getDefault(), "%d", count)
}

@Preview(name = "Profile statistics", showBackground = true, widthDp = 360, heightDp = 88)
@Composable
private fun ProfileStatsRowPreview() {
    AniTrendTheme {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer) {
            ProfileStatsRow(
                stats =
                UserStatisticsRecord(
                    anime = UserStatisticsRecord.Anime(count = 5_000, minutesWatched = 300),
                    manga = UserStatisticsRecord.Manga(count = 50, chaptersRead = 200),
                ),
                onAnimeTotalClick = {},
                onAnimeTimeClick = {},
                onMangaTotalClick = {},
                onMangaChaptersClick = {},
            )
        }
    }
}
