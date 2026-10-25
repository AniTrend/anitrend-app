package com.mxt.anitrend.ui

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.base.WelcomeActivity
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingPagerComposeTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun pageRestoresAndContinueStillWorksAfterSwipingBack() {
        ActivityScenario.launch(WelcomeActivity::class.java).use { scenario ->
            compose.waitForIdle()
            assertPage(R.string.app_name)
            val pager = compose.onNodeWithTag("onboarding-pager", useUnmergedTree = true)
            pager.performTouchInput { swipeLeft() }
            compose.waitForIdle()
            assertPage(R.string.app_intro_colors_title)
            continueOnboarding()
            assertPage(R.string.app_intro_content_title)
            pager.performTouchInput { swipeRight() }
            compose.waitForIdle()
            assertPage(R.string.app_intro_colors_title)
            continueOnboarding()
            assertPage(R.string.app_intro_content_title)

            scenario.recreate()
            compose.waitForIdle()
            assertPage(R.string.app_intro_content_title)
            continueOnboarding()
            assertPage(R.string.app_intro_search_title)
            continueOnboarding()
            assertPage(R.string.title_onboarding_manage)
            onView(withId(R.id.action_button)).check(matches(withText(R.string.get_started)))
        }
    }

    private fun continueOnboarding() {
        onView(withId(R.id.action_button)).perform(click())
        compose.waitForIdle()
    }

    private fun assertPage(title: Int) {
        onView(allOf(withId(R.id.title), withText(title))).check(matches(isDisplayed()))
    }
}
