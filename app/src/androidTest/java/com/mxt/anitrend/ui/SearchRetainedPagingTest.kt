@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.Manifest
import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import com.mxt.anitrend.ui.navigation.MAIN_NAV_DRAWER_LIST_TAG
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.search.SearchFragment
import com.mxt.anitrend.viewmodel.CharacterSearchViewModel
import com.mxt.anitrend.viewmodel.UserSearchViewModel
import io.mockk.coEvery
import io.mockk.mockk
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchRetainedPagingTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val resultsScroller =
        hasScrollToIndexAction() and !hasTestTag(MAIN_NAV_DRAWER_LIST_TAG)

    @Before
    fun grantFirstRunPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation
            .executeShellCommand("pm grant com.mxt.anitrend android.permission.POST_NOTIFICATIONS")
            .use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        assertTrue(
            ApplicationProvider.getApplicationContext<android.content.Context>()
                .checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED,
        )
    }

    @Test
    fun pageTwoSurvivesDetailBackAndSearchGridsRespectNativeRefreshBoundary() {
        val koin = GlobalContext.get()
        val originalCharacterRepository = koin.get<CharacterRepository>()
        val originalRepository = koin.get<SearchRepository>()
        val characterPages = CopyOnWriteArrayList<Int>()
        val staffPages = CopyOnWriteArrayList<Int>()
        val studioPages = CopyOnWriteArrayList<Int>()
        val repository = mockk<SearchRepository>(relaxed = true)
        val characterRepository = mockk<CharacterRepository>(relaxed = true)
        coEvery { characterRepository.getCharacterOverview(any()) } returns
            Result.failure(IllegalStateException("Character detail data is not needed for this route test"))
        coEvery {
            repository.searchMedia(
                id = any(),
                page = any(),
                perPage = any(),
                search = any(),
                type = any<MediaType>(),
                format = any(),
                startDate = any(),
                endDate = any(),
                season = any(),
                genres = any(),
                genresExclude = any(),
                isAdult = any(),
                sort = any(),
            )
        } returns Result.success(page<MediaBase>(emptyList(), hasNextPage = false))
        coEvery { repository.searchCharacter(any(), any(), any(), any(), any()) } coAnswers {
            val pageNumber = secondArg<Int?>() ?: 1
            characterPages += pageNumber
            Result.success(characterPage(pageNumber))
        }
        coEvery { repository.searchStaff(any(), any(), any(), any(), any()) } coAnswers {
            val pageNumber = secondArg<Int?>() ?: 1
            staffPages += pageNumber
            Result.success(staffPage(pageNumber))
        }
        coEvery { repository.searchStudio(any(), any(), any(), any(), any()) } coAnswers {
            val pageNumber = secondArg<Int?>() ?: 1
            studioPages += pageNumber
            Result.success(studioPage(pageNumber))
        }
        koin.declare(repository)

        koin.declare(characterRepository)
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "paging")
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                runCatching {
                    Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("What's New!"))
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
                    Espresso.pressBackUnconditionally()
                }
                clickSectionChip(scenario, "Characters")
                composeRule.waitUntil("first character page", 20_000L) {
                    characterPages.contains(1) && rowCount("character-row-card") > 0
                }
                assertMidListTouchScroll(scenario)
                requestCharacterPageTwo(characterPages)

                composeRule.waitUntil("complete unique page two snapshot", 20_000L) {
                    characterIds(scenario).size == 35
                }
                val accumulatedCharacterIds = characterIds(scenario)
                assertEquals((1L..35L).toList(), accumulatedCharacterIds)
                assertEquals(1, characterPages.count { it == 2 })
                scenario.recreate()
                composeRule.waitUntil("page two restored after activity recreation", 20_000L) {
                    currentDestination(scenario) == R.id.searchFragment && characterIds(scenario).size == 35
                }
                assertEquals("activity recreation must not reload page one", 1, characterPages.count { it == 1 })
                assertEquals("activity recreation must not replay page two", 1, characterPages.count { it == 2 })

                composeRule.onAllNodesWithTag("character-row-card", useUnmergedTree = true)[0]
                    .performClick()
                composeRule.waitUntil("character detail destination", 20_000L) {
                    currentDestination(scenario) == R.id.characterFragment
                }
                Espresso.pressBack()
                composeRule.waitUntil("search restored after detail", 20_000L) {
                    currentDestination(scenario) == R.id.searchFragment &&
                        rowCount("character-row-card") > 0 &&
                        characterIds(scenario).size == 35
                }
                assertEquals("Back must reuse the cached page instead of requesting page two again", 1, characterPages.count { it == 2 })
                requestCharacterPageThree(characterPages)
                composeRule.waitUntil("duplicate-only search page", 20_000L) {
                    characterSuccess(scenario)?.let { it.page == 3 && it.itemsInPage == 0 } == true
                }
                repeat(3) {
                    composeRule.onNode(resultsScroller).performScrollToIndex(0)
                    composeRule.onNode(resultsScroller).performScrollToIndex(34)
                    composeRule.waitForIdle()
                }
                assertEquals("a duplicate-only page must block page four", 0, characterPages.count { it == 4 })

                clickSectionChip(scenario, "Staff")
                composeRule.waitUntil("first staff page", 20_000L) {
                    staffPages.contains(1) && rowCount("staff-row-card") > 0
                }
                assertMidListTouchScroll(scenario)

                clickSectionChip(scenario, "Studio")
                composeRule.waitUntil("first studio page", 20_000L) {
                    studioPages.contains(1) && textCount("Studio 201") > 0
                }
                assertMidListTouchScroll(scenario)
            }
        } finally {
            koin.declare(originalRepository)
            koin.declare(originalCharacterRepository)
        }
    }

    @Test
    fun draftEditsPreservePendingResponseAndTabSwitchUsesDraft() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<SearchRepository>()
        val requestedQueries = CopyOnWriteArrayList<String>()
        val requestedStaffQueries = CopyOnWriteArrayList<String>()
        val pendingResponse = CompletableDeferred<Result<PageContainer<CharacterBase>>>()
        val repository = mockk<SearchRepository>(relaxed = true)
        coEvery {
            repository.searchMedia(
                id = any(),
                page = any(),
                perPage = any(),
                search = any(),
                type = any<MediaType>(),
                format = any(),
                startDate = any(),
                endDate = any(),
                season = any(),
                genres = any(),
                genresExclude = any(),
                isAdult = any(),
                sort = any(),
            )
        } returns Result.success(page<MediaBase>(emptyList(), hasNextPage = false))
        coEvery { repository.searchCharacter(any(), any(), any(), any(), any()) } coAnswers {
            requestedQueries += (invocation.args[3] as? String).orEmpty()
            pendingResponse.await()
        }
        coEvery { repository.searchStaff(any(), any(), any(), any(), any()) } coAnswers {
            requestedStaffQueries += (invocation.args[3] as? String).orEmpty()
            Result.success(staffPage(secondArg<Int?>() ?: 1))
        }
        koin.declare(repository)
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "submitted")
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                runCatching {
                    Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("What's New!"))
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
                    Espresso.pressBackUnconditionally()
                }
                clickSectionChip(scenario, "Characters")
                composeRule.waitUntil("submitted character request is pending", 20_000L) {
                    requestedQueries == listOf("submitted")
                }
                Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.search_query_input))
                    .perform(
                        androidx.test.espresso.action.ViewActions.replaceText("draft"),
                        androidx.test.espresso.action.ViewActions.closeSoftKeyboard(),
                    )
                scenario.onActivity { activity ->
                    activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout).setRefreshing(true)
                }
                pendingResponse.complete(Result.success(characterPage(1)))

                composeRule.waitUntil("submitted response renders after draft edit", 20_000L) {
                    characterSuccess(scenario)?.search == "submitted" && rowCount("character-row-card") > 0
                }
                assertEquals(listOf("submitted"), requestedQueries.toList())
                scenario.onActivity { activity ->
                    val refreshLayout = activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout)
                    assertFalse("successful page stops native refresh", refreshLayout.isRefreshing())
                    assertFalse("successful page stops native loading", refreshLayout.isLoading())
                }
                clickSectionChip(scenario, "Staff")
                composeRule.waitUntil("tab change submits the current draft", 20_000L) {
                    requestedStaffQueries == listOf("draft") && rowCount("staff-row-card") > 0
                }
            }
        } finally {
            koin.declare(originalRepository)
        }
    }

    @Test
    fun failedSearchAppendRetriesTheSamePageAndStopsNativeLoading() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<SearchRepository>()
        val requestedPages = CopyOnWriteArrayList<Int>()
        val retryResponse = CompletableDeferred<Result<PageContainer<CharacterBase>>>()
        val repository = mockk<SearchRepository>(relaxed = true)
        coEvery {
            repository.searchMedia(
                id = any(),
                page = any(),
                perPage = any(),
                search = any(),
                type = any<MediaType>(),
                format = any(),
                startDate = any(),
                endDate = any(),
                season = any(),
                genres = any(),
                genresExclude = any(),
                isAdult = any(),
                sort = any(),
            )
        } returns Result.success(page<MediaBase>(emptyList(), hasNextPage = false))
        coEvery { repository.searchCharacter(any(), any(), any(), any(), any()) } coAnswers {
            val pageNumber = secondArg<Int?>() ?: 1
            requestedPages += pageNumber
            when {
                pageNumber == 1 -> Result.success(characterPage(pageNumber))
                requestedPages.count { it == pageNumber } == 1 ->
                    Result.failure(IllegalStateException("First append fails"))
                else -> retryResponse.await()
            }
        }
        koin.declare(repository)
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "retry")
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                runCatching {
                    Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("What's New!"))
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
                    Espresso.pressBackUnconditionally()
                }
                clickSectionChip(scenario, "Characters")
                composeRule.waitUntil("first character page", 20_000L) {
                    requestedPages.contains(1) && rowCount("character-row-card") > 0
                }
                requestCharacterPageTwo(requestedPages)
                composeRule.waitUntil("native append retry is fully visible", 20_000L) {
                    var ready = false
                    scenario.onActivity { activity ->
                        val action = activity.findViewById<android.view.View>(com.google.android.material.R.id.snackbar_action)
                        ready = action != null &&
                            androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90).matches(action)
                    }
                    ready
                }
                Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(com.google.android.material.R.id.snackbar_action))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
                    .perform(androidx.test.espresso.action.ViewActions.click())
                composeRule.waitUntil("same page retry is pending", 20_000L) {
                    requestedPages.count { it == 2 } == 2
                }
                scenario.onActivity { activity ->
                    assertTrue(
                        "native retry indicator remains active until the response",
                        activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout).isLoading(),
                    )
                }
                retryResponse.complete(
                    Result.success(
                        characterPage(2).apply {
                            pageInfo.setHasNextPage(false)
                        },
                    ),
                )
                composeRule.waitUntil("retried append renders", 20_000L) {
                    characterSuccess(scenario)?.let { it.page == 2 && it.items.size == 35 } == true
                }
                assertEquals(listOf(1, 2, 2), requestedPages.toList())
                scenario.onActivity { activity ->
                    val refreshLayout = activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout)
                    assertFalse("successful retry stops native loading", refreshLayout.isLoading())
                    assertFalse("successful retry stops native refresh", refreshLayout.isRefreshing())
                }
            }
        } finally {
            koin.declare(originalRepository)
        }
    }

    @Test
    fun userPagesRemainUniqueAfterSearchViewRecreation() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<SearchRepository>()
        val settings = koin.get<Settings>()
        val wasAuthenticated = settings.isAuthenticated
        val requestedPages = CopyOnWriteArrayList<Int>()
        val repository = mockk<SearchRepository>(relaxed = true)
        coEvery {
            repository.searchMedia(
                id = any(),
                page = any(),
                perPage = any(),
                search = any(),
                type = any<MediaType>(),
                format = any(),
                startDate = any(),
                endDate = any(),
                season = any(),
                genres = any(),
                genresExclude = any(),
                isAdult = any(),
                sort = any(),
            )
        } returns Result.success(page<MediaBase>(emptyList(), hasNextPage = false))
        coEvery {
            repository.searchUser(any(), any(), any(), any(), any())
        } coAnswers {
            val pageNumber = secondArg<Int?>() ?: 1
            requestedPages += pageNumber
            val ids = if (pageNumber == 1) 1L..30L else 30L..35L
            Result.success(
                page(
                    ids.map { id ->
                        UserBase().apply {
                            this.id = id
                            name = "User $id"
                        }
                    },
                    hasNextPage = pageNumber == 1,
                ),
            )
        }
        koin.declare(repository)
        settings.isAuthenticated = true
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "users")
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                runCatching {
                    Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("What's New!"))
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()))
                    Espresso.pressBackUnconditionally()
                }
                clickSectionChip(scenario, "Users")
                composeRule.waitUntil("first user page", 20_000L) {
                    textCount("User 1") > 0
                }
                composeRule.onNode(resultsScroller).performScrollToIndex(29)
                composeRule.waitUntil("user page two", 20_000L) {
                    var loaded = false
                    scenario.onActivity { activity ->
                        val fragment = requireNotNull(findSearchFragment(activity.supportFragmentManager.fragments))
                        loaded = (
                            ViewModelProvider(fragment)[UserSearchViewModel::class.java]
                                .state.value as? UserSearchViewModel.UiState.Success
                            )
                            ?.content?.pageData?.size == 35
                    }
                    requestedPages.contains(2) && loaded
                }
                composeRule.onNode(resultsScroller).performScrollToNode(hasText("User 35"))
                scenario.onActivity { activity ->
                    val fragment = requireNotNull(findSearchFragment(activity.supportFragmentManager.fragments))
                    val state = ViewModelProvider(fragment)[UserSearchViewModel::class.java]
                        .state.value as UserSearchViewModel.UiState.Success
                    assertEquals((1L..35L).toList(), state.content.pageData.map { it.id })
                    fragment.parentFragmentManager.beginTransaction().detach(fragment).commitNow()
                    fragment.parentFragmentManager.beginTransaction().attach(fragment).commitNow()
                }
                composeRule.waitForIdle()
                composeRule.onNode(resultsScroller).performScrollToNode(hasText("User 1"))
                assertEquals("restoring the search must not reload the last page", listOf(1, 2), requestedPages.toList())
                assertEquals(1, textCount("User 1"))
            }
        } finally {
            settings.isAuthenticated = wasAuthenticated
            koin.declare(originalRepository)
        }
    }

    private fun characterSuccess(
        scenario: ActivityScenario<MainActivity>,
    ): CharacterSearchViewModel.UiState.Success? {
        var state: CharacterSearchViewModel.UiState.Success? = null
        scenario.onActivity { activity ->
            val search = requireNotNull(findSearchFragment(activity.supportFragmentManager.fragments))
            state = ViewModelProvider(search)[CharacterSearchViewModel::class.java]
                .state.value as? CharacterSearchViewModel.UiState.Success
        }
        return state
    }

    private fun characterIds(scenario: ActivityScenario<MainActivity>): List<Long> = characterSuccess(scenario)?.items?.map { it.id }.orEmpty()

    private fun requestCharacterPageTwo(requestedPages: List<Int>) {
        composeRule.onNode(resultsScroller).performScrollToIndex(29)
        composeRule.waitUntil("character page two response", 20_000L) {
            requestedPages.count { it == 2 } == 1 && rowCount("character-row-card") > 0
        }
    }

    private fun requestCharacterPageThree(requestedPages: List<Int>) {
        composeRule.onNode(resultsScroller).performScrollToIndex(34)
        composeRule.waitUntil("character page three response", 20_000L) {
            requestedPages.count { it == 3 } == 1
        }
    }

    private fun assertMidListTouchScroll(scenario: ActivityScenario<MainActivity>) {
        composeRule.onNode(resultsScroller).performScrollToIndex(8)
        composeRule.waitForIdle()
        val beforeDrag = gridScrollOffsets()
        assertTrue("grid must be scrolled before a native downward drag", beforeDrag.any { it > 0f })

        Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.refreshLayout))
            .perform(
                GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.translate(GeneralLocation.TOP_CENTER, 0f, 0.35f),
                    GeneralLocation.translate(GeneralLocation.BOTTOM_CENTER, 0f, -0.35f),
                    Press.FINGER,
                ),
            )
        composeRule.waitForIdle()
        val afterDrag = gridScrollOffsets()
        assertTrue(
            "mid-list downward touch must scroll the Compose grid through its native refresh parent",
            beforeDrag.size == afterDrag.size &&
                beforeDrag.zip(afterDrag).any { (before, after) -> after < before },
        )
        scenario.onActivity { activity ->
            assertFalse(
                "a mid-list touch must not start pull-to-refresh",
                activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout).isRefreshing(),
            )
        }
    }

    private fun gridScrollOffsets(): List<Float> = composeRule.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and
            !hasTestTag(MAIN_NAV_DRAWER_LIST_TAG),
        useUnmergedTree = true,
    ).fetchSemanticsNodes().map { node ->
        node.config[SemanticsProperties.VerticalScrollAxisRange].value()
    }

    private fun rowCount(tag: String): Int = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
        .fetchSemanticsNodes().size

    private fun textCount(text: String): Int = composeRule.onAllNodesWithText(text, useUnmergedTree = true)
        .fetchSemanticsNodes().size

    private fun currentDestination(scenario: ActivityScenario<MainActivity>): Int? {
        var destination: Int? = null
        scenario.onActivity { activity ->
            destination = (activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment)
                .navController.currentDestination?.id
        }
        return destination
    }

    private fun clickSectionChip(scenario: ActivityScenario<MainActivity>, text: String) {
        scenario.onActivity { activity ->
            val group = activity.findViewById<ChipGroup>(R.id.search_section_selector)
            val chip = (0 until group.childCount)
                .mapNotNull { group.getChildAt(it) as? Chip }
                .first { it.text.contains(text, ignoreCase = true) }
            chip.performClick()
        }
    }

    private fun findSearchFragment(fragments: List<androidx.fragment.app.Fragment>): SearchFragment? {
        for (fragment in fragments) {
            if (fragment is SearchFragment) return fragment
            findSearchFragment(fragment.childFragmentManager.fragments)?.let { return it }
        }
        return null
    }

    private fun characterPage(pageNumber: Int): PageContainer<CharacterBase> {
        val ids = when (pageNumber) {
            1 -> 1L..30L
            2 -> 30L..35L
            else -> 30L..30L
        }
        return page(
            ids.map { id ->
                CharacterBase().apply {
                    this.id = id
                    name = TitleBase("Character $id", null, null, null)
                }
            },
            hasNextPage = pageNumber < 4,
        )
    }

    private fun staffPage(pageNumber: Int): PageContainer<StaffBase> {
        val ids = if (pageNumber == 1) 101L..130L else 130L..135L
        return page(
            ids.map { id ->
                StaffBase().apply {
                    this.id = id
                    name = TitleBase("Staff $id", null, null, null)
                }
            },
            hasNextPage = pageNumber == 1,
        )
    }

    private fun studioPage(pageNumber: Int): PageContainer<StudioBase> {
        val ids = if (pageNumber == 1) 201L..230L else 230L..235L
        return page(
            ids.map { id ->
                StudioBase().apply {
                    this.id = id
                    name = "Studio $id"
                }
            },
            hasNextPage = pageNumber == 1,
        )
    }

    private fun <T> page(items: List<T>, hasNextPage: Boolean): PageContainer<T> = PageContainer<T>().apply {
        pageData = items
        pageInfo = PageInfo().apply { setHasNextPage(hasNextPage) }
    }
}
