@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.sheet

import android.content.Context
import android.view.View
import android.widget.DatePicker
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.TestSessionUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.sheet.BottomSheetSeriesManage
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
@RunWith(AndroidJUnit4::class)
class FuzzyDateManageSheetComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun partialDatesSeedPickerAndClearKeepsCommittedEntryUntouched() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val committedStartDate = FuzzyDate(day = 0, month = 0, year = 2024)
        val committedCompletedDate = FuzzyDate(day = 0, month = 6, year = 2024)
        val entry = MediaList().apply {
            id = 2L
            mediaId = 1L
            status = KeyUtil.CURRENT
            startedAt = committedStartDate
            completedAt = committedCompletedDate
        }
        val mediaBase = MediaBase().apply {
            id = 1L
            type = KeyUtil.ANIME
            status = KeyUtil.FINISHED
            episodes = 12
            mediaListEntry = entry
        }
        val currentDate = Calendar.getInstance()
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val sheet = BottomSheetSeriesManage.newInstance(mediaBase)
            runOnUiThread {
                sheet.show(hostActivity.supportFragmentManager, "fuzzy-date-test")
                hostActivity.supportFragmentManager.executePendingTransactions()
                assertTrue(requireNotNull(sheet.dialog).isShowing)
            }
            waitForIdle()

            runOnUiThread {
                val dialog = requireNotNull(sheet.dialog)
                val scrollView = requireNotNull(dialog.findViewById<NestedScrollView>(R.id.sheet_manage_scroll_view))
                val datesCard = requireNotNull(dialog.findViewById<View>(R.id.sheet_manage_dates_card))
                scrollView.scrollTo(0, datesCard.top)
            }
            waitForIdle()

            val startedAtLabel = hostActivity.getString(R.string.dialog_title_started_at)
            val completedAtLabel = hostActivity.getString(R.string.dialog_title_completed_at)
            onNodeWithContentDescription("$startedAtLabel, 2024")
                .assertIsDisplayed()
                .performClick()
            assertDatePickerSeed(2024, currentMonth, currentDay)

            val selectedStartedAt = FuzzyDate(day = 18, month = 10, year = 2025)
            onView(withClassName(equalTo(DatePicker::class.java.name)))
                .inRoot(isDialog())
                .perform(setDatePickerValue(year = 2025, month = 9, day = 18))
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
            waitForIdle()
            val selectedStartedAtDescription =
                "$startedAtLabel, ${requireNotNull(DateUtil.convertDate(selectedStartedAt))}"
            onNodeWithContentDescription(selectedStartedAtDescription)
                .assertIsDisplayed()
                .performClick()
            assertDatePickerSeed(2025, 9, 18)
            clearDatePicker()
            waitForIdle()
            onNodeWithContentDescription("$startedAtLabel, TBA")
                .assertIsDisplayed()
                .performClick()
            assertDatePickerSeed(currentYear, currentMonth, currentDay)
            clearDatePicker()
            waitForIdle()

            onNodeWithContentDescription("$completedAtLabel, Jun, 2024")
                .assertIsDisplayed()
                .performClick()
            assertDatePickerSeed(2024, 5, currentDay)
            clearDatePicker()
            waitForIdle()
            onNodeWithContentDescription("$completedAtLabel, TBA")
                .assertIsDisplayed()
                .performClick()
            assertDatePickerSeed(currentYear, currentMonth, currentDay)
            clearDatePicker()
            waitForIdle()

            runOnUiThread { sheet.dismiss() }
            waitForIdle()
        }

        assertEquals(2024, committedStartDate.year)
        assertEquals(0, committedStartDate.month)
        assertEquals(0, committedStartDate.day)
        assertEquals(2024, committedCompletedDate.year)
        assertEquals(6, committedCompletedDate.month)
        assertEquals(0, committedCompletedDate.day)
    }

    private fun assertDatePickerSeed(
        year: Int,
        month: Int,
        day: Int,
    ) {
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .check { view, _ ->
                val picker = view as DatePicker
                assertEquals(year, picker.year)
                assertEquals(month, picker.month)
                assertEquals(day, picker.dayOfMonth)
            }
    }

    private fun clearDatePicker() {
        onView(withText(R.string.dialog_button_clear))
            .inRoot(isDialog())
            .perform(click())
    }

    private fun setDatePickerValue(
        year: Int,
        month: Int,
        day: Int,
    ): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(DatePicker::class.java)

        override fun getDescription(): String = "set the date picker to $year-$month-$day"

        override fun perform(
            uiController: UiController,
            view: View,
        ) {
            (view as DatePicker).updateDate(year, month, day)
        }
    }
}
