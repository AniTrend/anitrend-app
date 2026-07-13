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
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView
import com.mxt.anitrend.base.custom.view.widget.AboutPanelWidget
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@LargeTest
@RunWith(AndroidJUnit4::class)
class EventBusMutationStateGuardTest {

    @Test
    fun avatarIndicator_refreshesNotificationBadgeAfterReattach() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            val widgetRef = AtomicReference<AvatarIndicatorView>()
            val notificationCountRef = AtomicReference<TextView>()
            val badgeContainerRef = AtomicReference<View>()
            val database = DatabaseHelper()

            val initialUser = User().apply {
                id = 7L
                name = "avatar-user"
                unreadNotificationCount = 2
            }
            val updatedUser = User().apply {
                id = 7L
                name = "avatar-user"
                unreadNotificationCount = 5
            }

            Settings(InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = true
            database.currentUser = initialUser

            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = AvatarIndicatorView(activity)
                container.addView(widget)
                widgetRef.set(widget)
                notificationCountRef.set(widget.findViewById(R.id.notification_count))
                badgeContainerRef.set(widget.findViewById(R.id.container))
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertTrue(EventBus.getDefault().isRegistered(widgetRef.get()))
                assertEquals("2", notificationCountRef.get().text.toString())
                assertEquals(View.VISIBLE, badgeContainerRef.get().visibility)
            }

            scenario.onActivity {
                val widget = widgetRef.get()
                val container = widget.parent as FrameLayout
                container.removeView(widget)
                assertFalse(EventBus.getDefault().isRegistered(widget))
                container.addView(widget)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertTrue(EventBus.getDefault().isRegistered(widgetRef.get()))
                database.currentUser = updatedUser
                widgetRef.get().onModelChanged(BaseConsumer(KeyUtil.USER_CURRENT_REQ, updatedUser))
            }

            scenario.onActivity {
                assertEquals("5", notificationCountRef.get().text.toString())
                assertEquals(View.VISIBLE, badgeContainerRef.get().visibility)
            }

            database.invalidateBoxStores()
            Settings(InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = false
        }
    }

    @Test
    @Suppress("LongMethod")
    fun followerMutation_isAppliedOnceWhenSameFollowStateEventRepostsAfterReattach() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            val widgetRef = AtomicReference<AboutPanelWidget>()
            val followersCountRef = AtomicReference<TextView>()
            val followMutation = UserBase(isFollowing = true)

            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = AboutPanelWidget(activity)
                container.addView(widget)
                widgetRef.set(widget)

                val lifecycleOwner = TestLifecycleOwner()
                lifecycleOwner.moveToStarted()
                widget.setPrivateField("lifecycle", lifecycleOwner.lifecycle)
                widget.setPrivateField("followers", PageInfo(total = 10))

                val followersCount = widget.findViewById<TextView>(R.id.user_followers_count)
                followersCountRef.set(followersCount)
                followersCount.text = WidgetPresenter.valueFormatter(10)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertTrue(
                    "Expected widget to be registered before the first mutation posts",
                    EventBus.getDefault().isRegistered(widgetRef.get()),
                )
            }

            scenario.onActivity {
                widgetRef.get().onModelChanged(BaseConsumer(KeyUtil.MUT_TOGGLE_FOLLOW, followMutation))
            }

            scenario.onActivity {
                assertEquals(
                    "Expected the first follow mutation to increment followers to 11",
                    WidgetPresenter.valueFormatter(11),
                    followersCountRef.get().text.toString(),
                )
                assertTrue(
                    "Expected widget to be registered while attached",
                    EventBus.getDefault().isRegistered(widgetRef.get()),
                )
            }

            scenario.onActivity {
                val widget = widgetRef.get()
                val container = widget.parent as FrameLayout
                container.removeView(widget)

                assertFalse(
                    "Expected widget to unregister when detached",
                    EventBus.getDefault().isRegistered(widget),
                )

                container.addView(widget)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertTrue(
                    "Expected widget to register again when re-attached",
                    EventBus.getDefault().isRegistered(widgetRef.get()),
                )
            }

            scenario.onActivity {
                widgetRef.get().onModelChanged(BaseConsumer(KeyUtil.MUT_TOGGLE_FOLLOW, followMutation))
            }

            scenario.onActivity {
                assertEquals(
                    "Expected the same follow mutation to be ignored after re-attach",
                    WidgetPresenter.valueFormatter(11),
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
