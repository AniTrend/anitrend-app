@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.view.MenuItem
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.LogEntryAdapter
import com.mxt.anitrend.extension.logFile
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.view.activity.base.LoggingActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileWriter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoggingActivityTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
        val logFile = context.logFile()
        logFile.parentFile?.mkdirs()
        FileWriter(logFile).use { writer ->
            writer.write(
                """
                07-19 09:15:30:001 I/TestTag(1000) : Info log line one
                07-19 09:15:30:002 W/TestTag(1000) : Warning log line one
                07-19 09:15:30:003 E/TestTag(1000) : Error log line one
                07-19 09:15:30:004 D/TestTag(1000) : Debug log line
                07-19 09:15:30:005 V/TestTag(1000) : Verbose log line
                07-19 09:15:30:006 E/TestTag(1000) : Error log line two with stack
                \t\tat com.example.Test.testMethod(Test.kt:42)
                07-19 09:15:30:007 W/TestTag(1000) : Warning log line two
                """.trimIndent(),
            )
        }
    }

    // ── helpers ──

    /** Polls the ViewModel load-complete flag from the test thread. */
    private fun waitForLogLoad(activity: LoggingActivity, timeoutMs: Long = 10_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!activity.loggingViewModel.isLogLoadComplete && System.currentTimeMillis() < deadline) {
            Thread.sleep(100)
        }
    }

    /** Yields the main thread so the ViewModel state collector can process a pending emission. */
    private fun yieldToCollector(activity: LoggingActivity, timeoutMs: Long = 3_000) {
        val latch = CountDownLatch(1)
        activity.window.decorView.post { latch.countDown() }
        latch.await(timeoutMs, TimeUnit.MILLISECONDS)
    }

    /** Creates a minimal [MenuItem] stub returning the given [itemId]. */
    private fun menuItemStub(itemId: Int): MenuItem = object : MenuItem {
        override fun getItemId(): Int = itemId
        override fun getGroupId(): Int = 0
        override fun getOrder(): Int = 0
        override fun getTitle(): CharSequence = ""
        override fun getTitleCondensed(): CharSequence = ""
        override fun setTitle(title: CharSequence?): MenuItem = this
        override fun setTitle(title: Int): MenuItem = this
        override fun setTitleCondensed(title: CharSequence?): MenuItem = this
        override fun getIcon() = null
        override fun setIcon(icon: android.graphics.drawable.Drawable?): MenuItem = this
        override fun setIcon(iconRes: Int): MenuItem = this
        override fun getIntent(): android.content.Intent? = null
        override fun setIntent(intent: android.content.Intent?): MenuItem = this
        override fun getSubMenu(): android.view.SubMenu? = null
        override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem = this
        override fun setNumericShortcut(numericChar: Char): MenuItem = this
        override fun getNumericShortcut(): Char = '0'
        override fun setAlphabeticShortcut(alphaChar: Char): MenuItem = this
        override fun getAlphabeticShortcut(): Char = '0'
        override fun isCheckable(): Boolean = false
        override fun setCheckable(checkable: Boolean): MenuItem = this
        override fun isChecked(): Boolean = false
        override fun setChecked(checked: Boolean): MenuItem = this
        override fun isVisible(): Boolean = true
        override fun setVisible(visible: Boolean): MenuItem = this
        override fun isEnabled(): Boolean = true
        override fun setEnabled(enabled: Boolean): MenuItem = this
        override fun hasSubMenu(): Boolean = false
        override fun setOnMenuItemClickListener(listener: MenuItem.OnMenuItemClickListener?): MenuItem = this
        override fun getMenuInfo(): android.view.ContextMenu.ContextMenuInfo? = null
        override fun setShowAsAction(actionEnum: Int) {}
        override fun setShowAsActionFlags(actionEnum: Int): MenuItem = this
        override fun setActionView(view: android.view.View?): MenuItem = this
        override fun setActionView(resId: Int): MenuItem = this
        override fun getActionView(): android.view.View? = null
        override fun setActionProvider(provider: android.view.ActionProvider?): MenuItem = this
        override fun getActionProvider(): android.view.ActionProvider? = null
        override fun expandActionView(): Boolean = false
        override fun collapseActionView(): Boolean = false
        override fun isActionViewExpanded(): Boolean = false
        override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem = this
        override fun setContentDescription(description: CharSequence?): MenuItem = this
        override fun getContentDescription(): CharSequence = ""
        override fun setTooltipText(tooltip: CharSequence?): MenuItem = this
        override fun getTooltipText(): CharSequence = ""
        override fun setIconTintList(tint: android.content.res.ColorStateList?): MenuItem = this
        override fun getIconTintList(): android.content.res.ColorStateList? = null
        override fun getIconTintMode(): android.graphics.PorterDuff.Mode? = null
        override fun setIconTintMode(mode: android.graphics.PorterDuff.Mode?): MenuItem = this
        override fun getIconTintBlendMode(): android.graphics.BlendMode? = null
        override fun setIconTintBlendMode(mode: android.graphics.BlendMode?): MenuItem = this
    }

    // ── presence and launch ──

    private fun getLogAdapter(activity: LoggingActivity): LogEntryAdapter {
        val recycler = activity.findViewById<RecyclerView>(R.id.log_recycler)
        assertNotNull("RecyclerView should be present", recycler)
        return recycler!!.adapter as LogEntryAdapter
    }

    @Test
    fun activityLaunches_withRecyclerViewAndParsedEntries() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)

                val recycler = activity.findViewById<RecyclerView>(R.id.log_recycler)
                assertNotNull("RecyclerView should be present", recycler)
                val adapter = recycler!!.adapter
                assertNotNull("RecyclerView adapter should be set", adapter)
                assertTrue(
                    "Adapter should have at least 5 parsed entries, got ${adapter!!.itemCount}",
                    adapter.itemCount >= 5,
                )
            }
        }
    }

    @Test
    fun activityLaunches_showsContentAfterLoad() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)

                val recycler = activity.findViewById<RecyclerView>(R.id.log_recycler)
                assertNotNull("RecyclerView should be present after load", recycler)
                assertTrue(
                    "Adapter should have entries indicating content is loaded",
                    recycler.adapter != null && recycler.adapter!!.itemCount > 0,
                )
            }
        }
    }

    @Test
    fun metadataCard_fieldsAreBoundAfterCreate() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val versionText = activity.findViewById<android.widget.TextView>(R.id.support_version)?.text?.toString()
                val deviceText = activity.findViewById<android.widget.TextView>(R.id.support_device)?.text?.toString()
                val androidText = activity.findViewById<android.widget.TextView>(R.id.support_android)?.text?.toString()

                assertNotNull("Version field should be bound", versionText)
                assertTrue("Version field should contain 'v' prefix", versionText!!.startsWith("v"))
                assertNotNull("Device field should be bound", deviceText)
                assertTrue("Device field should not be empty", deviceText!!.isNotBlank())
                assertNotNull("Android field should be bound", androidText)
                assertTrue("Android field should contain 'Android '", androidText!!.startsWith("Android "))
            }
        }
    }

    // ── filter behavior ──

    @Test
    fun selectingErrorFilter_narrowsDisplayToErrorEntriesOnly() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)

                val adapter = getLogAdapter(activity)
                val totalBefore = adapter.itemCount
                assertTrue("Should have entries before filter, got $totalBefore", totalBefore >= 5)

                // Apply Error filter via ViewModel
                activity.loggingViewModel.setFilter(LogFilter.Error)
                yieldToCollector(activity)

                val totalAfter = adapter.itemCount
                assertTrue(
                    "Filtered entries should be fewer than total, got $totalAfter of $totalBefore",
                    totalAfter < totalBefore,
                )
                assertTrue("Should have at least 2 error entries, got $totalAfter", totalAfter >= 2)

                for (entry in adapter.data) {
                    assertEquals(
                        "All entries should be ERROR, got ${entry.level}",
                        LogEntry.Level.ERROR,
                        entry.level,
                    )
                }

                // Switch back to All
                activity.loggingViewModel.setFilter(LogFilter.All)
                yieldToCollector(activity)
                assertEquals("All should restore full list", totalBefore, adapter.itemCount)
            }
        }
    }

    @Test
    fun selectingWarningFilter_narrowsDisplayToWarningEntriesOnly() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)

                val adapter = getLogAdapter(activity)
                val totalBefore = adapter.itemCount
                activity.loggingViewModel.setFilter(LogFilter.Warning)
                yieldToCollector(activity)

                val totalAfter = adapter.itemCount
                assertTrue("Warning filter should narrow entries", totalAfter < totalBefore)
                assertTrue("Should have at least 2 warning entries", totalAfter >= 2)

                for (entry in adapter.data) {
                    assertEquals(
                        "All entries should be WARNING",
                        LogEntry.Level.WARNING,
                        entry.level,
                    )
                }
            }
        }
    }

    @Test
    fun selectingDebugFilter_includesDebugAndVerboseEntries() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)
                val adapter = getLogAdapter(activity)
                activity.loggingViewModel.setFilter(LogFilter.Debug)
                yieldToCollector(activity)

                val entries = adapter.data
                assertTrue("Debug filter should show entries", entries.isNotEmpty())
                for (entry in entries) {
                    assertTrue(
                        "Should be DEBUG or VERBOSE under Debug filter, got ${entry.level}",
                        entry.level == LogEntry.Level.DEBUG || entry.level == LogEntry.Level.VERBOSE,
                    )
                }
            }
        }
    }

    @Test
    fun selectingInfoFilter_narrowsToInfoOnly() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)
                val adapter = getLogAdapter(activity)
                activity.loggingViewModel.setFilter(LogFilter.Info)
                yieldToCollector(activity)

                assertTrue("Info filter should show entries", adapter.itemCount >= 1)
                for (entry in adapter.data) {
                    assertEquals("All entries should be INFO", LogEntry.Level.INFO, entry.level)
                }
            }
        }
    }

    // ── chip listener path ──

    @Test
    fun selectingErrorChip_firesChipGroupListenerAndNarrowsEntries() {
        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)

                val adapter = getLogAdapter(activity)
                val totalBefore = adapter.itemCount
                assertTrue("Should have entries before filter", totalBefore > 0)

                val filterGroup = activity.findViewById<ChipGroup>(R.id.filter_group)
                assertNotNull("Filter ChipGroup should be present", filterGroup)
                filterGroup!!.check(R.id.filter_error)
                yieldToCollector(activity)

                val totalAfter = adapter.itemCount
                assertTrue(
                    "Selecting Error chip should narrow entries (got $totalAfter of $totalBefore)",
                    totalAfter < totalBefore,
                )
                for (entry in adapter.data) {
                    assertEquals(
                        "All entries should be ERROR after selecting Error chip",
                        LogEntry.Level.ERROR,
                        entry.level,
                    )
                }

                filterGroup.check(R.id.filter_all)
                yieldToCollector(activity)
                assertEquals("Selecting All should restore full list", totalBefore, adapter.itemCount)
            }
        }
    }

    // ── clear behavior ──

    @Test
    fun clearAction_emptiesAdapterAndLogFile() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val logFile = context.logFile()
        assertTrue("Log file should have content before clear", logFile.readText().isNotEmpty())

        ActivityScenario.launch(LoggingActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitForLogLoad(activity)

                val adapter = getLogAdapter(activity)
                assertTrue("Should have entries before clear", adapter.itemCount > 0)

                activity.onOptionsItemSelected(menuItemStub(R.id.action_clear_log))
            }

            // Clear is async via viewModelScope; wait for ViewModel + collector to settle
            Thread.sleep(4000)

            scenario.onActivity { activity ->
                val adapter = getLogAdapter(activity)
                assertEquals(
                    "Adapter should be empty after clear, got ${adapter.itemCount}",
                    0,
                    adapter.itemCount,
                )
            }
        }

        assertEquals("Log file should be empty after clear action", "", logFile.readText())
    }
}
