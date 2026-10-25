@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.sheet

import android.os.SystemClock
import android.view.View
import android.widget.TextView

import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry

import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import com.mxt.anitrend.widget.ProgressLayout
import com.mxt.anitrend.widget.ProgressLayoutTestActivity

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

/**
 * Dialog-level regression seam for [BottomSheetListUsers] state delivery.
 *
 * The sheet attaches its content via `onCreateDialog` + `dialog.setContentView` and
 * never creates a fragment view, so state collection must run on the fragment
 * lifecycle, not `viewLifecycleOwner`. These tests launch the real dialog against the
 * real Koin graph with a stubbed [UserRepository] and assert that the terminal
 * ViewModel states actually leave the loading state and render into the dialog:
 * success renders rows, failure shows the error/retry view, and retry re-dispatches.
 *
 * Without the lifecycle fix the collector never runs, the spinner persists forever,
 * and every poll below times out.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class BottomSheetListUsersDialogStateTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var originalRepository: UserRepository? = null

    @After
    fun tearDown() {
        originalRepository?.let { GlobalContext.get().declare(it) }
        originalRepository = null
    }

    @Test
    fun loadingSpinnerShownWhileRequestPending_thenSuccessRendersRows() {
        val gate = CompletableDeferred<Unit>()
        var followersResult: Result<PageContainer<UserBase>> =
            Result.success(containerOf(user(2L, "user-2"), user(7L, "user-7")))
        var followerCalls = 0
        val repository = mockk<UserRepository>()
        coEvery {
            repository.getFollowers(1L, any(), any(), any())
        } coAnswers {
            followerCalls++
            gate.await()
            followersResult
        }
        coEvery {
            repository.getFollowing(any(), any(), any(), any())
        } returns Result.success(PageContainer())
        install(repository)

        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            var activity: FragmentActivity? = null
            scenario.onActivity { host ->
                activity = host
                showFollowersSheet(host)
            }

            // While the request is in flight the dialog must render the loading
            // spinner (the state that used to persist forever before the fix).
            waitUntil("loading spinner shown while request is pending") {
                progressLayout(sheetFragment(activity!!))?.isLoading == true
            }

            gate.complete(Unit)

            waitUntil("success renders rows and leaves loading") {
                val sheet = sheetFragment(activity!!)
                progressLayout(sheet)?.isContent == true &&
                    userListComposeView(sheet) != null &&
                    userListItemCount(sheet) == 2
            }
            val progressIsLoading =
                readOnMain { progressLayout(sheetFragment(activity!!))?.isLoading ?: true }
            assertFalse("loading spinner must be cleared after success", progressIsLoading)
            assertEquals(1, followerCalls)
            coVerify(exactly = 1) { repository.getFollowers(1L, any(), any(), any()) }
        }
    }

    @Test
    fun failureLeavesLoading_showsErrorAndRetry() {
        var followersResult: Result<PageContainer<UserBase>> =
            Result.failure(RuntimeException("network down"))
        val repository = mockk<UserRepository>()
        coEvery {
            repository.getFollowers(1L, any(), any(), any())
        } coAnswers {
            followersResult
        }
        coEvery {
            repository.getFollowing(any(), any(), any(), any())
        } returns Result.success(PageContainer())
        install(repository)

        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            var activity: FragmentActivity? = null
            scenario.onActivity { host ->
                activity = host
                showFollowersSheet(host)
            }

            waitUntil("error state renders") {
                progressLayout(sheetFragment(activity!!))?.isError == true
            }

            val progressIsLoading =
                readOnMain { progressLayout(sheetFragment(activity!!))?.isLoading ?: true }
            val errorText =
                readOnMain {
                    sheetFragment(activity!!)?.dialog
                        ?.findViewById<TextView>(R.id.progressStateErrorText)
                        ?.text?.toString()
                }
            val retryVisible =
                readOnMain {
                    sheetFragment(activity!!)?.dialog
                        ?.findViewById<MaterialButton>(R.id.progressStateErrorAction)
                        ?.visibility == View.VISIBLE
                }
            assertFalse("loading spinner must be cleared after error", progressIsLoading)
            assertEquals("network down", errorText)
            assertTrue("retry button must be visible", retryVisible)

            // Backend recovers: retry must re-dispatch the request and render rows.
            instrumentation.runOnMainSync {
                followersResult = Result.success(containerOf(user(2L, "user-2")))
                sheetFragment(activity!!)?.dialog
                    ?.findViewById<MaterialButton>(R.id.progressStateErrorAction)
                    ?.performClick()
            }
            waitUntil("retry re-dispatches and renders rows") {
                val sheet = sheetFragment(activity!!)
                progressLayout(sheet)?.isContent == true &&
                    userListComposeView(sheet) != null &&
                    userListItemCount(sheet) == 1
            }
            coVerify(exactly = 2) { repository.getFollowers(1L, any(), any(), any()) }
        }
    }

    @Test
    fun stopThenStartDoesNotDuplicateTheReplayedPage() {
        val repository = mockk<UserRepository>()
        coEvery {
            repository.getFollowers(1L, any(), any(), any())
        } returns Result.success(containerOf(user(2L, "user-2"), user(7L, "user-7")))
        coEvery {
            repository.getFollowing(any(), any(), any(), any())
        } returns Result.success(PageContainer())
        install(repository)

        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            var activity: FragmentActivity? = null
            scenario.onActivity { host ->
                activity = host
                showFollowersSheet(host)
            }

            waitUntil("rows render before the lifecycle stop") {
                val sheet = sheetFragment(activity!!)
                progressLayout(sheet)?.isContent == true && userListItemCount(sheet) == 2
            }

            // Stopping re-subscribes the StateFlow collector on the way back to
            // STARTED; the replayed Success must not append its page again.
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            instrumentation.waitForIdleSync()

            waitUntil("replayed page does not duplicate rows") {
                userListItemCount(sheetFragment(activity!!)) == 2
            }
            assertEquals(2, readOnMain { userListItemCount(sheetFragment(activity!!)) })
            coVerify(exactly = 1) { repository.getFollowers(1L, any(), any(), any()) }
        }
    }

    @Test
    fun nextPageFailureThenRetryDoesNotDuplicateTheRefreshedFirstPage() {
        var followerCalls = 0
        val requestedPages = mutableListOf<Int>()
        val pageTwoFailure = CompletableDeferred<Unit>()
        val repository = mockk<UserRepository>()
        coEvery {
            repository.getFollowers(1L, any(), any(), any())
        } coAnswers {
            val page = invocation.args[1] as Int
            requestedPages.add(page)
            followerCalls++
            when {
                followerCalls == 1 -> Result.success(
                    containerOf(user(2L, "user-2"), user(7L, "user-7"), hasNextPage = true),
                )
                // Park the next-page failure until the first page is visibly
                // rendered, or the immediate Error state races the content sample.
                page > 1 -> {
                    pageTwoFailure.await()
                    Result.failure(RuntimeException("next page down"))
                }
                else -> Result.success(containerOf(user(2L, "user-2"), user(7L, "user-7")))
            }
        }
        coEvery {
            repository.getFollowing(any(), any(), any(), any())
        } returns Result.success(PageContainer())
        install(repository)

        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            var activity: FragmentActivity? = null
            scenario.onActivity { host ->
                activity = host
                showFollowersSheet(host)
            }

            waitUntil("first page rows render") {
                userListItemCount(sheetFragment(activity!!)) == 2
            }
            waitUntil("page two is requested") {
                requestedPages.size >= 2
            }
            pageTwoFailure.complete(Unit)

            // The parked page-2 request now fails, surfacing the error/retry state.
            waitUntil("next-page failure surfaces retry") {
                progressLayout(sheetFragment(activity!!))?.isError == true
            }
            instrumentation.runOnMainSync {
                sheetFragment(activity!!)?.dialog
                    ?.findViewById<MaterialButton>(R.id.progressStateErrorAction)
                    ?.performClick()
            }

            // Retry re-requests page 1; its rows must replace, not append, or the
            // keyed lazy grid crashes on duplicate row keys.
            waitUntil("retry refreshes the first page without duplicates") {
                val sheet = sheetFragment(activity!!)
                progressLayout(sheet)?.isContent == true && userListItemCount(sheet) == 2
            }
            assertEquals(2, readOnMain { userListItemCount(sheetFragment(activity!!)) })
            assertEquals("expected load, next-page failure, then page-1 retry", listOf(1, 2, 1), requestedPages)
        }
    }

    @Test
    fun nonemptyThenEmptyRefreshClearsRowsAndShowsEmptySurface() {
        var followerCalls = 0
        val repository = mockk<UserRepository>()
        coEvery {
            repository.getFollowers(1L, any(), any(), any())
        } coAnswers {
            followerCalls++
            if (followerCalls == 1) {
                Result.success(containerOf(user(2L, "user-2"), user(7L, "user-7")))
            } else {
                Result.success(PageContainer())
            }
        }
        coEvery {
            repository.getFollowing(any(), any(), any(), any())
        } returns Result.success(PageContainer())
        install(repository)

        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            var activity: FragmentActivity? = null
            scenario.onActivity { host ->
                activity = host
                showFollowersSheet(host)
            }

            waitUntil("first page rows render") {
                userListItemCount(sheetFragment(activity!!)) == 2
            }

            // A successful but now-empty page 1 must replace the rows and reach
            // the empty surface instead of leaving stale rows behind.
            instrumentation.runOnMainSync {
                sheetFragment(activity!!)?.makeRequest()
            }
            waitUntil("empty refresh clears rows") {
                sheetFragment(activity!!)?.userItems?.value.isNullOrEmpty()
            }
            waitUntil("empty surface shows") {
                progressLayout(sheetFragment(activity!!))?.isError == true
            }
            assertTrue(
                readOnMain { sheetFragment(activity!!)?.userItems?.value.isNullOrEmpty() },
            )
        }
    }

    private fun showFollowersSheet(activity: FragmentActivity) {
        BottomSheetListUsers
            .Builder()
            .setUserId(1L)
            .setRequestType(KeyUtil.USER_FOLLOWERS_REQ)
            .setModelCount(2)
            .setTitle(R.string.title_bottom_sheet_likes)
            .build()
            .let { it as BottomSheetListUsers }
            .show(activity.supportFragmentManager, SHEET_TAG)
    }

    private fun install(repository: UserRepository) {
        val koin = GlobalContext.get()
        if (originalRepository == null) {
            originalRepository = koin.get()
        }
        koin.declare(repository)
    }

    private fun sheetFragment(activity: FragmentActivity): BottomSheetListUsers? = activity.supportFragmentManager.findFragmentByTag(SHEET_TAG) as? BottomSheetListUsers

    private fun progressLayout(sheet: BottomSheetListUsers?): ProgressLayout? = sheet?.dialog?.findViewById(R.id.stateLayout)

    private fun userListComposeView(sheet: BottomSheetListUsers?): ComposeView? = sheet?.dialog?.findViewById(R.id.userListComposeView)

    private fun userListItemCount(sheet: BottomSheetListUsers?): Int? = sheet?.userListState?.layoutInfo?.totalItemsCount

    private inline fun <T> readOnMain(crossinline block: () -> T): T {
        var result: T? = null
        instrumentation.runOnMainSync { result = block() }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun waitUntil(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            var satisfied = false
            instrumentation.runOnMainSync { satisfied = condition() }
            if (satisfied) return
            SystemClock.sleep(POLL_MS)
        }
        fail("Timed out waiting for: $description")
    }

    private fun user(
        id: Long,
        name: String,
    ): UserBase = UserBase(name = name).apply {
        this.id = id
    }

    private fun containerOf(
        vararg users: UserBase,
        hasNextPage: Boolean = false,
    ): PageContainer<UserBase> = PageContainer<UserBase>().apply {
        pageData = users.toList()
        pageInfo = PageInfo().apply { setHasNextPage(hasNextPage) }
    }

    private companion object {
        const val SHEET_TAG = "users-list-sheet-state-test"
        const val TIMEOUT_MS = 8_000L
        const val POLL_MS = 50L
    }
}
