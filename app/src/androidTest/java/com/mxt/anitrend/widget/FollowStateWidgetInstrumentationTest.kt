@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import android.widget.FrameLayout
import android.widget.ViewFlipper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.WidgetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FollowStateWidgetInstrumentationTest {
    @Test
    fun clickDeliversUserIdFireAndForget_andDoesNotMutateBoundEntity() {
        val database = KoinExt.get(DatabaseHelper::class.java)
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            Settings(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = true
            database.currentUser = User().apply {
                id = 1L
                name = "current-user"
            }

            val targetUser = UserBase(name = "other-user", isFollowing = false).apply {
                id = 2L
            }

            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setUserModel(targetUser)

                val deliveredUserIds = mutableListOf<Long>()
                widget.setListener(
                    FollowStateWidget.Listener { userId ->
                        deliveredUserIds.add(userId)
                    },
                )

                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_flipper)
                flipper.performClick()

                // Fire-and-forget: the userId is delivered without any result callback.
                assertEquals(listOf(2L), deliveredUserIds)
                // Render-only: the bound entity is never mutated by the widget.
                assertFalse(targetUser.isFollowing)
                assertEquals(WidgetState.LOADING_STATE, flipper.displayedChild)

                // A newly bound committed state resets the loading spinner and re-renders.
                val committedUser = UserBase(name = "other-user", isFollowing = true).apply {
                    id = 2L
                }
                widget.setUserModel(committedUser)

                val label = widget.findViewById<MaterialButton>(R.id.button_state_text)
                assertEquals(WidgetState.CONTENT_STATE, flipper.displayedChild)
                assertEquals(activity.getString(R.string.following), label.text.toString())
            }
        }
        database.invalidateBoxStores()
        Settings(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = false
    }

    @Test
    fun clickWithoutListener_keepsContentState() {
        val database = KoinExt.get(DatabaseHelper::class.java)
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            Settings(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = true
            database.currentUser = User().apply {
                id = 1L
                name = "current-user"
            }

            val targetUser = UserBase(name = "other-user", isFollowing = false).apply {
                id = 2L
            }

            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setUserModel(targetUser)

                // No listener configured: the tap has no callback target, so the
                // widget must neither dispatch nor leave the content state.
                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_flipper)
                flipper.performClick()

                assertEquals(WidgetState.CONTENT_STATE, flipper.displayedChild)
                // Render-only contract preserved: the bound entity is never mutated.
                assertFalse(targetUser.isFollowing)
            }
        }
        database.invalidateBoxStores()
        Settings(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = false
    }

    @Test
    fun anotherUserControlVisible_whenCurrentUserArrivesAfterModel() {
        // Regression for the fresh-sheet defect: adapters bind the row model before
        // the current-user context on the first pass. The visibility decision must be
        // re-evaluated when the user context arrives, or every row stays hidden.
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                val targetUser = UserBase(name = "other-user").apply { id = 2L }

                // Adapter order: model first, current user second.
                widget.setUserModel(targetUser)
                assertEquals(View.GONE, widget.visibility)

                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )

                assertEquals(View.VISIBLE, widget.visibility)
                val label = widget.findViewById<MaterialButton>(R.id.button_state_text)
                assertEquals(activity.getString(R.string.follow), label.text.toString())
            }
        }
    }

    @Test
    fun anotherUserControlVisible_whenCurrentUserBoundBeforeModel() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setUserModel(UserBase(name = "other-user").apply { id = 2L })

                assertEquals(View.VISIBLE, widget.visibility)
            }
        }
    }

    @Test
    fun selfRowControlStaysHidden() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setUserModel(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )

                // Self rows intentionally render no follow control: you cannot
                // follow yourself.
                assertEquals(View.GONE, widget.visibility)
            }
        }
    }

    @Test
    fun tapOnInnerButtonStateText_dispatchesRealUserIdExactlyOnce() {
        // Regression for the follow-control defect: the inner button state text is
        // the actual tap target Android delivers touches (and accessibility
        // activations) to. Tapping it must dispatch the bound user's real id, and a
        // single tap must never dispatch twice (parent and child share the listener,
        // but the CONTENT_STATE guard switches to loading on the first dispatch).
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setUserModel(UserBase(name = "other-user").apply { id = 2L })

                val deliveredUserIds = mutableListOf<Long>()
                widget.setListener(
                    FollowStateWidget.Listener { userId ->
                        deliveredUserIds.add(userId)
                    },
                )

                val button = widget.findViewById<MaterialButton>(R.id.button_state_text)
                button.performClick()

                // The real bound id is delivered exactly once and the control moves
                // into the loading state.
                assertEquals(listOf(2L), deliveredUserIds)
                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_flipper)
                assertEquals(WidgetState.LOADING_STATE, flipper.displayedChild)

                // A further tap while the mutation is in flight must not re-dispatch.
                button.performClick()
                assertEquals(listOf(2L), deliveredUserIds)
            }
        }
    }

    @Test
    fun noCurrentUser_hidesControl() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)

                val widget = FollowStateWidget(activity)
                container.addView(widget)
                widget.setUserModel(UserBase(name = "other-user").apply { id = 2L })

                // Without an authenticated current-user context there is nothing to
                // follow from, so the control stays hidden.
                assertEquals(View.GONE, widget.visibility)
            }
        }
    }
}
