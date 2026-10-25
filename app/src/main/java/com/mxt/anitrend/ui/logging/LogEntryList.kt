package com.mxt.anitrend.ui.logging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R as AppR
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/**
 * Stateless logging list rendering. The logging ViewModel owns entry filtering and
 * updates; this composable only selects rows and emits their content.
 */
@Composable
internal fun LogEntryList(
    entries: List<LogEntry>,
    modifier: Modifier = Modifier,
) {
    val rowPadding = dimensionResource(AppR.dimen.xl_margin)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(LOG_ENTRY_LIST_TAG),
        contentPadding = PaddingValues(start = rowPadding, end = rowPadding, bottom = rowPadding),
        reverseLayout = true,
        verticalArrangement = Arrangement.Bottom,
    ) {
        itemsIndexed(
            items = entries,
            key = { index, entry -> "log:$index:${entry.level}:${entry.message}" },
            contentType = { _, entry -> entry.level },
        ) { _, entry ->
            val selectionState = remember(entry.level, entry.message) { SelectionState() }
            LogEntryRow(
                level = entry.level,
                message = entry.message,
                selectionState = selectionState,
                modifier = Modifier.testTag(LOG_ENTRY_ROW_TAG),
            )
        }
    }
}

internal const val LOG_ENTRY_LIST_TAG = "log-entry-list"
internal const val LOG_ENTRY_ROW_TAG = "log-entry-row"

@Preview(name = "Log entry list", showBackground = true)
@Composable
private fun LogEntryListPreview() {
    AniTrendTheme {
        Column {
            LogEntryList(
                entries = listOf(
                    LogEntry(date = "2026-01-01", time = "00:00:00.000", level = LogEntry.Level.ERROR, message = "Preview error"),
                    LogEntry(date = "2026-01-01", time = "00:00:01.000", level = LogEntry.Level.INFO, message = "Preview info"),
                ),
            )
        }
    }
}
