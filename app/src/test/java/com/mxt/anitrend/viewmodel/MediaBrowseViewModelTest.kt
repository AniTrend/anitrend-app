package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class MediaBrowseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load forwards season filter to browse repository`() = runTest {
        val baseRepository = mock(BaseRepository::class.java)
        val browseRepository = mock(BrowseRepository::class.java)
        val pageContainer = PageContainer<MediaBase>()
        `when`(browseRepository.mutationEvents).thenReturn(MutableSharedFlow())

        `when`(
            browseRepository.getMediaBrowse(
                page = 1,
                perPage = 50,
                seasonYear = null,
                type = MediaType.ANIME,
                format = null,
                startDateLike = null,
                season = MediaSeason.WINTER,
                genres = null,
                isAdult = false,
                sort = null,
                onList = null,
                status = null,
                tags = null,
            ),
        ).thenReturn(Result.success(pageContainer))

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        viewModel.load(
            type = MediaType.ANIME,
            page = 1,
            pageLimit = 50,
            season = "WINTER",
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
        )

        verify(browseRepository).getMediaBrowse(
            page = 1,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            season = MediaSeason.WINTER,
            genres = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
        )
        assertTrue(viewModel.state.value is MediaBrowseViewModel.UiState.Success)
    }

    @Test
    fun `load maps invalid season filter to null`() = runTest {
        val baseRepository = mock(BaseRepository::class.java)
        val browseRepository = mock(BrowseRepository::class.java)
        val pageContainer = PageContainer<MediaBase>()
        `when`(browseRepository.mutationEvents).thenReturn(MutableSharedFlow())

        `when`(
            browseRepository.getMediaBrowse(
                page = 1,
                perPage = 50,
                seasonYear = null,
                type = MediaType.ANIME,
                format = null,
                startDateLike = null,
                season = null,
                genres = null,
                isAdult = false,
                sort = null,
                onList = null,
                status = null,
                tags = null,
            ),
        ).thenReturn(Result.success(pageContainer))

        val viewModel = MediaBrowseViewModel(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        viewModel.load(
            type = MediaType.ANIME,
            page = 1,
            pageLimit = 50,
            season = "INVALID",
            sort = null,
            isAdult = false,
            format = null,
            seasonYear = null,
            startDateLike = null,
            status = null,
            genres = emptyList(),
            tags = emptyList(),
        )

        verify(browseRepository).getMediaBrowse(
            page = 1,
            perPage = 50,
            seasonYear = null,
            type = MediaType.ANIME,
            format = null,
            startDateLike = null,
            season = null,
            genres = null,
            isAdult = false,
            sort = null,
            onList = null,
            status = null,
            tags = null,
        )
        assertTrue(viewModel.state.value is MediaBrowseViewModel.UiState.Success)
    }

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
