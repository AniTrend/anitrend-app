package com.mxt.anitrend.view.activity.base

import androidx.lifecycle.ViewModel
import com.mxt.anitrend.R
import com.mxt.anitrend.util.Settings

/**
 * Defines the pages shown during first-run onboarding and records completion.
 */
class OnboardingViewModel(
    private val settings: Settings,
) : ViewModel() {

    val pages = listOf(
        OnboardingPage(
            R.string.app_name,
            R.string.description_onboarding_welcome,
            R.layout.layout_onboarding_welcome_hero,
        ),
        OnboardingPage(
            R.string.app_intro_colors_title,
            R.string.description_onboarding_colors,
            R.layout.layout_onboarding_colors_hero,
        ),
        OnboardingPage(
            R.string.app_intro_content_title,
            R.string.description_onboarding_content,
            R.layout.layout_onboarding_content_hero,
        ),
        OnboardingPage(
            R.string.app_intro_search_title,
            R.string.description_onboarding_searching,
            R.layout.layout_onboarding_searching_hero,
        ),
        OnboardingPage(
            R.string.title_onboarding_manage,
            R.string.description_onboarding_manage,
            R.layout.layout_onboarding_manage_hero,
        ),
    )

    /** Marks onboarding as completed so it is not shown on the next launch. */
    fun onPostFreshInstall() {
        settings.isFreshInstall = false
    }

    /** Immutable resources used to render one onboarding page. */
    data class OnboardingPage(
        val titleRes: Int,
        val descriptionRes: Int,
        val heroLayoutRes: Int,
    )
}
