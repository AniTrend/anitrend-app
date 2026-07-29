package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.meta.CustomList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
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
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MediaListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var browseRepository: BrowseRepository
    private lateinit var userRepository: UserRepository
    private lateinit var settings: Settings

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        browseRepository = spy(BrowseRepository(mock(BrowseService::class.java), testDispatcher))
        userRepository = mock(UserRepository::class.java)
        settings = mock(Settings::class.java)
        doReturn(KeyUtil.PROGRESS)
            .`when`(settings)
            .mediaListSort
        doReturn(KeyUtil.DESC)
            .`when`(settings)
            .sortOrder
        doReturn(true)
            .`when`(settings)
            .isAuthenticated
        doReturn(
            User().apply {
                id = 42L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saved media list event patches current items without reload`() = runTest {
        val initialEntry = aMediaList(id = 1L, mediaId = 100L)
        val savedEntry = aMediaList(id = 1L, mediaId = 100L).apply {
            customLists = listOf(CustomList(name = "Favorites", isEnabled = true))
            progress = 9
        }
        doReturn(Result.success(pageContainer(initialEntry)))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 42L,
                userName = null,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.PROGRESS_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_100,
            )

        val vm = MediaListViewModel(
            browseRepository = browseRepository,
            userRepository = userRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        vm.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        advanceUntilIdle()
        vm.onMediaListSaved(savedEntry)

        val state = vm.state.value as MediaListViewModel.UiState.Success
        assertEquals(9, state.items.single().progress)
        assertEquals("Favorites", state.items.single().customLists?.single()?.name)
        verify(browseRepository, times(1)).getMediaListCollection(
            userId = 42L,
            userName = null,
            type = MediaType.ANIME,
            forceSingleCompletedList = true,
            sort = listOf(MediaListSort.PROGRESS_DESC),
            statusIn = listOf(MediaListStatus.CURRENT),
            scoreFormat = ScoreFormat.POINT_100,
        )
    }

    @Test
    fun `saved media list event removes item when status no longer matches loaded filter`() = runTest {
        val initialEntry = aMediaList(id = 1L, mediaId = 100L)
        val movedEntry = aMediaList(id = 1L, mediaId = 100L, status = KeyUtil.COMPLETED)
        doReturn(Result.success(pageContainer(initialEntry)))
            .`when`(browseRepository)
            .getMediaListCollection(
                userId = 42L,
                userName = null,
                type = MediaType.ANIME,
                forceSingleCompletedList = true,
                sort = listOf(MediaListSort.PROGRESS_DESC),
                statusIn = listOf(MediaListStatus.CURRENT),
                scoreFormat = ScoreFormat.POINT_100,
            )

        val vm = MediaListViewModel(
            browseRepository = browseRepository,
            userRepository = userRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        vm.load(userId = 42L, userName = null, mediaType = KeyUtil.ANIME, statusIn = KeyUtil.CURRENT)
        advanceUntilIdle()
        vm.onMediaListSaved(movedEntry)

        val state = vm.state.value as MediaListViewModel.UiState.Success
        assertTrue(state.items.isEmpty())
        assertTrue(state.isEmpty)
    }

    private fun pageContainer(vararg entries: MediaList): PageContainer<MediaListCollection> = PageContainer<MediaListCollection>().apply {
        pageData = listOf(
            newMediaListCollection(
                status = KeyUtil.CURRENT,
                entries = entries.toList(),
            ),
        )
    }

    private fun newMediaListCollection(
        status: String,
        entries: List<MediaList>,
    ): MediaListCollection = mock(MediaListCollection::class.java).apply {
        this.status = status
        doReturn(entries).`when`(this).entries
    }
}
