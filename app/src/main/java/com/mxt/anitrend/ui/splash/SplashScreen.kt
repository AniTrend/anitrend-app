/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.viewmodel.StartupViewModel.StartupUiState

/**
 * Compose splash surface: the retained wordmark, provider line, giphy
 * credit and progress indicator, plus the migration-failure
 * acknowledge-and-exit dialog.
 */
@Composable
internal fun SplashContent(
    state: StartupUiState,
    onMigrationFailureAcknowledged: () -> Unit,
) {
    when (state) {
        StartupUiState.Initialising -> SplashScreen()
        is StartupUiState.Ready -> Unit
        StartupUiState.MigrationFailed -> MigrationFailedDialog(onAcknowledge = onMigrationFailureAcknowledged)
    }
}

@Composable
private fun SplashScreen() {
    val isLightTheme = MaterialTheme.colorScheme.surface.luminance() >= 0.5f
    Box(modifier = Modifier.fillMaxSize()) {
        SplashColorStrip(
            modifier =
            Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Text(
                    text = "Ani",
                    fontSize = 55.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Trend",
                    fontSize = 55.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.colorStateBlue),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.app_provider),
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Image(
            painter =
            painterResource(
                if (isLightTheme) {
                    R.drawable.powered_by_giphy_dark
                } else {
                    R.drawable.powered_by_giphy_light
                },
            ),
            contentDescription = null,
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .height(50.dp),
        )

        LinearProgressIndicator(
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .fillMaxWidth(0.6f),
        )
    }
}

@Composable
private fun SplashColorStrip(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SplashColorSquare(colorResource(R.color.colorStateGreen))
        SplashColorSquare(colorResource(R.color.colorStateBlue))
        SplashColorSquare(colorResource(R.color.colorStateOrange))
        SplashColorSquare(colorResource(R.color.colorStateRed))
    }
}

@Composable
private fun SplashColorSquare(color: Color) {
    Box(
        modifier =
        Modifier
            .size(12.dp)
            .background(color, RoundedCornerShape(2.dp)),
    )
}

@Composable
private fun MigrationFailedDialog(onAcknowledge: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(R.string.title_migration_failed)) },
        text = { Text(text = stringResource(R.string.text_migration_failed)) },
        confirmButton = {
            TextButton(onClick = onAcknowledge) {
                Text(text = stringResource(R.string.Ok))
            }
        },
    )
}
