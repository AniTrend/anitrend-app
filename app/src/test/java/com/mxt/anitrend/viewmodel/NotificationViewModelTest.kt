package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock(UserRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading with no active request`() = runTest {
        val vm = NotificationViewModel(userRepository = userRepository)

        val state = vm.state.value as NotificationViewModel.UiState.Loading
        assertTrue(state.snapshot.notifications.isEmpty())
        assertEquals(null, state.request)
    }

    @Test
    fun `successful pages publish one deduplicated accumulated snapshot`() = runTest {
        val firstPage = page(1, hasNextPage = true, ids = listOf(1L, 1L, 2L))
        val secondPage = page(2, hasNextPage = false, ids = listOf(2L, 3L))
        stubPage(page = 1, result = Result.success(firstPage))
        stubPage(page = 2, result = Result.success(secondPage))
        val vm = NotificationViewModel(userRepository = userRepository)

        vm.load(page = 1, isRefresh = true)
        val pageOneState = vm.state.value as NotificationViewModel.UiState.Success
        assertEquals(listOf(1L, 2L), pageOneState.snapshot.notifications.map { it.id })
        assertEquals(1, pageOneState.snapshot.lastLoadedPage)
        assertEquals(2, pageOneState.snapshot.nextPage)
        assertTrue(pageOneState.snapshot.hasMore)
        assertEquals(2, pageOneState.snapshot.lastPageUniqueCount)
        assertTrue(pageOneState.request.isRefresh)

        vm.load(page = 2, isRefresh = false)
        val pageTwoState = vm.state.value as NotificationViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), pageTwoState.snapshot.notifications.map { it.id })
        assertEquals(2, pageTwoState.snapshot.lastLoadedPage)
        assertEquals(3, pageTwoState.snapshot.nextPage)
        assertFalse(pageTwoState.snapshot.hasMore)
        assertEquals(1, pageTwoState.snapshot.lastPageUniqueCount)
        assertFalse(pageTwoState.request.isRefresh)
        verify(userRepository).getUserNotifications(
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            resetNotificationCount = true,
        )
        verify(userRepository).getUserNotifications(
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            resetNotificationCount = true,
        )
    }

    @Test
    fun `refresh replaces accumulated snapshot even when the page is empty`() = runTest {
        doReturn(Result.success(page(1, true, listOf(1L))))
            .doReturn(Result.success(page(1, false, emptyList())))
            .`when`(userRepository)
            .getUserNotifications(
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                resetNotificationCount = true,
            )
        stubPage(page = 2, result = Result.success(page(2, true, listOf(2L))))
        val vm = NotificationViewModel(userRepository = userRepository)
        vm.load(page = 1, isRefresh = true)
        vm.load(page = 2, isRefresh = false)
        vm.load(page = 1, isRefresh = true)

        val state = vm.state.value as NotificationViewModel.UiState.Success
        assertTrue(state.snapshot.notifications.isEmpty())
        assertTrue(state.snapshot.hasLoadedPage)
        assertEquals(1, state.snapshot.lastLoadedPage)
        assertEquals(2, state.snapshot.nextPage)
        assertFalse(state.snapshot.hasMore)
        assertTrue(state.request.isRefresh)
    }

    @Test
    fun `failed append keeps the page available for retry`() = runTest {
        stubPage(page = 1, result = Result.success(page(1, true, listOf(1L))))
        doReturn(Result.failure<NotificationPageResult>(RuntimeException("Notifications failed")))
            .doReturn(Result.success(page(2, false, listOf(2L))))
            .`when`(userRepository)
            .getUserNotifications(
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                resetNotificationCount = true,
            )
        val vm = NotificationViewModel(userRepository = userRepository)

        vm.load(page = 1, isRefresh = true)
        vm.load(page = 2, isRefresh = false)

        val failedState = vm.state.value as NotificationViewModel.UiState.Error
        assertEquals(listOf(1L), failedState.snapshot.notifications.map { it.id })
        assertEquals(2, failedState.snapshot.nextPage)
        assertEquals(2, failedState.request.page)
        assertFalse(failedState.request.isRefresh)

        vm.load(page = 2, isRefresh = false)

        val retriedState = vm.state.value as NotificationViewModel.UiState.Success
        assertEquals(listOf(1L, 2L), retriedState.snapshot.notifications.map { it.id })
        assertEquals(3, retriedState.snapshot.nextPage)
    }

    private suspend fun stubPage(page: Int, result: Result<NotificationPageResult>) {
        doReturn(result)
            .`when`(userRepository)
            .getUserNotifications(
                page = page,
                perPage = KeyUtil.PAGING_LIMIT,
                resetNotificationCount = true,
            )
    }

    private fun page(page: Int, hasNextPage: Boolean, ids: List<Long>) = NotificationPageResult(
        notifications = ids.map { NotificationRecord(id = it, type = KeyUtil.FOLLOWING) },
        pageInfo = PageInfoRecord(
            currentPage = page,
            lastPage = null,
            perPage = KeyUtil.PAGING_LIMIT,
            total = null,
            hasNextPage = hasNextPage,
            hasPreviousPage = page > 1,
        ),
    )
}
