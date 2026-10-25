package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.logFile
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.ui.logging.LOG_ENTRY_LIST_TAG
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.detail.LoggingFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoggingFragmentTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    /** Messages written by [setUp]'s fixture text; keep in sync with it. */
    private val fixtureMessages = listOf(
        "first error",
        "first warning",
        "first info",
        "first debug",
        "first verbose",
        "second error",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // setAuthenticated() also marks settings updated, suppressing the changelog
        // destination push (checkUpdatedVersion) over the destination under test.
        TestSessionUtil.setAuthenticated(context, authenticated = true)
        val logFile = context.logFile()
        logFile.parentFile?.mkdirs()
        logFile.writeText(
            """
            07-19 09:15:30:100 E/ExceptionCrashHandler(2) : first error
            07-19 09:15:30:200 W/chromium(4596) : first warning
            07-19 09:15:30:300 I/[Koin](2) : first info
            07-19 09:15:30:400 D/WM-Schedulers(4573) : first debug
            07-19 09:15:30:500 V/[Koin](4573) : first verbose
            07-19 09:15:30:600 E/ExceptionCrashHandler(2) : second error
            """.trimIndent(),
        )
    }

    @Test
    fun activityLaunches_withComposableAndParsedEntries() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            // The log file is the live application log, so startup lines may append
            // after the fixture; assert one rendered row per fixture line.
            fixtureMessages.forEach(::assertEntryRenderedOnce)
        }
    }

    @Test
    fun selectingErrorFilter_narrowsDisplayToErrorEntriesOnly() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            setFilter(scenario, LogFilter.Error)
            assertEntryRenderedOnce("first error")
            assertEntryRenderedOnce("second error")
            assertEntryAbsent("first warning")
            assertEntryAbsent("first info")
            assertEntryAbsent("first debug")
            assertEntryAbsent("first verbose")
        }
    }

    @Test
    fun selectingWarningFilter_narrowsDisplayToWarningEntriesOnly() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            setFilter(scenario, LogFilter.Warning)
            assertEntryRenderedOnce("first warning")
            assertEntryAbsent("first error")
            assertEntryAbsent("second error")
            assertEntryAbsent("first info")
            assertEntryAbsent("first debug")
            assertEntryAbsent("first verbose")
        }
    }

    @Test
    fun selectingDebugFilter_includesDebugAndVerboseEntries() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            setFilter(scenario, LogFilter.Debug)
            assertEntryRenderedOnce("first debug")
            assertEntryRenderedOnce("first verbose")
            assertEntryAbsent("first error")
            assertEntryAbsent("second error")
            assertEntryAbsent("first warning")
            assertEntryAbsent("first info")
        }
    }

    @Test
    fun selectingInfoFilter_narrowsToInfoOnly() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            setFilter(scenario, LogFilter.Info)
            assertEntryRenderedOnce("first info")
            assertEntryAbsent("first error")
            assertEntryAbsent("second error")
            assertEntryAbsent("first warning")
            assertEntryAbsent("first debug")
            assertEntryAbsent("first verbose")
        }
    }

    @Test
    fun filterChipsDriveTheVisibleEntrySet() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            scenario.onActivity { activity ->
                val filterGroup = activity.findViewById<ChipGroup>(R.id.filter_group)
                assertEquals(R.id.filter_all, filterGroup.checkedChipId)
                filterGroup.check(R.id.filter_error)
            }
            composeRule.waitForIdle()
            assertEntryRenderedOnce("first error")
            assertEntryAbsent("first info")
            scenario.onActivity { activity ->
                activity.findViewById<ChipGroup>(R.id.filter_group).check(R.id.filter_all)
            }
            composeRule.waitForIdle()
            assertEntryRenderedOnce("first info")
            assertEntryRenderedOnce("first error")
        }
    }

    @Test
    fun clearLog_removesAllVisibleEntries() {
        launchLogging { scenario ->
            waitForLogLoad(scenario)
            assertEntryRenderedOnce("first error")
            scenario.onActivity { activity ->
                loggingFragment(activity).onOptionsItemSelected(menuItemStub(R.id.action_clear_log))
            }
            composeRule.waitUntil(5_000L) {
                fixtureMessages.none { message ->
                    composeRule.onAllNodesWithText(message, substring = true)
                        .fetchSemanticsNodes().isNotEmpty()
                }
            }
            fixtureMessages.forEach(::assertEntryAbsent)
        }
    }

    /**
     * The log file is the live application log, so tests assert the fixture's own
     * entries instead of row totals. Messages match by substring because the parser
     * accumulates non-header lines (such as appended stack traces) onto the
     * preceding entry's message. The lazy list is scrolled before visibility checks.
     */
    private fun assertEntryRenderedOnce(message: String) {
        composeRule.onNodeWithTag(LOG_ENTRY_LIST_TAG)
            .performScrollToNode(hasText(message, substring = true))
        composeRule.onAllNodesWithText(message, substring = true).assertCountEquals(1)
        composeRule.onNodeWithText(message, substring = true).assertIsDisplayed()
    }

    private fun assertEntryAbsent(message: String) {
        composeRule.onAllNodesWithText(message, substring = true).assertCountEquals(0)
    }

    private fun setFilter(
        scenario: ActivityScenario<MainActivity>,
        filter: LogFilter,
    ) {
        scenario.onActivity { activity ->
            loggingFragment(activity).loggingViewModel.setFilter(filter)
        }
        composeRule.waitForIdle()
    }

    private fun launchLogging(block: (ActivityScenario<MainActivity>) -> Unit) {
        // Launch MainActivity directly: the launcher intent opens SplashActivity,
        // which finishes itself after routing, leaving ActivityScenario's tracked
        // activity destroyed ("Cannot run onActivity").
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            MainActivity::class.java,
        ).putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_LOGGING)
        ActivityScenario.launch<MainActivity>(intent).use(block)
    }

    private fun waitForLogLoad(scenario: ActivityScenario<MainActivity>) {
        val deadline = System.currentTimeMillis() + 10_000L
        var complete = false
        while (!complete && System.currentTimeMillis() < deadline) {
            scenario.onActivity { activity ->
                complete = runCatching {
                    loggingFragment(activity).loggingViewModel.isLogLoadComplete
                }.getOrDefault(false)
            }
            if (!complete) Thread.sleep(100)
        }
        assertTrue("Log load should complete", complete)
    }

    private fun loggingFragment(activity: MainActivity): LoggingFragment {
        val fragmentManager = activity.supportFragmentManager
        val navHost = fragmentManager.primaryNavigationFragment as NavHostFragment
        val current = navHost.childFragmentManager.primaryNavigationFragment
        assertTrue("Logging destination should be current", current is LoggingFragment)
        return current as LoggingFragment
    }

    private fun menuItemStub(id: Int): android.view.MenuItem = object : android.view.MenuItem {
        override fun getItemId(): Int = id
        override fun getGroupId(): Int = 0
        override fun getOrder(): Int = 0
        override fun setTitle(p0: CharSequence?): android.view.MenuItem = this
        override fun setTitle(p0: Int): android.view.MenuItem = this
        override fun getTitle(): CharSequence? = null
        override fun setTitleCondensed(p0: CharSequence?): android.view.MenuItem = this
        override fun getTitleCondensed(): CharSequence? = null
        override fun setIcon(p0: android.graphics.drawable.Drawable?): android.view.MenuItem = this
        override fun setIcon(p0: Int): android.view.MenuItem = this
        override fun getIcon(): android.graphics.drawable.Drawable? = null
        override fun setIntent(p0: android.content.Intent?): android.view.MenuItem = this
        override fun getIntent(): android.content.Intent? = null
        override fun setShortcut(p0: Char, p1: Char): android.view.MenuItem = this
        override fun setAlphabeticShortcut(p0: Char): android.view.MenuItem = this
        override fun setAlphabeticShortcut(p0: Char, p1: Int): android.view.MenuItem = this
        override fun getAlphabeticShortcut(): Char = ' '
        override fun setNumericShortcut(p0: Char): android.view.MenuItem = this
        override fun setNumericShortcut(p0: Char, p1: Int): android.view.MenuItem = this
        override fun getNumericShortcut(): Char = ' '
        override fun setShowAsAction(p0: Int) = Unit
        override fun setShowAsActionFlags(p0: Int): android.view.MenuItem = this
        override fun setActionView(p0: View?): android.view.MenuItem = this
        override fun setActionView(p0: Int): android.view.MenuItem = this
        override fun getActionView(): View? = null
        override fun setActionProvider(p0: android.view.ActionProvider?): android.view.MenuItem = this
        override fun getActionProvider(): android.view.ActionProvider? = null
        override fun expandActionView(): Boolean = false
        override fun collapseActionView(): Boolean = false
        override fun isActionViewExpanded(): Boolean = false
        override fun setOnActionExpandListener(p0: android.view.MenuItem.OnActionExpandListener?): android.view.MenuItem = this
        override fun setCheckable(p0: Boolean): android.view.MenuItem = this
        override fun isCheckable(): Boolean = false
        override fun setChecked(p0: Boolean): android.view.MenuItem = this
        override fun isChecked(): Boolean = false
        override fun setVisible(p0: Boolean): android.view.MenuItem = this
        override fun isVisible(): Boolean = true
        override fun setEnabled(p0: Boolean): android.view.MenuItem = this
        override fun isEnabled(): Boolean = true
        override fun hasSubMenu(): Boolean = false
        override fun getSubMenu(): android.view.SubMenu? = null
        override fun getMenuInfo(): android.view.ContextMenu.ContextMenuInfo? = null
        override fun setOnMenuItemClickListener(p0: android.view.MenuItem.OnMenuItemClickListener?): android.view.MenuItem = this
    }
}
