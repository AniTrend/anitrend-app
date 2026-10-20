package com.mxt.anitrend.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock(UserRepository::class.java)
        preferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        `when`(preferences.edit()).thenReturn(editor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createSettings(): Settings = Settings(
        context = mock(Context::class.java),
        resources = mock(Resources::class.java),
        preferences = preferences,
    )

    private fun createViewModel(settings: Settings = createSettings()) = MainViewModel(
        userRepository = userRepository,
        settings = settings,
    )

    // ── UiState sealed type ──

    @Test
    fun `UiState Loading is a singleton`() {
        assertEquals(MainViewModel.UiState.Loading, MainViewModel.UiState.Loading)
    }

    @Test
    fun `UiState Success holds user instance`() {
        val user =
            User().apply {
                id = 1L
                name = "TestUser"
            }
        val state = MainViewModel.UiState.Success(user)
        assertEquals(1L, state.user.id)
        assertEquals("TestUser", state.user.name)
    }

    @Test
    fun `UiState Error holds message`() {
        val state = MainViewModel.UiState.Error("Something went wrong")
        assertEquals("Something went wrong", state.message)
        assertTrue(state.message.isNotEmpty())
    }

    // ── initial state ──

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = createViewModel()
        assertTrue(vm.state.value is MainViewModel.UiState.Loading)
    }

    @Test
    fun `loadCurrentUser emits Success from repository result`() = runTest {
        val user =
            User().apply {
                id = 1L
                name = "MainUser"
            }
        doReturn(Result.success(user)).`when`(userRepository).getCurrentUser(asHtml = false)

        val vm = createViewModel()
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Success
        assertEquals(1L, state.user.id)
        assertEquals("MainUser", state.user.name)
    }

    @Test
    fun `loadCurrentUser emits Error from repository failure`() = runTest {
        doReturn(Result.failure<User>(RuntimeException("Network failed")))
            .`when`(userRepository)
            .getCurrentUser(asHtml = false)

        val vm = createViewModel()
        vm.loadCurrentUser()

        val state = vm.state.value as MainViewModel.UiState.Error
        assertEquals("Network failed", state.message)
    }

    // ── UiPreferences ──

    @Test
    fun `uiPreferences seeds from the preference store`() {
        `when`(preferences.getString(any(), anyString())).thenAnswer { invocation ->
            invocation.getArgument(1)
        }

        val vm = createViewModel()

        val prefs = vm.uiPreferences.value
        assertEquals(KeyUtil.THEME_LIGHT, prefs.theme)
        assertEquals(Locale.getDefault().language, prefs.language)
        assertEquals(0, prefs.listViewStyle)
        assertFalse(prefs.displayAdultContent)
    }

    @Test
    fun `uiPreferences effective adult value stays false when the flavor resource forbids it`() {
        `when`(preferences.getString(any(), anyString())).thenAnswer { invocation ->
            invocation.getArgument(1)
        }
        // Stored adult value true behind a mocked flavor resource that
        // resolves false: the effective value must stay false.
        `when`(preferences.getBoolean(any(), eq(false))).thenReturn(true)

        val vm = createViewModel()

        assertFalse(vm.uiPreferences.value.displayAdultContent)
    }

    @Test
    fun `observing uiPreferences never triggers the user fetch`() = runTest {
        val vm = createViewModel()

        vm.uiPreferences.first()

        assertTrue(vm.state.value is MainViewModel.UiState.Loading)
    }
}
