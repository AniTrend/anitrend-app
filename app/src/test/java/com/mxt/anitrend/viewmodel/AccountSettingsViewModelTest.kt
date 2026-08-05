package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.domain.user.model.UserSettingsUpdate
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Focused tests for the account settings ViewModel: cache seeding, sparse
 * dirty tracking, server refresh, save/merge state reduction, failure
 * retention, duplicate-save protection and discard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── seeding ──

    @Test
    fun `state seeds from the cached current user`() {
        val user = createUser(about = "Cached bio")
        val viewModel = createViewModel(cachedUser = user)

        val state = viewModel.state.value
        assertEquals("Cached bio", state.about)
        assertEquals("blue", state.profileColor)
        assertEquals("POINT_10", state.scoreFormat)
        assertEquals("ROMAJI", state.titleLanguage)
        assertEquals("CUSTOM", state.rowOrder)
        assertEquals(true, state.airingNotifications)
        assertEquals(false, state.displayAdultContent)
        assertFalse(state.hasDirtyFields)
    }

    @Test
    fun `state seeds with defaults when the cache is missing`() {
        val viewModel = createViewModel(cachedUser = null)

        val state = viewModel.state.value
        assertEquals("", state.about)
        assertNull(state.profileColor)
        assertEquals(AccountSettingsOptions.DEFAULT_SCORE_FORMAT, state.scoreFormat)
        assertEquals(AccountSettingsOptions.DEFAULT_TITLE_LANGUAGE, state.titleLanguage)
        assertNull(state.rowOrder)
        assertEquals(false, state.airingNotifications)
        assertEquals(false, state.displayAdultContent)
        assertFalse(state.hasDirtyFields)
    }

    // ── constrained setters ──

    @Test
    fun `setters accept only constrained values`() {
        val viewModel = createViewModel(cachedUser = createUser())

        viewModel.setProfileColor("not-a-color")
        viewModel.setScoreFormat("not-a-format")
        viewModel.setTitleLanguage("not-a-language")
        viewModel.setRowOrder("arbitrary free text")

        val state = viewModel.state.value
        assertEquals("blue", state.profileColor)
        assertEquals("POINT_10", state.scoreFormat)
        assertEquals("ROMAJI", state.titleLanguage)
        assertEquals("CUSTOM", state.rowOrder)
        assertFalse(state.hasDirtyFields)

        viewModel.setProfileColor("purple")
        viewModel.setScoreFormat("POINT_5")
        viewModel.setTitleLanguage("NATIVE")
        viewModel.setRowOrder(AccountSettingsOptions.ROW_ORDER_CUSTOM)

        val updated = viewModel.state.value
        assertEquals("purple", updated.profileColor)
        assertEquals("POINT_5", updated.scoreFormat)
        assertEquals("NATIVE", updated.titleLanguage)
        assertEquals("CUSTOM", updated.rowOrder)
    }

    // ── no-op constrained selections ──

    @Test
    fun `selecting the current constrained value does not mark the field dirty`() {
        val viewModel = createViewModel(cachedUser = createUser())

        viewModel.setProfileColor("blue")
        viewModel.setScoreFormat("POINT_10")
        viewModel.setTitleLanguage("ROMAJI")
        viewModel.setRowOrder(AccountSettingsOptions.ROW_ORDER_CUSTOM)

        val state = viewModel.state.value
        assertFalse(state.profileColorDirty)
        assertFalse(state.scoreFormatDirty)
        assertFalse(state.titleLanguageDirty)
        assertFalse(state.rowOrderDirty)
        assertFalse(state.hasDirtyFields)
    }

    @Test
    fun `re selecting a pending value keeps the edit dirty`() {
        val viewModel = createViewModel(cachedUser = createUser())

        viewModel.setProfileColor("purple")
        assertTrue(viewModel.state.value.profileColorDirty)

        // Re-selecting the same pending value must not clear the pending edit.
        viewModel.setProfileColor("purple")

        val state = viewModel.state.value
        assertEquals("purple", state.profileColor)
        assertTrue(state.profileColorDirty)
        assertTrue(state.hasDirtyFields)
    }

    @Test
    fun `no-op selection keeps the error message and other dirty fields intact`() = runTest {
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)
        viewModel.setAbout("New bio")
        doReturn(Result.failure<UserSettingsRecord>(RuntimeException("Server exploded")))
            .`when`(repository)
            .updateUser(UserSettingsUpdate(about = "New bio"))
        viewModel.save()
        assertEquals("Server exploded", viewModel.state.value.errorMessage)

        // Selecting the seeded value is a no-op: it must not clear the error
        // or the unrelated pending about edit.
        viewModel.setProfileColor("blue")

        val state = viewModel.state.value
        assertEquals("Server exploded", state.errorMessage)
        assertEquals("New bio", state.about)
        assertTrue(state.aboutDirty)
        assertFalse(state.profileColorDirty)
    }

    // ── sparse dirty payload ──

    @Test
    fun `save sends only the dirty fields in the sparse update`() = runTest {
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)

        viewModel.setAbout("New bio")
        viewModel.setScoreFormat("POINT_5")
        val sparse = UserSettingsUpdate(about = "New bio", scoreFormat = "POINT_5")
        doReturn(Result.success(serverRecord())).`when`(repository).updateUser(sparse)

        viewModel.save()

        verify(repository).updateUser(sparse)
        assertFalse(viewModel.state.value.hasDirtyFields)
    }

    @Test
    fun `save with no dirty fields does not call the repository`() = runTest {
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)

        viewModel.save()

        verify(repository, never()).updateUser(UserSettingsUpdate())
    }

    // ── successful save ──

    @Test
    fun `successful save reduces the form to the server record and clears dirty flags`() = runTest {
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)
        viewModel.setAbout("New bio")
        viewModel.setProfileColor("purple")
        viewModel.setDisplayAdultContent(true)
        val sparse = UserSettingsUpdate(about = "New bio", profileColor = "purple", displayAdultContent = true)
        doReturn(
            Result.success(
                serverRecord(
                    about = "New bio",
                    profileColor = "purple",
                    displayAdultContent = true,
                ),
            ),
        ).`when`(repository).updateUser(sparse)

        viewModel.save()

        val state = viewModel.state.value
        assertEquals("New bio", state.about)
        assertEquals("purple", state.profileColor)
        assertEquals(true, state.displayAdultContent)
        assertFalse(state.hasDirtyFields)
        assertFalse(state.isSaving)
        assertNull(state.errorMessage)
    }

    // ── failed save ──

    @Test
    fun `failed save keeps the edits and reports the error`() = runTest {
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)
        viewModel.setAbout("New bio")
        doReturn(Result.failure<UserSettingsRecord>(RuntimeException("Server exploded")))
            .`when`(repository)
            .updateUser(UserSettingsUpdate(about = "New bio"))

        viewModel.save()

        val state = viewModel.state.value
        assertEquals("New bio", state.about)
        assertTrue(state.aboutDirty)
        assertFalse(state.isSaving)
        assertEquals("Server exploded", state.errorMessage)
    }

    // ── duplicate-save protection ──

    @Test
    fun `duplicate save calls are ignored while a save is in flight`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)
        viewModel.setAbout("New bio")
        val sparse = UserSettingsUpdate(about = "New bio")
        doReturn(Result.success(serverRecord(about = "New bio"))).`when`(repository).updateUser(sparse)

        viewModel.save()
        viewModel.save()

        // Nothing has executed yet: the launch is queued on the controlled main dispatcher.
        verify(repository, never()).updateUser(sparse)
        assertTrue(viewModel.state.value.isSaving)

        // The single queued save runs; the second call was rejected by the guard.
        advanceUntilIdle()
        verify(repository, times(1)).updateUser(sparse)
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.hasDirtyFields)
    }

    // ── edits during save ──

    @Test
    fun `fields edited while saving keep their edits when the save completes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)
        viewModel.setAbout("New bio")
        viewModel.setProfileColor("purple")
        val sparse = UserSettingsUpdate(about = "New bio", profileColor = "purple")
        doReturn(Result.success(serverRecord(about = "New bio", profileColor = "purple")))
            .`when`(repository)
            .updateUser(sparse)

        viewModel.save()
        // Edit a field that was not part of the in-flight save.
        viewModel.setTitleLanguage("ENGLISH")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("New bio", state.about)
        assertFalse(state.aboutDirty)
        assertEquals("purple", state.profileColor)
        assertFalse(state.profileColorDirty)
        // The mid-save edit survives and stays dirty for the next save.
        assertEquals("ENGLISH", state.titleLanguage)
        assertTrue(state.titleLanguageDirty)
        assertTrue(state.hasDirtyFields)
    }

    // ── discard ──

    @Test
    fun `discard resets the form to the cached values and clears dirty flags`() {
        val repository = createRepository(cachedUser = createUser(about = "Cached bio"))
        val viewModel = createViewModel(repository = repository)
        viewModel.setAbout("Uncommitted bio")
        viewModel.setProfileColor("purple")
        assertTrue(viewModel.state.value.hasDirtyFields)

        viewModel.discard()

        val state = viewModel.state.value
        assertEquals("Cached bio", state.about)
        assertEquals("blue", state.profileColor)
        assertFalse(state.hasDirtyFields)
        assertNull(state.errorMessage)
    }

    // ── refresh ──

    @Test
    fun `refresh reseeds the form from the server response`() = runTest {
        val repository = createRepository(cachedUser = createUser(about = "Cached bio"))
        val viewModel = createViewModel(repository = repository)
        doReturn(Result.success(createUser(about = "Server bio", profileColor = "green")))
            .`when`(repository)
            .getCurrentUser(asHtml = false)

        viewModel.refresh()

        val state = viewModel.state.value
        assertEquals("Server bio", state.about)
        assertEquals("green", state.profileColor)
        assertFalse(state.isLoading)
        assertFalse(state.hasDirtyFields)
    }

    @Test
    fun `failed refresh keeps the current form and reports the error`() = runTest {
        val repository = createRepository(cachedUser = createUser(about = "Cached bio"))
        val viewModel = createViewModel(repository = repository)
        doReturn(Result.failure<User>(RuntimeException("Network down")))
            .`when`(repository)
            .getCurrentUser(asHtml = false)

        viewModel.refresh()

        val state = viewModel.state.value
        assertEquals("Cached bio", state.about)
        assertFalse(state.isLoading)
        assertEquals("Network down", state.errorMessage)
    }

    @Test
    fun `refresh while a refresh is in flight is ignored`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = createRepository(cachedUser = createUser())
        val viewModel = createViewModel(repository = repository)
        doReturn(Result.success(createUser(about = "Server bio")))
            .`when`(repository)
            .getCurrentUser(asHtml = false)

        viewModel.refresh()
        viewModel.refresh()

        // Nothing has executed yet: the launch is queued on the controlled main dispatcher.
        verify(repository, never()).getCurrentUser(asHtml = false)
        assertTrue(viewModel.state.value.isLoading)

        // The single queued refresh runs; the second call was rejected by the guard.
        advanceUntilIdle()
        verify(repository, times(1)).getCurrentUser(asHtml = false)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Server bio", viewModel.state.value.about)
    }

    // ── helpers ──

    private fun createViewModel(
        repository: UserRepository = createRepository(),
        cachedUser: User? = null,
    ): AccountSettingsViewModel {
        if (cachedUser != null) {
            `when`(repository.cachedCurrentUser).thenReturn(cachedUser)
        }
        return AccountSettingsViewModel(
            userRepository = repository,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    private fun createRepository(cachedUser: User? = null): UserRepository {
        val repository = mock(UserRepository::class.java)
        `when`(repository.cachedCurrentUser).thenReturn(cachedUser)
        return repository
    }

    private fun createUser(
        about: String? = "Cached bio",
        profileColor: String? = "blue",
        titleLanguage: String? = "ROMAJI",
        rowOrder: String? = "CUSTOM",
    ): User = User().also {
        it.id = 7L
        it.name = "mxt"
        it.about = about
        it.options = UserOptions(
            titleLanguage = titleLanguage,
            isDisplayAdultContent = false,
            isAiringNotifications = true,
            profileColor = profileColor,
        )
        it.mediaListOptions = MediaListOptions(scoreFormat = "POINT_10", rowOrder = rowOrder)
    }

    private fun serverRecord(
        about: String? = "New bio",
        profileColor: String? = "purple",
        titleLanguage: String? = "NATIVE",
        displayAdultContent: Boolean? = true,
        airingNotifications: Boolean? = false,
        scoreFormat: String? = "POINT_5",
        rowOrder: String? = "CUSTOM",
    ): UserSettingsRecord = UserSettingsRecord(
        id = 7L,
        about = about,
        titleLanguage = titleLanguage,
        displayAdultContent = displayAdultContent,
        airingNotifications = airingNotifications,
        profileColor = profileColor,
        scoreFormat = scoreFormat,
        rowOrder = rowOrder,
    )

    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
