package com.mxt.anitrend.viewmodel

import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.graphql.generated.CharacterActorsVariables
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterBaseVariables
import com.mxt.anitrend.graphql.generated.CharacterMediaVariables
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.graphql.generated.CharacterOverviewVariables
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.api.retro.anilist.CharacterService
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.repository.StaffRepository
import com.mxt.anitrend.util.KeyUtil
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Request
import okio.Timeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MediaFormatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var characterRepository: CharacterRepository
    private lateinit var staffRepository: StaffRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        characterRepository = mock(CharacterRepository::class.java)
        staffRepository = mock(StaffRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val vm = viewModel()
        assertTrue(vm.state.value is MediaFormatViewModel.UiState.Loading)
    }

    @Test
    fun `page one plus overlapping page two gives each id once and correct grouped header counts`() = runTest(testDispatcher) {
        val pageOneMedia2 = media(2L, "TV")
        val pageOneMedia3 = media(3L, "MOVIE")
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"), pageOneMedia2, pageOneMedia3)
        val pageTwoMedia2 = media(2L, "TV")
        val pageTwoMedia3 = media(3L, "MOVIE")
        stubCharacterPage(id = OWNER_ID, page = 2, pageTwoMedia2, pageTwoMedia3, media(4L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L, 2L, 3L, 4L)
        // Repeated ids from page two replace the page-one server objects in the snapshot.
        val mediaItems = state.items.filterIsInstance<MediaBase>()
        assertSame(pageTwoMedia2, mediaItems.first { it.id == 2L })
        assertSame(pageTwoMedia3, mediaItems.first { it.id == 3L })
        val headers = state.items.filterIsInstance<RecyclerHeaderItem>()
        // capitalizeWords appends a trailing space per word; trim for the display title.
        assertEquals(setOf("Movie", "TV"), headers.map { it.getTitle().trim() }.toSet())
        assertEquals(1, headers.first { it.getTitle().trim() == "Movie" }.size)
        assertEquals(3, headers.first { it.getTitle().trim() == "TV" }.size)
        assertEquals(6, state.items.size)
    }

    @Test
    fun `repeated page two is idempotent`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"), media(2L, "TV"), media(3L, "MOVIE"))
        stubCharacterPage(id = OWNER_ID, page = 2, media(2L, "TV"), media(3L, "MOVIE"), media(4L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        advanceUntilIdle()
        val afterFirstPageTwo = vm.state.value as MediaFormatViewModel.UiState.Success

        // A repeated page two after it was accepted is ignored: no duplicate request, no regression.
        vm.loadPage(page = 2)
        advanceUntilIdle()
        val afterSecondPageTwo = vm.state.value as MediaFormatViewModel.UiState.Success

        assertEquals(afterFirstPageTwo, afterSecondPageTwo)
        assertMediaIdsOnce(afterSecondPageTwo.items, 1L, 2L, 3L, 4L)
        verify(characterRepository, times(1))
            .getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `page one replaces old contents including empty success`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"), media(2L, "TV"), media(3L, "MOVIE"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        assertEquals(5, (vm.state.value as MediaFormatViewModel.UiState.Success).items.size)

        // Re-stub page one with an empty page to simulate an empty refresh response.
        stubCharacterEmpty(id = OWNER_ID, page = 1)
        vm.loadPage(page = 1)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertTrue(state.items.isEmpty())
        assertTrue(state.isEmpty)
    }

    @Test
    fun `duplicate page one for the same active query is coalesced while the reset is in flight`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"), media(2L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        vm.loadPage(page = 1)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L, 2L)
        verify(characterRepository, times(1))
            .getCharacterMedia(id = OWNER_ID, page = 1, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `pre refresh page two success cannot overwrite refresh state`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPage(id = OWNER_ID, page = 2, media(2L, "MOVIE"), media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 1)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L)
        verify(characterRepository, times(2))
            .getCharacterMedia(id = OWNER_ID, page = 1, perPage = KeyUtil.PAGING_LIMIT, type = null)
        verify(characterRepository, times(1))
            .getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `pre refresh page two failure cannot overwrite refresh state`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterFailure(id = OWNER_ID, page = 2, message = "stale failure")
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 1)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L)
        verify(characterRepository, times(2))
            .getCharacterMedia(id = OWNER_ID, page = 1, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `failed page two retry succeeds and pagination continues without duplication`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterFailure(id = OWNER_ID, page = 2, message = "retry me")
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        advanceUntilIdle()

        var state: MediaFormatViewModel.UiState = vm.state.value
        assertEquals("retry me", (state as MediaFormatViewModel.UiState.Error).message)

        // The failed page was not accepted, so it remains retryable.
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = false, media(3L, "TV"))
        vm.loadPage(page = 2)
        advanceUntilIdle()

        state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L), mediaIds(state.items))
        assertTrue(state.pageInfo?.hasNextPage() ?: false)

        // Continuation after the retry works normally.
        vm.loadPage(page = 3)
        advanceUntilIdle()

        state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), mediaIds(state.items))
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
        verify(characterRepository, times(2))
            .getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
        verify(characterRepository, times(1))
            .getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `same page concurrent loads cannot duplicate or regress state`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPage(id = OWNER_ID, page = 2, media(2L, "MOVIE"), media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 2)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L, 2L, 3L)
        verify(characterRepository, times(1))
            .getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `page two and page three apply in order when requested sequentially`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = false, media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        advanceUntilIdle()

        var state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L), mediaIds(state.items))
        assertTrue(state.pageInfo?.hasNextPage() ?: false)

        vm.loadPage(page = 3)
        advanceUntilIdle()

        state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), mediaIds(state.items))
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
    }

    @Test
    fun `page three requested while page two is in flight is deferred and applies in order`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = false, media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 3)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        // Pages apply strictly in ascending order: page three cannot render before page two.
        assertEquals(listOf(1L, 2L, 3L), mediaIds(state.items))
        // Metadata belongs to the highest accepted page, never a lower page's response.
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
        val order = inOrder(characterRepository)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `page three and page four requested while page two is in flight both apply in order`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = true, media(3L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 4, hasNextPage = false, media(4L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 3)
        vm.loadPage(page = 4)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        // Neither requested page is skipped: both apply in ascending order.
        assertEquals(listOf(1L, 2L, 3L, 4L), mediaIds(state.items))
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
        val order = inOrder(characterRepository)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 4, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `direct page four after settled page one is queued and pages two and three drain it in order`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = true, media(3L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 4, hasNextPage = false, media(4L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        // Page four is not the next contiguous page, so it must be queued, not launched.
        vm.loadPage(page = 4)
        advanceUntilIdle()

        var state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L), mediaIds(state.items))
        verify(characterRepository, never()).getCharacterMedia(id = OWNER_ID, page = 4, perPage = KeyUtil.PAGING_LIMIT, type = null)

        // Requesting the missing pages drains the queue strictly in order.
        vm.loadPage(page = 2)
        advanceUntilIdle()
        vm.loadPage(page = 3)
        advanceUntilIdle()

        state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), mediaIds(state.items))
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
        val order = inOrder(characterRepository)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 4, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `page four without page three is retained until page three is requested`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = true, media(3L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 4, hasNextPage = false, media(4L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 4)
        advanceUntilIdle()

        var state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L), mediaIds(state.items))
        verify(characterRepository, never()).getCharacterMedia(id = OWNER_ID, page = 4, perPage = KeyUtil.PAGING_LIMIT, type = null)

        vm.loadPage(page = 3)
        advanceUntilIdle()

        state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), mediaIds(state.items))
        val order = inOrder(characterRepository)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
        order.verify(characterRepository).getCharacterMedia(id = OWNER_ID, page = 4, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `terminal page two clears queued pages and page three is never requested`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = false)
        stubCharacterPage(id = OWNER_ID, page = 3, media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 3)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L)
        assertTrue(state.isEmpty)
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
        verify(characterRepository, never()).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `page three requested after terminal page two is rejected without a repository call`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = false)
        stubCharacterPage(id = OWNER_ID, page = 3, media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        advanceUntilIdle()

        val before = vm.state.value as MediaFormatViewModel.UiState.Success
        assertTrue(before.isEmpty)
        assertFalse(before.pageInfo?.hasNextPage() ?: true)

        // The generation ended with page two: a later page three must be rejected.
        vm.loadPage(page = 3)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(before, state)
        verify(characterRepository, never()).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `lower page retry after accepted progress is ignored and metadata is preserved`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 2, hasNextPage = true, media(2L, "TV"))
        stubCharacterPageWithInfo(id = OWNER_ID, page = 3, hasNextPage = true, media(3L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 3)
        advanceUntilIdle()

        val before = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), mediaIds(before.items))
        assertTrue(before.pageInfo?.hasNextPage() ?: false)

        // A retry of the already accepted page two cannot change state or metadata.
        vm.loadPage(page = 2)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(before, state)
        verify(characterRepository, times(1)).getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
        verify(characterRepository, times(1)).getCharacterMedia(id = OWNER_ID, page = 3, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `page three is launched only after a suspended page two response is released`() = runTest(testDispatcher) {
        // A real repository over a fake service: page two's response signals entry and
        // then blocks on a latch, so it is genuinely in flight while page three is requested.
        val service = GatedCharacterService()
        characterRepository = CharacterRepository(service, testDispatcher)
        val vm = viewModel()

        service.stubPage(page = 1, container = characterPage(media(1L, "TV")))
        service.stubPage(page = 2, container = characterPageWithInfo(page = 2, hasNextPage = true, media(2L, "TV")))
        service.stubPage(page = 3, container = characterPageWithInfo(page = 3, hasNextPage = false, media(3L, "TV")))
        service.holdPage(2)

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        vm.loadPage(page = 3)

        // The scheduler is driven from a helper thread; page two's response blocks there
        // after signalling entry, so the main thread can assert and release it deterministically.
        val worker = thread { testScheduler.advanceUntilIdle() }
        assertTrue("page two response was never entered", service.awaitEntry(LATCH_TIMEOUT_MILLIS))
        assertTrue("page three must not be requested while page two is held", 3 !in service.requestedPages)
        service.releasePage()
        worker.join(LATCH_TIMEOUT_MILLIS)
        assertFalse("scheduler worker did not finish after page two was released", worker.isAlive)

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), mediaIds(state.items))
        assertFalse(state.pageInfo?.hasNextPage() ?: true)
        // Page three was requested only after page two's response was released and applied.
        assertEquals(listOf(1, 2, 3), service.requestedPages)
    }

    @Test
    fun `unrelated query results cannot merge into the active snapshot`() = runTest(testDispatcher) {
        // Query A: character id 1. Query B: character id 2.
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"), media(2L, "TV"))
        stubCharacterPage(id = OWNER_ID, page = 2, media(3L, "TV"))
        stubCharacterPage(id = OTHER_ID, page = 1, media(10L, "TV"))
        stubCharacterPage(id = OTHER_ID, page = 2, media(11L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        // Query A page two is in flight when the host switches to query B.
        vm.loadPage(page = 2)
        vm.loadPage(id = OTHER_ID, page = 1)
        advanceUntilIdle()

        var state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 10L)

        // A stray page two for query A after the switch must not merge.
        vm.loadPage(page = 2)
        vm.loadPage(id = OTHER_ID, page = 2)
        advanceUntilIdle()

        state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 10L, 11L)
        verify(characterRepository, times(1)).getCharacterMedia(id = OWNER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
        verify(characterRepository, times(1)).getCharacterMedia(id = OTHER_ID, page = 2, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    @Test
    fun `re collected state flow success renders complete snapshot by replacement not append`() = runTest(testDispatcher) {
        stubCharacterPage(id = OWNER_ID, page = 1, media(1L, "TV"), media(2L, "TV"))
        stubCharacterPage(id = OWNER_ID, page = 2, media(3L, "MOVIE"), media(4L, "TV"))
        val vm = viewModel()

        vm.loadPage(page = 1)
        advanceUntilIdle()
        vm.loadPage(page = 2)
        advanceUntilIdle()

        // A fresh collector (e.g. a fragment re-entering STARTED) replays the current
        // Success; it must be the complete snapshot, never an append delta.
        val replayed = vm.state.take(1).toList()
        val success = replayed.filterIsInstance<MediaFormatViewModel.UiState.Success>()
        assertEquals(1, success.size)
        assertEquals(vm.state.value, success.single())
        assertMediaIdsOnce(success.single().items, 1L, 2L, 3L, 4L)
    }

    @Test
    fun `staff request path preserves onList and mediaType arguments`() = runTest(testDispatcher) {
        doReturn(Result.success(staffPage(media(1L, "TV"))))
            .`when`(staffRepository)
            .getStaffMedia(id = OWNER_ID, onList = true, page = 1, perPage = KeyUtil.PAGING_LIMIT, type = MediaType.ANIME)
        val vm = viewModel()

        vm.loadPage(onList = true, mediaType = "ANIME", page = 1, requestType = KeyUtil.STAFF_MEDIA_REQ)
        advanceUntilIdle()

        val state = vm.state.value as MediaFormatViewModel.UiState.Success
        assertMediaIdsOnce(state.items, 1L)
        verify(staffRepository)
            .getStaffMedia(id = OWNER_ID, onList = true, page = 1, perPage = KeyUtil.PAGING_LIMIT, type = MediaType.ANIME)
    }

    private fun viewModel() = MediaFormatViewModel(
        characterRepository = characterRepository,
        staffRepository = staffRepository,
    )

    private fun MediaFormatViewModel.loadPage(
        id: Long = OWNER_ID,
        onList: Boolean? = null,
        mediaType: String? = null,
        page: Int,
        requestType: Int = KeyUtil.CHARACTER_MEDIA_REQ,
    ) = load(id = id, onList = onList, mediaType = mediaType, page = page, requestType = requestType)

    private fun media(
        id: Long,
        format: String,
    ): MediaBase = MediaBase().apply {
        this.id = id
        this.format = format
    }

    private fun characterPage(vararg media: MediaBase): ConnectionContainer<PageContainer<MediaBase>> = ConnectionContainer<PageContainer<MediaBase>>().apply {
        connection =
            PageContainer<MediaBase>().apply {
                pageData = media.toList()
            }
    }

    private fun characterPageWithInfo(
        page: Int,
        hasNextPage: Boolean,
        vararg media: MediaBase,
    ): ConnectionContainer<PageContainer<MediaBase>> = ConnectionContainer<PageContainer<MediaBase>>().apply {
        connection =
            PageContainer<MediaBase>().apply {
                pageData = media.toList()
                this.pageInfo = PageInfo(total = 0, perPage = 0, currentPage = page, hasNextPageValue = hasNextPage)
            }
    }

    private fun staffPage(vararg media: MediaBase): ConnectionContainer<PageContainer<MediaBase>> = characterPage(*media)

    private suspend fun stubCharacterPage(
        id: Long,
        page: Int,
        vararg media: MediaBase,
    ) {
        doReturn(Result.success(characterPage(*media)))
            .`when`(characterRepository)
            .getCharacterMedia(id = id, page = page, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    private suspend fun stubCharacterPageWithInfo(
        id: Long,
        page: Int,
        hasNextPage: Boolean,
        vararg media: MediaBase,
    ) {
        doReturn(Result.success(characterPageWithInfo(page = page, hasNextPage = hasNextPage, *media)))
            .`when`(characterRepository)
            .getCharacterMedia(id = id, page = page, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    private suspend fun stubCharacterEmpty(
        id: Long,
        page: Int,
    ) {
        doReturn(Result.success(characterPage()))
            .`when`(characterRepository)
            .getCharacterMedia(id = id, page = page, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    private suspend fun stubCharacterFailure(
        id: Long,
        page: Int,
        message: String,
    ) {
        doReturn(Result.failure<ConnectionContainer<PageContainer<MediaBase>>>(RuntimeException(message)))
            .`when`(characterRepository)
            .getCharacterMedia(id = id, page = page, perPage = KeyUtil.PAGING_LIMIT, type = null)
    }

    private fun mediaIds(items: List<RecyclerItem>): List<Long> = items.filterIsInstance<MediaBase>().map { it.id }

    private fun assertMediaIdsOnce(
        items: List<RecyclerItem>,
        vararg expectedIds: Long,
    ) {
        val ids = mediaIds(items)
        assertEquals(expectedIds.size, ids.size)
        assertEquals(expectedIds.sorted().toList(), ids.sorted())
    }

    private companion object {
        const val OWNER_ID = 1L
        const val OTHER_ID = 2L
        const val LATCH_TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Fake [CharacterService] serving per-page fixtures through a real [CharacterRepository].
 * A single page can be held mid-response: its Call signals entry on a latch, then
 * blocks on a release latch, so its reply is genuinely in flight while later pages
 * are requested. Requests are recorded in arrival order.
 */
private class GatedCharacterService : CharacterService {

    private val fixtures = mutableMapOf<Int, AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>()
    private var heldPage: Int? = null
    private var entry: CountDownLatch = CountDownLatch(0)
    private var release: CountDownLatch = CountDownLatch(0)

    val requestedPages = CopyOnWriteArrayList<Int>()

    fun stubPage(
        page: Int,
        container: ConnectionContainer<PageContainer<MediaBase>>,
    ) {
        fixtures[page] = AniListContainer(data = DataContainer(result = container), errors = null)
    }

    fun holdPage(page: Int) {
        heldPage = page
        entry = CountDownLatch(1)
        release = CountDownLatch(1)
    }

    /** Blocks up to [timeoutMillis] for the held page's response to be entered; false if it never was. */
    fun awaitEntry(timeoutMillis: Long): Boolean = entry.await(timeoutMillis, TimeUnit.MILLISECONDS)

    fun releasePage() {
        release.countDown()
    }

    override fun getCharacterMedia(
        request: GraphQLRequest<CharacterMediaVariables>,
    ): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>> {
        val page = request.variables?.page ?: 0
        requestedPages.add(page)
        return object : Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>> {
            override fun execute(): Response<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>> {
                if (heldPage == page) {
                    entry.countDown()
                    if (!release.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                        error("page $page response was never released")
                    }
                }
                val fixture = fixtures[page] ?: error("no fixture for page $page")
                return Response.success(fixture)
            }

            override fun enqueue(callback: Callback<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>>) = Unit

            override fun isExecuted(): Boolean = false

            override fun cancel() = Unit

            override fun isCanceled(): Boolean = false

            override fun clone(): Call<AniListContainer<ConnectionContainer<PageContainer<MediaBase>>>> = this

            override fun request(): Request = Request.Builder().url("https://anilist.co/").build()

            override fun timeout(): Timeout = Timeout.NONE
        }
    }

    override fun getCharacterBase(request: GraphQLRequest<CharacterBaseVariables>): Call<GraphContainer<CharacterBaseData>> = error("unused")

    override fun getCharacterOverview(request: GraphQLRequest<CharacterOverviewVariables>): Call<GraphContainer<CharacterOverviewData>> = error("unused")

    override fun getCharacterActors(request: GraphQLRequest<CharacterActorsVariables>): Call<AniListContainer<ConnectionContainer<EdgeContainer<MediaEdge>>>> = error("unused")

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
