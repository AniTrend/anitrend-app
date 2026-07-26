package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
import org.junit.Assert.assertSame
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
    fun `load emits Success from repository result`() = runTest {
        val content = PageContainer<Notification>().apply {
            pageData = listOf(Notification().apply { id = 1L })
        }
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
        assertSame(content, state.content)
        verify(userRepository).getUserNotifications(
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            resetNotificationCount = true,
        )
    }

    @Test
    fun `load emits Error from repository failure`() = runTest {
        doReturn(Result.failure<PageContainer<Notification>>(RuntimeException("Notifications failed")))
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
