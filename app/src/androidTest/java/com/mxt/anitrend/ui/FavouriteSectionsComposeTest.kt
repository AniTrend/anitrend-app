package com.mxt.anitrend.ui

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.view.fragment.favourite.FavouriteFragment
import com.mxt.anitrend.viewmodel.MediaFavouritesViewModel
import com.mxt.anitrend.widget.ProgressLayout
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertTrue
import org.koin.core.context.GlobalContext
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.util.KeyUtil
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke coverage for FavouriteFragment's Compose section grids. The search
 * screen exercises the same wiring live, but this fragment is sign-in gated and
 * its renderers previously had no instrumentation. Each migrated tab must reach
 * rows (which only the Compose grids can render now that the delegate adapters
 * are gone) or the shared empty state without falling back to the legacy host.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FavouriteSectionsComposeTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            InstrumentationRegistry.getInstrumentation().targetContext,
            authenticated = true,
        )
    }

    @After
    fun tearDown() {
        TestSessionUtil.setAuthenticated(
            InstrumentationRegistry.getInstrumentation().targetContext,
            authenticated = false,
        )
    }

    @Test
    fun favouriteComposeSectionTabsRenderRowsOrEmptyState() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_FAVOURITES)
            .putExtra(KeyUtil.arg_id, 1L)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // Section.values() order: ANIME, CHARACTERS, MANGA, STAFF, STUDIOS.
            val migratedTabs = listOf(
                1 to "character-row-card",
                3 to "staff-row-card",
                4 to "studio-favourite-marker",
            )
            for ((chipIndex, rowTag) in migratedTabs) {
                scenario.onActivity { activity ->
                    val sectionSelector = activity.findViewById<ChipGroup>(R.id.favourite_section_selector)
                    // Chips route through click listeners, so check() alone would not switch tabs.
                    sectionSelector.getChildAt(chipIndex).performClick()
                }
                composeRule.waitForIdle()
                composeRule.waitUntil(20_000L) {
                    val rows = composeRule.onAllNodesWithTag(rowTag, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                    rows.isNotEmpty() || sharedStateVisible()
                }
            }
        }
    }

    @Test
    fun lateAnimeFailureDoesNotReplaceCharacterTab() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<UserRepository>()
        val animeStarted = CompletableDeferred<Unit>()
        val animeResponse = CompletableDeferred<Result<ConnectionContainer<Favourite>>>()
        val repository = mockk<UserRepository>(relaxed = true)
        coEvery { repository.getAnimeFavourites(any(), any(), any(), any()) } coAnswers {
            animeStarted.complete(Unit)
            animeResponse.await()
        }
        val characterPage = PageContainer<CharacterBase>().apply {
            pageData = listOf(
                CharacterBase().apply {
                    id = 991L
                    name = TitleBase("Kept Character", null, null, null)
                },
            )
            pageInfo = PageInfo().apply { setHasNextPage(false) }
        }
        val characters = ConnectionContainer<Favourite>().apply {
            connection = Favourite().apply { this.characters = characterPage }
        }
        coEvery { repository.getCharacterFavourites(any(), any(), any(), any()) } returns Result.success(characters)
        koin.declare(repository)
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_FAVOURITES)
                .putExtra(KeyUtil.arg_id, 1L)
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                composeRule.waitUntil(10_000L) { animeStarted.isCompleted }
                var mediaViewModel: MediaFavouritesViewModel? = null
                scenario.onActivity { activity ->
                    val fragment = requireNotNull(findFavourite(activity.supportFragmentManager.fragments))
                    mediaViewModel = ViewModelProvider(fragment)[MediaFavouritesViewModel::class.java]
                    activity.findViewById<ChipGroup>(R.id.favourite_section_selector)
                        .getChildAt(1).performClick()
                }
                composeRule.waitUntil(10_000L) {
                    composeRule.onAllNodesWithTag("character-row-card", useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty()
                }
                composeRule.onNodeWithText("Kept Character").assertIsDisplayed()
                animeResponse.complete(Result.failure(IllegalStateException("late anime failure")))
                composeRule.waitUntil(10_000L) {
                    mediaViewModel?.state?.value is MediaFavouritesViewModel.UiState.Error
                }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                composeRule.waitForIdle()
                composeRule.onNodeWithText("Kept Character").assertIsDisplayed()
                scenario.onActivity { activity ->
                    assertTrue(
                        "late media failure must leave the selected character surface visible",
                        activity.findViewById<ProgressLayout>(R.id.stateLayout).isContent,
                    )
                }
            }
        } finally {
            animeResponse.cancel()
            koin.declare(originalRepository)
        }
    }

    private fun findFavourite(fragments: List<Fragment>): FavouriteFragment? {
        for (fragment in fragments) {
            if (fragment is FavouriteFragment) return fragment
            findFavourite(fragment.childFragmentManager.fragments)?.let { return it }
        }
        return null
    }

    /**
     * The shared empty/error state is a View-hosted ProgressLayout surface outside
     * Compose semantics, so it settles through Espresso checks.
     */
    private fun sharedStateVisible(): Boolean = try {
        onView(withText(R.string.layout_empty_response)).check(matches(isDisplayed()))
        true
    } catch (emptyMissed: Throwable) {
        try {
            onView(withText(R.string.button_try_again)).check(matches(isDisplayed()))
            true
        } catch (errorMissed: Throwable) {
            false
        }
    }
}
