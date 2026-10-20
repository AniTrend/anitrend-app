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
package com.mxt.anitrend.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.base.OnboardingPageId
import com.mxt.anitrend.view.activity.base.OnboardingViewModel.OnboardingPage

/**
 * Four-page first-run onboarding tour.
 *
 * The pager keeps its page across configuration and process recreation.
 * Pages 1-3 offer Next with Previous from page 2; page 4 offers the
 * AniList connection and the guest continuation. All controls keep at
 * least 48dp targets. On short heights the infographic is dropped first
 * and the page content scrolls above the fixed bottom controls.
 */
@Composable
internal fun OnboardingScreen(
    pages: List<OnboardingPage>,
    isAuthenticated: Boolean,
    showPrivacyChoices: Boolean,
    onOpenPrivacyChoices: () -> Unit,
    onConnectAniList: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0f,
            pageCount = { pages.size },
        )
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        Text(
            text =
            stringResource(
                R.string.label_onboarding_page_indicator,
                pagerState.currentPage + 1,
                pages.size,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
        )

        if (showPrivacyChoices) {
            TextButton(
                onClick = onOpenPrivacyChoices,
                modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_onboarding_privacy_choices))
            }
        }
        if (isLastPage) {
            StartPageControls(
                isAuthenticated = isAuthenticated,
                onPrevious = { pagerState.requestScrollToPage(pagerState.currentPage - 1) },
                onConnectAniList = onConnectAniList,
                onContinueAsGuest = onContinueAsGuest,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            TourPageControls(
                showPrevious = pagerState.currentPage > 0,
                onPrevious = { pagerState.requestScrollToPage(pagerState.currentPage - 1) },
                onNext = { pagerState.requestScrollToPage(pagerState.currentPage + 1) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val title = stringResource(page.titleRes)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // The decoration is the first thing dropped on short heights.
        val showDecoration = maxHeight >= 420.dp
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showDecoration) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    OnboardingInfographic(
                        pageId = page.id,
                        contentDescription = title,
                        modifier = Modifier.size(width = 280.dp, height = 180.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * One themed infographic per page inside a 280 x 180dp canvas. The shapes
 * are decorative and non-focusable; the single meaning description carries
 * the page title.
 */
@Composable
private fun OnboardingInfographic(
    pageId: OnboardingPageId,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Canvas(
        modifier =
        modifier.semantics {
            this.contentDescription = contentDescription
        },
    ) {
        when (pageId) {
            OnboardingPageId.DISCOVER -> drawDiscoverInfographic(colors)
            OnboardingPageId.TRACK -> drawTrackInfographic(colors)
            OnboardingPageId.SOCIAL -> drawSocialInfographic(colors)
            OnboardingPageId.START -> drawStartInfographic(colors)
        }
    }
}

@Composable
private fun TourPageControls(
    showPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPrevious) {
            TextButton(
                onClick = onPrevious,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_onboarding_previous))
            }
        } else {
            Spacer(modifier = Modifier.size(1.dp))
        }
        Button(
            onClick = onNext,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.action_onboarding_next))
        }
    }
}

@Composable
private fun StartPageControls(
    isAuthenticated: Boolean,
    onPrevious: () -> Unit,
    onConnectAniList: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = onPrevious,
            modifier =
            Modifier
                .align(Alignment.Start)
                .heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.action_onboarding_previous))
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (isAuthenticated) {
            // An existing session continues directly; Connect is replaced.
            Button(
                onClick = onContinueAsGuest,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_onboarding_continue))
            }
        } else {
            OutlinedButton(
                onClick = onConnectAniList,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_onboarding_connect))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onContinueAsGuest,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_onboarding_continue_as_guest))
            }
        }
    }
}

// ── infographic drawings (all decorative, theme-token colored) ──

/** Search outline, two media chips and two poster rectangles. */
private fun DrawScope.drawDiscoverInfographic(colors: ColorScheme) {
    val stroke = 3.dp.toPx()
    val corner = CornerRadius(12.dp.toPx())

    // Search outline.
    drawCircle(
        color = colors.outline,
        radius = 44.dp.toPx(),
        center = Offset(size.width / 2f, size.height / 2f - 8.dp.toPx()),
        style = Stroke(width = stroke),
    )
    drawLine(
        color = colors.outline,
        start = Offset(size.width / 2f + 26.dp.toPx(), size.height / 2f + 16.dp.toPx()),
        end = Offset(size.width / 2f + 48.dp.toPx(), size.height / 2f + 38.dp.toPx()),
        strokeWidth = stroke,
    )

    // Two media chips.
    drawRoundRect(
        color = colors.secondaryContainer,
        topLeft = Offset(16.dp.toPx(), 24.dp.toPx()),
        size = Size(96.dp.toPx(), 32.dp.toPx()),
        cornerRadius = corner,
    )
    drawRoundRect(
        color = colors.secondaryContainer,
        topLeft = Offset(size.width - 112.dp.toPx(), size.height - 56.dp.toPx()),
        size = Size(96.dp.toPx(), 32.dp.toPx()),
        cornerRadius = corner,
    )

    // Two poster rectangles.
    drawRoundRect(
        color = colors.surfaceVariant,
        topLeft = Offset(size.width - 88.dp.toPx(), 20.dp.toPx()),
        size = Size(56.dp.toPx(), 76.dp.toPx()),
        cornerRadius = CornerRadius(8.dp.toPx()),
    )
    drawRoundRect(
        color = colors.surfaceVariant,
        topLeft = Offset(32.dp.toPx(), size.height - 100.dp.toPx()),
        size = Size(56.dp.toPx(), 76.dp.toPx()),
        cornerRadius = CornerRadius(8.dp.toPx()),
    )
}

