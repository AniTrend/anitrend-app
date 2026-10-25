package com.mxt.anitrend.view.activity.base

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.mxt.anitrend.R
import com.mxt.anitrend.util.Settings

/** Stable identity of one onboarding tour page. */
enum class OnboardingPageId {
    DISCOVER,
    TRACK,
    SOCIAL,
    START,
}

/**
 * Defines the four pages shown during first-run onboarding and records
 * completion.
 *
 * Pages are immutable resource definitions owned by this ViewModel; the
 * Compose screen keeps only the current page index and completion state in
 * saveable state.
 */
class OnboardingViewModel(
    private val settings: Settings,
) : ViewModel() {

    val pages =
        listOf(
            OnboardingPage(
                id = OnboardingPageId.DISCOVER,
                titleRes = R.string.title_onboarding_discover,
                descriptionRes = R.string.description_onboarding_discover,
            ),
            OnboardingPage(
                id = OnboardingPageId.TRACK,
                titleRes = R.string.title_onboarding_track,
                descriptionRes = R.string.description_onboarding_track,
            ),
            OnboardingPage(
                id = OnboardingPageId.SOCIAL,
                titleRes = R.string.title_onboarding_social,
                descriptionRes = R.string.description_onboarding_social,
            ),
            OnboardingPage(
                id = OnboardingPageId.START,
                titleRes = R.string.title_onboarding_start,
                descriptionRes = R.string.description_onboarding_start,
            ),
        )

    /** Marks onboarding as completed so it is not shown on the next launch. */
    fun onPostFreshInstall() {
        settings.isFreshInstall = false
    }

    /** Immutable resources used to render one onboarding page. */
    data class OnboardingPage(
        val id: OnboardingPageId,
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
    )
}
