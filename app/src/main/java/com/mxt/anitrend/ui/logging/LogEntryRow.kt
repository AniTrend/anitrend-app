package com.mxt.anitrend.ui.logging

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.SelectionState
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.mediaCaptionTextStyle

@Composable
internal fun LogEntryRow(
    level: LogEntry.Level,
    message: String,
    selectionState: SelectionState,
    modifier: Modifier = Modifier,
) {
    val levelColor =
        colorResource(
            when (level) {
                LogEntry.Level.ERROR -> R.color.colorStateRed
                LogEntry.Level.WARNING -> R.color.colorStateOrange
                LogEntry.Level.INFO -> R.color.colorStateBlue
                LogEntry.Level.DEBUG -> R.color.colorAccent
                LogEntry.Level.VERBOSE -> R.color.colorTextGrey
            },
        )
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val messageColor =
        remember(context, configuration) {
            val attributes = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
            try {
                check(attributes.hasValue(0)) { "App theme is missing android:textColorPrimary" }
                Color(attributes.getColor(0, android.graphics.Color.BLACK))
            } finally {
                attributes.recycle()
            }
        }
    val messageStyle =
        MaterialTheme.typography.bodyMedium.copy(
            letterSpacing = 0.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = true),
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.md_margin)),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = level.name,
            modifier =
            Modifier
                .alignByBaseline()
                .padding(end = dimensionResource(R.dimen.lg_margin)),
            style = mediaCaptionTextStyle().copy(fontWeight = FontWeight.Bold),
            color = levelColor,
        )
        SelectionContainer(
            state = selectionState,
            modifier = Modifier.weight(1f).alignByBaseline(),
        ) {
            Text(
                text = message,
                style = messageStyle,
                color = messageColor,
            )
        }
    }
}

@Preview(name = "Log entry levels", showBackground = true)
@Composable
private fun LogEntryRowPreview() {
    AniTrendTheme("light") {
        Column {
            LogEntry.Level.entries.forEach { level ->
                key(level) {
                    LogEntryRow(
                        level = level,
                        message = "${level.name} log line",
                        selectionState = rememberSelectionState(),
                    )
                }
            }
        }
    }
}
