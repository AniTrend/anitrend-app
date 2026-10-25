package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.user.InMemoryUserStore
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.graphql.generated.CharacterSort
import com.mxt.anitrend.graphql.generated.StaffSort
import com.mxt.anitrend.graphql.generated.StudioSort
import com.mxt.anitrend.graphql.generated.UserSort
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var searchRepository: SearchRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = mock(SearchRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `character pages accumulate unique ids and replay the same page idempotently`() = runTest {
        val first = CharacterBase().apply { id = 1L }
        val duplicateFirst = CharacterBase().apply { id = 1L }
        val second = CharacterBase().apply { id = 2L }
        val firstPage = PageContainer<CharacterBase>().apply {
            pageData = listOf(first, duplicateFirst)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val secondPage = PageContainer<CharacterBase>().apply {
            pageData = listOf(duplicateFirst, second)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val refreshedFirst = CharacterBase().apply { id = 1L }
        val refreshedSecond = CharacterBase().apply { id = 2L }
        val replayedPageTwo = PageContainer<CharacterBase>().apply {
            pageData = listOf(refreshedFirst, refreshedSecond)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(firstPage))
            .`when`(searchRepository)
            .searchCharacter(
                search = "spike",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(CharacterSort.SEARCH_MATCH),
            )
        doReturn(Result.success(secondPage), Result.success(replayedPageTwo))
            .`when`(searchRepository)
            .searchCharacter(
                search = "spike",
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(CharacterSort.SEARCH_MATCH),
            )
        val viewModel = CharacterSearchViewModel(searchRepository = searchRepository)

        viewModel.load(search = "spike", page = 1)
        viewModel.load(search = "spike", page = 2)
        val firstPageTwoState = viewModel.state.value as CharacterSearchViewModel.UiState.Success
        viewModel.load(search = "spike", page = 2)

        val state = viewModel.state.value as CharacterSearchViewModel.UiState.Success
        assertNotSame(firstPageTwoState, state)
        assertSame(refreshedFirst, state.items.first())
        assertEquals("spike", state.search)
        assertEquals(2, state.page)
        assertEquals(1, state.itemsInPage)
        assertEquals(listOf(1L, 2L), state.items.map { it.id })

        val duplicateOnlyPage = PageContainer<CharacterBase>().apply {
            pageData = listOf(first)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(duplicateOnlyPage))
            .`when`(searchRepository)
            .searchCharacter(
                search = "spike",
                page = 3,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(CharacterSort.SEARCH_MATCH),
            )
        viewModel.load(search = "spike", page = 3)
        val duplicateOnly = viewModel.state.value as CharacterSearchViewModel.UiState.Success
        assertEquals(0, duplicateOnly.itemsInPage)
        assertEquals(listOf(1L, 2L), duplicateOnly.items.map { it.id })

        val freshPage = PageContainer<CharacterBase>().apply {
            pageData = listOf(CharacterBase().apply { id = 3L })
            pageInfo = PageInfo().apply { setHasNextPage(false) }
        }
        doReturn(Result.success(freshPage))
            .`when`(searchRepository)
            .searchCharacter(
                search = "fresh",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(CharacterSort.SEARCH_MATCH),
            )
        viewModel.load(search = "fresh", page = 1)
        val refreshed = viewModel.state.value as CharacterSearchViewModel.UiState.Success
        assertEquals("fresh", refreshed.search)
        assertEquals(1, refreshed.page)
        assertEquals(listOf(3L), refreshed.items.map { it.id })
    }

    @Test
    fun `staff pages accumulate unique ids and replay the same page idempotently`() = runTest {
        val first = StaffBase().apply { id = 1L }
        val second = StaffBase().apply { id = 2L }
        val firstPage = PageContainer<StaffBase>().apply {
            pageData = listOf(first)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val secondPage = PageContainer<StaffBase>().apply {
            pageData = listOf(first, second)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val refreshedFirst = StaffBase().apply { id = 1L }
        val refreshedSecond = StaffBase().apply { id = 2L }
        val replayedPageTwo = PageContainer<StaffBase>().apply {
            pageData = listOf(refreshedFirst, refreshedSecond)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(firstPage))
            .`when`(searchRepository)
            .searchStaff(
                search = "yuki",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StaffSort.SEARCH_MATCH),
            )
        doReturn(Result.success(secondPage), Result.success(replayedPageTwo))
            .`when`(searchRepository)
            .searchStaff(
                search = "yuki",
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StaffSort.SEARCH_MATCH),
            )
        val viewModel = StaffSearchViewModel(searchRepository = searchRepository)

        viewModel.load(search = "yuki", page = 1)
        viewModel.load(search = "yuki", page = 2)
        val firstPageTwoState = viewModel.state.value as StaffSearchViewModel.UiState.Success
        viewModel.load(search = "yuki", page = 2)

        val state = viewModel.state.value as StaffSearchViewModel.UiState.Success
        assertNotSame(firstPageTwoState, state)
        assertSame(refreshedFirst, state.items.first())
        assertEquals("yuki", state.search)
        assertEquals(2, state.page)
        assertEquals(1, state.itemsInPage)
        assertEquals(listOf(1L, 2L), state.items.map { it.id })
        val duplicateOnlyPage = PageContainer<StaffBase>().apply {
            pageData = listOf(first)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(duplicateOnlyPage))
            .`when`(searchRepository)
            .searchStaff(
                search = "yuki",
                page = 3,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StaffSort.SEARCH_MATCH),
            )
        viewModel.load(search = "yuki", page = 3)
        val duplicateOnly = viewModel.state.value as StaffSearchViewModel.UiState.Success
        assertEquals(0, duplicateOnly.itemsInPage)
        assertEquals(listOf(1L, 2L), duplicateOnly.items.map { it.id })

        val freshPage = PageContainer<StaffBase>().apply {
            pageData = listOf(StaffBase().apply { id = 3L })
            pageInfo = PageInfo().apply { setHasNextPage(false) }
        }
        doReturn(Result.success(freshPage))
            .`when`(searchRepository)
            .searchStaff(
                search = "fresh",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StaffSort.SEARCH_MATCH),
            )
        viewModel.load(search = "fresh", page = 1)
        val refreshed = viewModel.state.value as StaffSearchViewModel.UiState.Success
        assertEquals("fresh", refreshed.search)
        assertEquals(1, refreshed.page)
        assertEquals(listOf(3L), refreshed.items.map { it.id })
    }

    @Test
    fun `studio pages accumulate unique ids and replay the same page idempotently`() = runTest {
        val first = StudioBase().apply { id = 1L }
        val second = StudioBase().apply { id = 2L }
        val firstPage = PageContainer<StudioBase>().apply {
            pageData = listOf(first)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val secondPage = PageContainer<StudioBase>().apply {
            pageData = listOf(first, second)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        val refreshedFirst = StudioBase().apply { id = 1L }
        val refreshedSecond = StudioBase().apply { id = 2L }
        val replayedPageTwo = PageContainer<StudioBase>().apply {
            pageData = listOf(refreshedFirst, refreshedSecond)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(firstPage))
            .`when`(searchRepository)
            .searchStudio(
                search = "bones",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StudioSort.SEARCH_MATCH),
            )
        doReturn(Result.success(secondPage), Result.success(replayedPageTwo))
            .`when`(searchRepository)
            .searchStudio(
                search = "bones",
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StudioSort.SEARCH_MATCH),
            )
        val viewModel = StudioSearchViewModel(searchRepository = searchRepository)

        viewModel.load(search = "bones", page = 1)
        viewModel.load(search = "bones", page = 2)
        val firstPageTwoState = viewModel.state.value as StudioSearchViewModel.UiState.Success
        viewModel.load(search = "bones", page = 2)

        val state = viewModel.state.value as StudioSearchViewModel.UiState.Success
        assertNotSame(firstPageTwoState, state)
        assertSame(refreshedFirst, state.items.first())
        assertEquals("bones", state.search)
        assertEquals(2, state.page)
        assertEquals(1, state.itemsInPage)
        assertEquals(listOf(1L, 2L), state.items.map { it.id })

        val duplicateOnlyPage = PageContainer<StudioBase>().apply {
            pageData = listOf(first)
            pageInfo = PageInfo().apply { setHasNextPage(true) }
        }
        doReturn(Result.success(duplicateOnlyPage))
            .`when`(searchRepository)
            .searchStudio(
                search = "bones",
                page = 3,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StudioSort.SEARCH_MATCH),
            )
        viewModel.load(search = "bones", page = 3)
        val duplicateOnly = viewModel.state.value as StudioSearchViewModel.UiState.Success
        assertEquals(0, duplicateOnly.itemsInPage)
        assertEquals(listOf(1L, 2L), duplicateOnly.items.map { it.id })

        val freshPage = PageContainer<StudioBase>().apply {
            pageData = listOf(StudioBase().apply { id = 3L })
            pageInfo = PageInfo().apply { setHasNextPage(false) }
        }
        doReturn(Result.success(freshPage))
            .`when`(searchRepository)
            .searchStudio(
                search = "fresh",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StudioSort.SEARCH_MATCH),
            )
        viewModel.load(search = "fresh", page = 1)
        val refreshed = viewModel.state.value as StudioSearchViewModel.UiState.Success
        assertEquals("fresh", refreshed.search)
        assertEquals(1, refreshed.page)
        assertEquals(listOf(3L), refreshed.items.map { it.id })
    }

    @Test
    fun `user pages retain earlier results and replace overlapping ids on replay`() = runTest {
        val first = UserBase().apply {
            id = 1L
            name = "First"
        }
        val overlap = UserBase().apply {
            id = 2L
            name = "Original"
        }
        val updated = UserBase().apply {
            id = 2L
            name = "Updated"
        }
        val last = UserBase().apply {
            id = 3L
            name = "Last"
        }
        doReturn(
            Result.success(
                PageContainer<UserBase>().apply {
                    pageData = listOf(first, overlap)
                    pageInfo = PageInfo().apply { setHasNextPage(true) }
                },
            ),
        ).`when`(searchRepository).searchUser(
            search = "max",
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = listOf(UserSort.SEARCH_MATCH),
        )
        doReturn(
            Result.success(
                PageContainer<UserBase>().apply {
                    pageData = listOf(updated, last)
                    pageInfo = PageInfo().apply { setHasNextPage(false) }
                },
            ),
        ).`when`(searchRepository).searchUser(
            search = "max",
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = listOf(UserSort.SEARCH_MATCH),
        )
        val viewModel = UserSearchViewModel(
            searchRepository = searchRepository,
            toggleUserFollowInteractor = mock(ToggleUserFollowInteractor::class.java),
            userStore = InMemoryUserStore(),
        )

        viewModel.load(search = "max", page = 1)
        viewModel.load(search = "max", page = 2)
        viewModel.load(search = "max", page = 2)

        val state = viewModel.state.value as UserSearchViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L), state.content.pageData.map { it.id })
        assertEquals(listOf("First", "Updated", "Last"), state.content.pageData.map { it.name })
    }
}
