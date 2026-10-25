package com.mxt.anitrend.ui

import android.widget.FrameLayout
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.detail.NotificationFragment
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import io.mockk.coEvery
import io.mockk.mockk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
class NotificationFragmentLifecycleTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun loadedPagesStayUniqueAcrossResumeAndViewRecreation() {
        val repository = mockk<UserRepository>()
        val requestsByPage = ConcurrentHashMap<Int, AtomicInteger>()
        coEvery {
            repository.getUserNotifications(any(), any(), any(), any(), any())
        } coAnswers {
            val page = requireNotNull(firstArg<Int?>())
            requestsByPage.computeIfAbsent(page) { AtomicInteger() }.incrementAndGet()
            Result.success(notificationPage(page))
        }
        val koin = GlobalContext.get()
        val originalRepository = koin.get<UserRepository>()
        koin.declare(repository)

        try {
            val containerId = android.view.View.generateViewId()
            lateinit var fragment: NotificationFragment
            ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContentView(FrameLayout(activity).apply { id = containerId })
                    fragment = NotificationFragment.newInstance()
                    activity.supportFragmentManager.beginTransaction()
                        .replace(containerId, fragment, NOTIFICATION_FRAGMENT_TAG)
                        .commitNow()
                }

                val firstNotification = notificationContext(NOTIFICATION_ID_BASE + 1L)
                val lastPageOneNotification = notificationContext(NOTIFICATION_ID_BASE + PAGE_SIZE)
                compose.waitUntil(timeoutMillis = 10_000L) {
                    requestsByPage[1]?.get() == 1 &&
                        compose.onAllNodesWithText(firstNotification)
                            .fetchSemanticsNodes().isNotEmpty()
                }
                scrollToNotification(lastPageOneNotification)
                compose.waitUntil(timeoutMillis = 10_000L) {
                    requestsByPage[2]?.get() == 1
                }
                compose.waitForIdle()

                val lastPageTwoNotification =
                    notificationContext(NOTIFICATION_ID_BASE + PAGE_SIZE * 2L)
                scrollToNotification(lastPageTwoNotification)
                compose.onNodeWithText(lastPageTwoNotification).assertIsDisplayed()

                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.moveToState(Lifecycle.State.RESUMED)
                compose.waitForIdle()

                scenario.onActivity { activity ->
                    activity.supportFragmentManager.beginTransaction()
                        .detach(fragment)
                        .commitNow()
                    activity.supportFragmentManager.beginTransaction()
                        .attach(fragment)
                        .commitNow()
                }
                compose.waitForIdle()

                scrollToNotification(firstNotification)
                compose.onAllNodesWithText(firstNotification).assertCountEquals(1)
                compose.onNodeWithText(firstNotification).assertIsDisplayed()

                assertEquals(1, requestsByPage[1]?.get())
                assertEquals(1, requestsByPage[2]?.get())
                assertNull(requestsByPage[3])
            }
        } finally {
            koin.declare(originalRepository)
        }
    }

    private fun scrollToNotification(notificationText: String) {
        compose.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText(notificationText))
    }

    private fun notificationPage(page: Int): NotificationPageResult {
        val pageStart = NOTIFICATION_ID_BASE + (page - 1L) * PAGE_SIZE
        return NotificationPageResult(
            notifications = (1..PAGE_SIZE).map { index ->
                val id = pageStart + index
                NotificationRecord(
                    id = id,
                    type = KeyUtil.FOLLOWING,
                    context = notificationContext(id),
                    createdAt = NOTIFICATION_TIMESTAMP,
                )
            },
            pageInfo = PageInfoRecord(
                currentPage = page,
                lastPage = 2,
                perPage = PAGE_SIZE,
                total = PAGE_SIZE * 2,
                hasNextPage = page == 1,
                hasPreviousPage = page > 1,
            ),
        )
    }

    private fun notificationContext(id: Long): String = "controlled-notification-$id"

    private companion object {
        const val NOTIFICATION_FRAGMENT_TAG = "notification-lifecycle-regression"
        const val NOTIFICATION_ID_BASE = 8_000_000L
        const val NOTIFICATION_TIMESTAMP = 1_700_000_000L
        const val PAGE_SIZE = 20
    }
}
