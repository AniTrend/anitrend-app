package com.mxt.anitrend.viewmodel

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
    fun `initial state is Loading`() = runTest {
        val vm = NotificationViewModel(userRepository = userRepository)
        assertTrue(vm.state.value is NotificationViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success exposing the page result directly`() = runTest {
        val content = NotificationPageResult(
            notifications = listOf(
                NotificationRecord(id = 1L, type = "FOLLOWING"),
                NotificationRecord(id = 2L, type = "AIRING"),
            ),
            pageInfo = null,
        )
        doReturn(Result.success(content))
            .`when`(userRepository)
            .getUserNotifications(
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                resetNotificationCount = true,
            )
        val vm = NotificationViewModel(userRepository = userRepository)

        vm.load(page = 2)

        val state = vm.state.value as NotificationViewModel.UiState.Success
        assertEquals(content, state.content)
        assertEquals(listOf(1L, 2L), state.content.notifications.map { it.id })
        assertEquals("FOLLOWING", state.content.notifications.first().type)
        verify(userRepository).getUserNotifications(
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            resetNotificationCount = true,
        )
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<NotificationPageResult>(RuntimeException("Notifications failed")))
            .`when`(userRepository)
            .getUserNotifications(
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                resetNotificationCount = true,
            )
        val vm = NotificationViewModel(userRepository = userRepository)

        vm.load(page = 1)

        val state = vm.state.value as NotificationViewModel.UiState.Error
        assertEquals("Notifications failed", state.message)
    }
}
