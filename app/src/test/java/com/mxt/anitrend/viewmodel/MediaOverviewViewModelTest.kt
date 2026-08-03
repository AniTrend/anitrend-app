package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewCoverImageRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewStudioRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Focused tests for the record-backed media overview pipeline: repository
 * routing, success state exposure, and display-data transformation semantics.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaOverviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mediaRepository: MediaRepository
    private lateinit var settings: Settings

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mock(MediaRepository::class.java)
        settings = mock(Settings::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load routes through getMediaOverviewRecord and exposes the record in Success`() = runTest {
        val record = overviewRecord(type = "ANIME")
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        val state = viewModel.state.value as MediaOverviewViewModel.UiState.Success
        assertSame(record, state.record)
        verify(mediaRepository).getMediaOverviewRecord(21L, MediaType.ANIME, false)
        collector.cancel()
    }

    @Test
    fun `load exposes error state when repository fails`() = runTest {
        doReturn(Result.failure<MediaOverviewRecord>(RuntimeException("boom")))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        val state = viewModel.state.value as MediaOverviewViewModel.UiState.Error
        assertEquals("boom", state.message)
    }

    @Test
    fun `load respects displayAdultContent flag when building isAdult filter`() = runTest {
        doReturn(true).`when`(settings).displayAdultContent
        val record = overviewRecord(type = "ANIME")
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, null)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        verify(mediaRepository).getMediaOverviewRecord(21L, MediaType.ANIME, null)
    }

    @Test
    fun `transformToDisplayData preserves hashtag html and capitalization`() = runTest {
        val record = overviewRecord(
            type = "ANIME",
            hashtag = "#NoGameNoLife",
            format = "TV",
            source = "ORIGINAL",
            status = "FINISHED",
        )
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        val data = viewModel.displayData.value
        assertEquals(
            "<a href=\"https://twitter.com/search?q=%23NoGameNoLife&src=typd\">#NoGameNoLife</a>",
            data?.hashTagHtml,
        )
        // capitalizeWords appends a trailing space for single-word inputs; this
        // matches the legacy Media-backed lane exactly.
        assertEquals("TV ", data?.formatText)
        assertEquals("Original ", data?.sourceText)
        assertEquals("Finished ", data?.statusText)
    }

    @Test
    fun `transformToDisplayData resolves season from a valid start date`() = runTest {
        val record = overviewRecord(
            type = "ANIME",
            startDate = FuzzyDateRecord(year = 2017, month = 10, day = 1),
        )
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        // The season resolver inherits the legacy capitalizeWords trailing-space
        // behavior, so the season text carries the double space.
        assertEquals("Fall  2017", viewModel.displayData.value?.seasonText)
    }

    @Test
    fun `transformToDisplayData keeps season null for invalid start date`() = runTest {
        val record = overviewRecord(
            type = "ANIME",
            startDate = FuzzyDateRecord(year = null, month = null, day = null),
        )
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        assertNull(viewModel.displayData.value?.seasonText)
    }

    @Test
    fun `transformToDisplayData sets type flags from record type`() = runTest {
        val anime = overviewRecord(type = "ANIME")
        val manga = overviewRecord(type = "MANGA")
        doReturn(Result.success(anime))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(1L, MediaType.ANIME, false)
        doReturn(Result.success(manga))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(2L, MediaType.MANGA, false)

        val animeViewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )
        animeViewModel.load(mediaId = 1L, mediaType = "ANIME")
        advanceUntilIdle()
        var data = animeViewModel.displayData.value
        assertTrue(data?.isAnime == true)
        assertFalse(data?.isManga == true)

        val mangaViewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )
        mangaViewModel.load(mediaId = 2L, mediaType = "MANGA")
        advanceUntilIdle()
        data = mangaViewModel.displayData.value
        assertTrue(data?.isManga == true)
        assertFalse(data?.isAnime == true)
    }

    @Test
    fun `transformToDisplayData keeps only positive counts and passes score through`() = runTest {
        val record = overviewRecord(
            type = "ANIME",
            duration = 24,
            episodes = 12,
            volumes = 0,
            chapters = -1,
            meanScore = 78,
        )
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        val data = viewModel.displayData.value
        assertEquals(24, data?.episodeDuration)
        assertEquals(12, data?.episodeCount)
        assertNull(data?.volumeCount)
        assertNull(data?.chapterCount)
        assertEquals(78, data?.meanScore)
    }

    @Test
    fun `transformToDisplayData takes main studio and genres from the record`() = runTest {
        val record = overviewRecord(
            type = "ANIME",
            genres = listOf("Action", "Comedy", ""),
            studios = listOf(
                MediaOverviewStudioRecord(
                    id = 9L,
                    name = "Studio Trigger",
                    isAnimationStudio = true,
                    siteUrl = null,
                    isFavourite = false,
                ),
            ),
        )
        doReturn(Result.success(record))
            .`when`(mediaRepository)
            .getMediaOverviewRecord(21L, MediaType.ANIME, false)
        val viewModel = MediaOverviewViewModel(
            repository = mediaRepository,
            settings = settings,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(mediaId = 21L, mediaType = "ANIME")
        advanceUntilIdle()

        val data = viewModel.displayData.value
        assertEquals("Studio Trigger", data?.mainStudioName)
        assertEquals(9L, data?.mainStudio?.id)
        assertEquals(listOf("Action", "Comedy"), data?.genres?.map { it.genre })
    }

    private fun overviewRecord(
        type: String? = "ANIME",
        hashtag: String? = null,
        format: String? = "TV",
        source: String? = "ORIGINAL",
        status: String? = "FINISHED",
        startDate: FuzzyDateRecord? = null,
        duration: Int? = null,
        episodes: Int? = null,
        volumes: Int? = null,
        chapters: Int? = null,
        meanScore: Int? = null,
        genres: List<String?>? = null,
        studios: List<MediaOverviewStudioRecord>? = null,
    ) = MediaOverviewRecord(
        id = 21L,
        titleUserPreferred = "No Game No Life",
        titleRomaji = "No Game No Life",
        titleEnglish = "No Game No Life",
        titleOriginal = "ノーゲーム・ノーライフ",
        bannerImage = null,
        coverImage = MediaOverviewCoverImageRecord(
            color = null,
            extraLarge = "https://example.com/cover_extra.jpg",
            large = "https://example.com/cover_large.jpg",
            medium = "https://example.com/cover_medium.jpg",
        ),
        type = type,
        format = format,
        season = "FALL",
        status = status,
        meanScore = meanScore,
        averageScore = meanScore,
        startDate = startDate,
        endDate = null,
        episodes = episodes,
        chapters = chapters,
        volumes = volumes,
        isAdult = false,
        isFavourite = false,
        nextAiringEpisode = null,
        mediaListEntry = null,
        siteUrl = null,
        updatedAt = null,
        genres = genres,
        tags = null,
        trailer = null,
        duration = duration,
        hashtag = hashtag,
        source = source,
        studios = studios,
        description = "Some description",
    )
}
