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
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

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
    fun `StaffSearchViewModel routes load through SearchRepository`() = runTest {
        val content = PageContainer<StaffBase>()
        doReturn(Result.success(content))
            .`when`(searchRepository)
            .searchStaff(
                search = "yuki",
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StaffSort.SEARCH_MATCH),
            )
        val viewModel = StaffSearchViewModel(searchRepository = searchRepository)

        viewModel.load(search = "yuki", page = 1)

        val state = viewModel.state.value as StaffSearchViewModel.UiState.Success
        assertSame(content, state.content)
        verify(searchRepository).searchStaff(
            search = "yuki",
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = listOf(StaffSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `StudioSearchViewModel routes load through SearchRepository`() = runTest {
        val content = PageContainer<StudioBase>()
        doReturn(Result.success(content))
            .`when`(searchRepository)
            .searchStudio(
                search = "bones",
                page = 3,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(StudioSort.SEARCH_MATCH),
            )
        val viewModel = StudioSearchViewModel(searchRepository = searchRepository)

        viewModel.load(search = "bones", page = 3)

        val state = viewModel.state.value as StudioSearchViewModel.UiState.Success
        assertSame(content, state.content)
        verify(searchRepository).searchStudio(
            search = "bones",
            page = 3,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = listOf(StudioSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `CharacterSearchViewModel routes load through SearchRepository`() = runTest {
        val content = PageContainer<CharacterBase>()
        doReturn(Result.success(content))
            .`when`(searchRepository)
            .searchCharacter(
                search = "spike",
                page = 4,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(CharacterSort.SEARCH_MATCH),
            )
        val viewModel = CharacterSearchViewModel(searchRepository = searchRepository)

        viewModel.load(search = "spike", page = 4)

        val state = viewModel.state.value as CharacterSearchViewModel.UiState.Success
        assertSame(content, state.content)
        verify(searchRepository).searchCharacter(
            search = "spike",
            page = 4,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = listOf(CharacterSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `UserSearchViewModel routes load through SearchRepository`() = runTest {
        val content = PageContainer<UserBase>()
        doReturn(Result.success(content))
            .`when`(searchRepository)
            .searchUser(
                search = "max",
                page = 5,
                perPage = KeyUtil.PAGING_LIMIT,
                sort = listOf(UserSort.SEARCH_MATCH),
            )
        val viewModel = UserSearchViewModel(
            searchRepository = searchRepository,
            toggleUserFollowInteractor = mock(ToggleUserFollowInteractor::class.java),
            userStore = InMemoryUserStore(),
        )

        viewModel.load(search = "max", page = 5)

        val state = viewModel.state.value as UserSearchViewModel.UiState.Success
        assertSame(content, state.content)
        verify(searchRepository).searchUser(
            search = "max",
            page = 5,
            perPage = KeyUtil.PAGING_LIMIT,
            sort = listOf(UserSort.SEARCH_MATCH),
        )
    }
}
