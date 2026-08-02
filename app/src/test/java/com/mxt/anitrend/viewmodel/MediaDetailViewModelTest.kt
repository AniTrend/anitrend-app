package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.data.store.favourite.InMemoryFavouriteStore
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.domain.favourite.interactor.ToggleFavouriteInteractor
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MediaDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mediaRepository: MediaRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var mediaListStore: InMemoryMediaListStore
    private lateinit var feedStore: InMemoryFeedStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mock(MediaRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
        mediaListStore = InMemoryMediaListStore()
        feedStore = InMemoryFeedStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `MediaViewModel routes load through MediaRepository`() = runTest {
        val content = MediaBase()
        doReturn(Result.success(content))
            .`when`(mediaRepository)
            .getMediaBase(1L, MediaType.ANIME, false)
        val viewModel = MediaViewModel(
            mediaRepository = mediaRepository,
            baseRepository = baseRepository,
            mediaListStore = mediaListStore,
            favouriteStore = InMemoryFavouriteStore(),
            toggleFavouriteInteractor = mock(ToggleFavouriteInteractor::class.java),
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(mediaId = 1L, mediaType = "ANIME", showAdult = false)
        advanceUntilIdle()

        val state = viewModel.state.value as MediaViewModel.UiState.Success
        assertEquals(content.id, state.media.id)
        verify(mediaRepository).getMediaBase(1L, MediaType.ANIME, false)
        collector.cancel()
    }

    @Test
    fun `MediaCharacterViewModel routes load through MediaRepository`() = runTest {
        val content = ConnectionContainer<EdgeContainer<CharacterEdge>>()
        doReturn(Result.success(content))
            .`when`(mediaRepository)
            .getMediaCharacters(2L, MediaType.MANGA, null, 3, KeyUtil.PAGING_LIMIT)
        val viewModel = MediaCharacterViewModel(mediaRepository = mediaRepository)

        viewModel.load(mediaId = 2L, type = MediaType.MANGA, page = 3, isAdult = null)

        val state = viewModel.state.value as MediaCharacterViewModel.UiState.Success
        assertSame(content, state.content)
        verify(mediaRepository).getMediaCharacters(2L, MediaType.MANGA, null, 3, KeyUtil.PAGING_LIMIT)
    }

    @Test
    fun `MediaStaffViewModel routes load through MediaRepository`() = runTest {
        val content = ConnectionContainer<EdgeContainer<StaffEdge>>()
        doReturn(Result.success(content))
            .`when`(mediaRepository)
            .getMediaStaff(3L, MediaType.ANIME, false, 4, KeyUtil.PAGING_LIMIT)
        val viewModel = MediaStaffViewModel(mediaRepository = mediaRepository)

        viewModel.load(mediaId = 3L, type = MediaType.ANIME, page = 4, isAdult = false)

        val state = viewModel.state.value as MediaStaffViewModel.UiState.Success
        assertSame(content, state.content)
        verify(mediaRepository).getMediaStaff(3L, MediaType.ANIME, false, 4, KeyUtil.PAGING_LIMIT)
    }

    @Test
    fun `MediaRelationViewModel routes load through MediaRepository`() = runTest {
        val content = ConnectionContainer<EdgeContainer<MediaEdge>>()
        doReturn(Result.success(content))
            .`when`(mediaRepository)
            .getMediaRelations(4L, MediaType.MANGA, false)
        val viewModel = MediaRelationViewModel(mediaRepository = mediaRepository)

        viewModel.load(mediaId = 4L, type = MediaType.MANGA, isAdult = false)

        val state = viewModel.state.value as MediaRelationViewModel.UiState.Success
        assertSame(content, state.content)
        verify(mediaRepository).getMediaRelations(4L, MediaType.MANGA, false)
    }

    @Test
    fun `MediaStatsViewModel routes load through MediaRepository`() = runTest {
        val content = Media()
        doReturn(Result.success(content))
            .`when`(mediaRepository)
            .getMediaStats(5L, MediaType.ANIME, null)
        val viewModel = MediaStatsViewModel(mediaRepository = mediaRepository)

        viewModel.load(mediaId = 5L, type = MediaType.ANIME, isAdult = null)

        val state = viewModel.state.value as MediaStatsViewModel.UiState.Success
        assertSame(content, state.media)
        verify(mediaRepository).getMediaStats(5L, MediaType.ANIME, null)
    }

    @Test
    fun `MediaRecommendationsViewModel routes load through MediaRepository and projects UI items`() = runTest {
        val content = RecommendationPageResult(recommendations = emptyList(), pageInfo = null)
        doReturn(Result.success(content))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                isNull(),
                eq(21),
                isNull(),
            )
        val viewModel = MediaRecommendationsViewModel(
            mediaRepository = mediaRepository,
            dispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val state = viewModel.state.value as MediaRecommendationsViewModel.UiState.Success
        assertEquals(content.recommendations, state.items)
        assertSame(content.pageInfo, state.pageInfo)
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            isNull(),
            eq(21),
            isNull(),
        )
    }
}
