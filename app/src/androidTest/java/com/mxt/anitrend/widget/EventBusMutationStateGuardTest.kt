@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget
import com.mxt.anitrend.util.WidgetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@LargeTest
@RunWith(AndroidJUnit4::class)
class EventBusMutationStateGuardTest {

    @Test
    fun avatarIndicator_rebindsViewsAfterReattach() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            val widgetRef = AtomicReference<AvatarIndicatorView>()
            val notificationCountRef = AtomicReference<TextView>()
            val badgeContainerRef = AtomicReference<View>()
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = AvatarIndicatorView(activity)
                container.addView(widget)
                widgetRef.set(widget)
                notificationCountRef.set(widget.findViewById(R.id.notification_count))
                badgeContainerRef.set(widget.findViewById(R.id.container))
                widget.render(null, unreadNotificationCount = 2)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertNotNull(notificationCountRef.get())
                assertEquals("2", notificationCountRef.get().text.toString())
                assertEquals(View.VISIBLE, badgeContainerRef.get().visibility)
            }

            scenario.onActivity {
                val widget = widgetRef.get()
                val container = widget.parent as FrameLayout
                container.removeView(widget)
                container.addView(widget)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertEquals("2", notificationCountRef.get().text.toString())
                assertEquals(View.VISIBLE, badgeContainerRef.get().visibility)
            }
        }
    }

    @Test
    fun aboutPanelWidget_preservesDisplayedStatsAfterReattach() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            val widgetRef = AtomicReference<AboutPanelWidget>()
            val followersCountRef = AtomicReference<TextView>()

            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = AboutPanelWidget(activity)
                container.addView(widget)
                widgetRef.set(widget)

                val lifecycleOwner = TestLifecycleOwner()
                lifecycleOwner.moveToStarted()
                widget.setPrivateField("lifecycle", lifecycleOwner.lifecycle)
                widget.setPrivateField("followers", AboutPanelWidget.StatState.Loaded(total = 10))

                val followersCount = widget.findViewById<TextView>(R.id.user_followers_count)
                followersCountRef.set(followersCount)
                followersCount.text = WidgetState.valueFormatter(10)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertEquals(
                    "Expected initial follower text to match assigned value",
                    WidgetState.valueFormatter(10),
                    followersCountRef.get().text.toString(),
                )
            }

            scenario.onActivity {
                val widget = widgetRef.get()
                val container = widget.parent as FrameLayout
                container.removeView(widget)
                container.addView(widget)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertEquals(
                    "Expected follower text to remain stable after re-attach",
                    WidgetState.valueFormatter(10),
                    followersCountRef.get().text.toString(),
                )
            }
        }
    }

    private class TestLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        fun moveToStarted() {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
    }

    private fun Any.setPrivateField(fieldName: String, value: Any?) {
        val field = javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(this, value)
    }
}
