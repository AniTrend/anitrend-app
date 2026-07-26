@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.widget.FrameLayout
import android.widget.ViewFlipper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.WidgetState
import okhttp3.Request
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@LargeTest
@RunWith(AndroidJUnit4::class)
class FollowStateWidgetInstrumentationTest {
    @Test
    fun onResponse_togglesFollowStateAndResetsLoadingSpinner() {
        val database = DatabaseHelper()
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
                widget.setUserModel(targetUser)

                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_flipper)
                flipper.displayedChild = WidgetState.LOADING_STATE
                widget.onResponse(call = NoOpUserBaseCall(), response = Response.success(targetUser))

                val label = widget.findViewById<MaterialButton>(R.id.button_state_text)
                assertTrue(targetUser.isFollowing)
                assertEquals(activity.getString(R.string.following), label.text.toString())
                assertEquals(WidgetState.CONTENT_STATE, flipper.displayedChild)
            }
        }
        database.invalidateBoxStores()
        Settings(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext).isAuthenticated = false
    }
}

private class NoOpUserBaseCall : retrofit2.Call<UserBase> {
    override fun execute(): Response<UserBase> = throw UnsupportedOperationException()

    override fun enqueue(callback: retrofit2.Callback<UserBase>) = Unit

    override fun isExecuted(): Boolean = false

    override fun cancel() = Unit

    override fun isCanceled(): Boolean = false

    override fun clone(): retrofit2.Call<UserBase> = NoOpUserBaseCall()

    override fun request(): Request = Request.Builder().url("https://example.com").build()

    override fun timeout(): Timeout = Timeout.NONE
}
