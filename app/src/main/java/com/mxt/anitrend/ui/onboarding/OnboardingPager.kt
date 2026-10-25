package com.mxt.anitrend.ui.onboarding

import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ItemOnboardingPageBinding
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.view.activity.base.OnboardingViewModel

/** Native pager ownership with the existing, non-list onboarding artwork kept intact. */
@Composable
internal fun OnboardingPager(
    pages: List<OnboardingViewModel.OnboardingPage>,
    advanceRequests: Int,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPageChanged by rememberUpdatedState(onPageChanged)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPageChanged(it) }
    }
    LaunchedEffect(advanceRequests) {
        if (advanceRequests > 0 && pagerState.currentPage < pages.lastIndex) {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }
    HorizontalPager(
        state = pagerState,
        key = { pages[it].titleRes },
        modifier = modifier.fillMaxSize().testTag("onboarding-pager"),
    ) { index ->
        val page = pages[index]
        key(page) {
            AndroidView(
                factory = { context ->
                    ItemOnboardingPageBinding.inflate(LayoutInflater.from(context)).apply {
                        title.setText(page.titleRes)
                        description.setText(page.descriptionRes)
                        LayoutInflater.from(context).inflate(page.heroLayoutRes, heroContainer, true)
                    }.root
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(name = "Onboarding pager", widthDp = 400, heightDp = 600)
@Composable
private fun OnboardingPagerPreview() {
    AniTrendTheme {
        OnboardingPager(
            pages = listOf(OnboardingViewModel.OnboardingPage(R.string.app_name, R.string.description_onboarding_welcome, R.layout.layout_onboarding_welcome_hero)),
            advanceRequests = 0,
            onPageChanged = {},
        )
    }
}
