@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.sheet

import android.os.Bundle
import android.os.Parcel
import android.widget.ViewFlipper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.extension.parcelableArrayList
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.view.sheet.UserSheetModel
import com.mxt.anitrend.view.sheet.toUserSheetModel
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feed users sheet boundary tests ([BottomSheetUsers] + [UserSheetModel]).
 *
 * A real [android.os.Parcel] is not available in JVM unit tests (the SDK is stubbed),
 * so the bundle round trip that previously zeroed every `@IgnoredOnParcel` [UserBase]
 * id is covered here on a real Android runtime.
 */
@RunWith(AndroidJUnit4::class)
class BottomSheetUsersBoundaryTest {

    @Test
    fun userSheetModelBundleRoundTripPreservesIdAndFollowState() {
        val original =
            UserSheetModel(
                id = 42L,
                name = "liker",
                avatar = "https://example.com/avatar.png",
                isFollowing = true,
            )

        val restored = original.toBundleRoundTrip()

        assertEquals(42L, restored.id)
        assertEquals("liker", restored.name)
        assertEquals("https://example.com/avatar.png", restored.avatar)
        assertEquals(true, restored.isFollowing)
    }

    @Test
    fun builderBundleRoundTripPreservesRealUserIdsAvatarsAndFollowState() {
        val liker =
            UserBase(name = "liker", isFollowing = true).apply {
                id = 42L
                avatar = ImageBase(extraLarge = null, large = "https://example.com/a.png", medium = null)
            }
        val other =
            UserBase(name = "other-user", isFollowing = false).apply {
                id = 1337L
                avatar = ImageBase(extraLarge = "https://example.com/b.png", large = null, medium = null)
            }

        val bundle =
            BottomSheetUsers
                .Builder()
                .setModel(listOf(liker, other))
                .build()
                .arguments
        val restored = BottomSheetUsers.resolveUsers(requireNotNull(bundle).toParcelRoundTrip())

        assertEquals(2, restored?.size)
        assertEquals(42L, restored?.get(0)?.id)
        assertEquals("liker", restored?.get(0)?.name)
        assertEquals("https://example.com/a.png", restored?.get(0)?.avatar?.large)
        assertEquals(true, restored?.get(0)?.isFollowing)
        assertEquals(1337L, restored?.get(1)?.id)
        assertEquals("other-user", restored?.get(1)?.name)
        assertEquals("https://example.com/b.png", restored?.get(1)?.avatar?.extraLarge)
        assertEquals(false, restored?.get(1)?.isFollowing)
    }

    @Test
    fun legacyUserBaseDirectParcelRoundTripDropsId() {
        // Documents why the sheet boundary exists: parceling UserBase directly zeroes
        // the @IgnoredOnParcel id (and avatar), which is what used to break follow
        // dispatch and store rebinding for the feed users sheet.
        val original =
            UserBase(name = "liker", isFollowing = true).apply {
                id = 42L
                avatar = ImageBase(extraLarge = null, large = "https://example.com/a.png", medium = null)
            }

        val bundle = Bundle().apply {
            putParcelableArrayList(KeyUtil.arg_list_model, arrayListOf(original))
        }
        val restored = bundle.toParcelRoundTrip().parcelableArrayList<UserBase>(KeyUtil.arg_list_model)

        assertEquals(1, restored?.size)
        assertEquals(0L, restored?.get(0)?.id)
        assertNull(restored?.get(0)?.avatar)
        // Constructor-parceled fields survive; only the ignored ones are lost.
        assertEquals("liker", restored?.get(0)?.name)
    }

    @Test
    fun roundTrippedUserClickDispatchesRealUserId() {
        val liker =
            UserBase(name = "liker").apply {
                id = 42L
            }
        val bundle =
            BottomSheetUsers
                .Builder()
                .setModel(listOf(liker))
                .build()
                .arguments
        val restored = BottomSheetUsers.resolveUsers(requireNotNull(bundle).toParcelRoundTrip())
        val roundTrippedUser = restored?.single() ?: error("Expected one round-tripped user")

        val deliveredUserIds = mutableListOf<Long>()
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val widget = FollowStateWidget(activity)
                widget.setCurrentUser(
                    UserBase(name = "current-user").apply {
                        id = 1L
                    },
                )
                widget.setListener(
                    FollowStateWidget.Listener { userId ->
                        deliveredUserIds.add(userId)
                    },
                )
                widget.setUserModel(roundTrippedUser)

                val flipper = widget.findViewById<ViewFlipper>(R.id.widget_flipper)
                flipper.performClick()
            }
        }

        // The id that survived the bundle round trip is the id that gets dispatched.
        assertEquals(listOf(42L), deliveredUserIds)
        assertTrue(roundTrippedUser.id == 42L)
    }

    private fun UserSheetModel.toBundleRoundTrip(): UserSheetModel =
        Bundle().apply {
            putParcelableArrayList(KeyUtil.arg_list_model, arrayListOf(this@toBundleRoundTrip))
        }.toParcelRoundTrip().parcelableArrayList<UserSheetModel>(KeyUtil.arg_list_model)!!.single()

    private fun Bundle.toParcelRoundTrip(): Bundle {
        val parcel = Parcel.obtain()
        return try {
            writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            Bundle.CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
