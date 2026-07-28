@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.view.activity.detail.NotificationActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ToolbarHomeNavigationTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
    }

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
        override fun getIntent(): Intent? = null
        override fun setIntent(intent: Intent?): MenuItem = this
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

    private fun launchEntryPoint(name: String): ActivityScenario<Activity> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent =
            EntryPointFixtures.authenticated(context)
                .first { it.name == name }
                .intentProvider(context)
        @Suppress("UNCHECKED_CAST")
        return ActivityScenario.launch<Activity>(intent) as ActivityScenario<Activity>
    }

    @Test
    fun homeMenu_dispatchesBackForRepresentativeActivities() {
        listOf(
            "AboutActivity",
            "CommentActivity",
            "MediaListActivity",
            "SearchActivity",
        ).forEach { activityName ->
            launchEntryPoint(activityName).use { scenario ->
                scenario.onActivity { activity ->
                    assertTrue(
                        "$activityName should consume toolbar home",
                        activity.onOptionsItemSelected(menuItemStub(android.R.id.home)),
                    )
                    assertTrue(
                        "$activityName should begin finishing after toolbar home",
                        activity.isFinishing,
                    )
                }
            }
        }
    }

    @Test
    fun notificationActivity_homeMatchesTaskRootBackBehavior() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(MainActivity::class.java.name, null, false)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent =
            Intent(context, NotificationActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        try {
            ActivityScenario.launch<NotificationActivity>(intent).use { scenario ->
                scenario.onActivity { activity ->
                    assertTrue(
                        "NotificationActivity should launch as task root for this regression check",
                        activity.isTaskRoot,
                    )
                    assertTrue(
                        "NotificationActivity should consume toolbar home",
                        activity.onOptionsItemSelected(menuItemStub(android.R.id.home)),
                    )
                    assertTrue(
                        "NotificationActivity should begin finishing after toolbar home",
                        activity.isFinishing,
                    )
                }

                instrumentation.waitForIdleSync()
                val launchedMain = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
                assertNotNull(
                    "Task-root toolbar home should redirect to MainActivity before finishing",
                    launchedMain,
                )
                launchedMain?.finish()
            }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }
}