/** One list card, one Airing preset pill and a progress stepper. */
private fun DrawScope.drawTrackInfographic(colors: ColorScheme) {
    val corner = CornerRadius(12.dp.toPx())

    // List card with rows.
    drawRoundRect(
        color = colors.surfaceVariant,
        topLeft = Offset(32.dp.toPx(), 16.dp.toPx()),
        size = Size(216.dp.toPx(), 104.dp.toPx()),
        cornerRadius = corner,
    )
    drawCircle(
        color = colors.primary,
        radius = 8.dp.toPx(),
        center = Offset(52.dp.toPx(), 40.dp.toPx()),
    )
    drawRoundRect(
        color = colors.outline,
        topLeft = Offset(68.dp.toPx(), 34.dp.toPx()),
        size = Size(120.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )
    drawRoundRect(
        color = colors.outlineVariant,
        topLeft = Offset(68.dp.toPx(), 58.dp.toPx()),
        size = Size(88.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )
    drawRoundRect(
        color = colors.outlineVariant,
        topLeft = Offset(68.dp.toPx(), 82.dp.toPx()),
        size = Size(104.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )

    // Airing preset pill.
    drawRoundRect(
        color = colors.tertiaryContainer,
        topLeft = Offset(32.dp.toPx(), 132.dp.toPx()),
        size = Size(88.dp.toPx(), 32.dp.toPx()),
        cornerRadius = CornerRadius(16.dp.toPx()),
    )

    // Progress stepper.
    val stepperY = 148.dp.toPx()
    drawLine(
        color = colors.outlineVariant,
        start = Offset(148.dp.toPx(), stepperY),
        end = Offset(248.dp.toPx(), stepperY),
        strokeWidth = 4.dp.toPx(),
    )
    (0..2).forEach { index ->
        drawCircle(
            color = if (index == 0) colors.primary else colors.outlineVariant,
            radius = 8.dp.toPx(),
            center = Offset(148.dp.toPx() + index * 50.dp.toPx(), stepperY),
        )
    }
}

/** One activity card with one reply line. */
private fun DrawScope.drawSocialInfographic(colors: ColorScheme) {
    val corner = CornerRadius(12.dp.toPx())

    // Activity card.
    drawRoundRect(
        color = colors.surfaceVariant,
        topLeft = Offset(32.dp.toPx(), 20.dp.toPx()),
        size = Size(216.dp.toPx(), 80.dp.toPx()),
        cornerRadius = corner,
    )
    drawCircle(
        color = colors.primary,
        radius = 14.dp.toPx(),
        center = Offset(56.dp.toPx(), 48.dp.toPx()),
    )
    drawRoundRect(
        color = colors.outline,
        topLeft = Offset(80.dp.toPx(), 38.dp.toPx()),
        size = Size(136.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )
    drawRoundRect(
        color = colors.outlineVariant,
        topLeft = Offset(80.dp.toPx(), 60.dp.toPx()),
        size = Size(96.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )

    // One reply line indented under the card.
    drawRoundRect(
        color = colors.secondaryContainer,
        topLeft = Offset(56.dp.toPx(), 112.dp.toPx()),
        size = Size(192.dp.toPx(), 36.dp.toPx()),
        cornerRadius = corner,
    )
    drawCircle(
        color = colors.tertiary,
        radius = 8.dp.toPx(),
        center = Offset(72.dp.toPx(), 130.dp.toPx()),
    )
    drawRoundRect(
        color = colors.outlineVariant,
        topLeft = Offset(88.dp.toPx(), 124.dp.toPx()),
        size = Size(120.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )
}

/** One start node branching to two simple choice cards. */
private fun DrawScope.drawStartInfographic(colors: ColorScheme) {
    val corner = CornerRadius(12.dp.toPx())

    // Start node.
    drawCircle(
        color = colors.primary,
        radius = 20.dp.toPx(),
        center = Offset(48.dp.toPx(), size.height / 2f),
    )
    drawCircle(
        color = colors.onPrimary,
        radius = 8.dp.toPx(),
        center = Offset(48.dp.toPx(), size.height / 2f),
    )

    // Branching connectors.
    drawLine(
        color = colors.outlineVariant,
        start = Offset(68.dp.toPx(), size.height / 2f),
        end = Offset(120.dp.toPx(), 44.dp.toPx()),
        strokeWidth = 3.dp.toPx(),
    )
    drawLine(
        color = colors.outlineVariant,
        start = Offset(68.dp.toPx(), size.height / 2f),
        end = Offset(120.dp.toPx(), size.height - 44.dp.toPx()),
        strokeWidth = 3.dp.toPx(),
    )

    // Two choice cards.
    drawRoundRect(
        color = colors.surfaceVariant,
        topLeft = Offset(120.dp.toPx(), 20.dp.toPx()),
        size = Size(136.dp.toPx(), 48.dp.toPx()),
        cornerRadius = corner,
    )
    drawRoundRect(
        color = colors.secondaryContainer,
        topLeft = Offset(120.dp.toPx(), size.height - 68.dp.toPx()),
        size = Size(136.dp.toPx(), 48.dp.toPx()),
        cornerRadius = corner,
    )
}
