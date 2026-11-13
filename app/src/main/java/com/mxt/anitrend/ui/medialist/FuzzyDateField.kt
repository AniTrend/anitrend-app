package com.mxt.anitrend.ui.medialist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun FuzzyDateField(
    value: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = null,
                indication = ripple(color = MaterialTheme.colorScheme.surfaceContainerHighest),
                role = Role.Button,
                onClick = onClick,
            ).semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier =
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = CircleShape,
                )
                .heightIn(min = 48.dp)
                .padding(horizontal = dimensionResource(R.dimen.mg_margin)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                Modifier
                    .width(48.dp)
                    .heightIn(min = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_grey_600_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.lg_margin)))
            Text(
                text = value,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(name = "Fuzzy date fields", showBackground = true)
@Composable
private fun FuzzyDateFieldPreview() {
    AniTrendTheme {
        Column {
            FuzzyDateField(
                value = "TBA",
                contentDescription = "Started at, TBA",
                onClick = {},
            )
            FuzzyDateField(
                value = "2024",
                contentDescription = "Started at, 2024",
                onClick = {},
            )
            FuzzyDateField(
                value = "Jun, 2024",
                contentDescription = "Started at, Jun, 2024",
                onClick = {},
            )
            FuzzyDateField(
                value = "Jun 15, 2024",
                contentDescription = "Started at, Jun 15, 2024",
                onClick = {},
            )
        }
    }
}
