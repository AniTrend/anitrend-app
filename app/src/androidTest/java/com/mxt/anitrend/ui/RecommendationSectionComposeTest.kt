package com.mxt.anitrend.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.domain.model.RecommendationRecord
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.view.fragment.detail.MediaRecommendationsSection
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import com.mxt.anitrend.widget.ProgressLayout
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecommendationSectionComposeTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun recommendationsForTheSameMediaKeepDistinctKeysAndActions() {
        val repository = mockk<MediaRepository>()
        coEvery { repository.getMediaRecommendations(any(), any(), any(), any(), any(), any()) } returns Result.success(
            RecommendationPageResult(listOf(recommendation(1L, "First recommendation"), recommendation(2L, "Second recommendation")), null),
        )
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            var clicked: Long? = null
            var longPressed: Long? = null
            lateinit var section: MediaRecommendationsSection
            scenario.onActivity { host ->
                val viewModel = MediaRecommendationsViewModel(repository)
                host.viewModelStore.put("recommendations", viewModel)
                section = MediaRecommendationsSection(
                    context = host,
                    viewModel = viewModel,
                    mediaId = 1L,
                    mediaType = "ANIME",
                    isAdultContent = false,
                    onOpenMedia = { _, item -> clicked = item.id },
                    onLongPressMedia = { _, item ->
                        longPressed = item.id
                        true
                    },
                )
                host.setContentView(section.createView(host.layoutInflater, null))
                section.start(host)
                section.select()
            }
            compose.waitUntil(10_000L) {
                var content = false
                scenario.onActivity { content = it.findViewById<ProgressLayout>(R.id.stateLayout).isContent }
                content
            }
            compose.onNodeWithText("First recommendation").assertIsDisplayed().performClick()
            compose.onNodeWithText("Second recommendation").assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.OnLongClick) { action -> action() }
            assertEquals(1L, clicked)
            assertEquals(2L, longPressed)
            scenario.onActivity { host ->
                section.select()
                assertTrue(host.findViewById<ProgressLayout>(R.id.stateLayout).isContent)
            }
        }
    }

    private fun recommendation(id: Long, title: String) = RecommendationRecord(
        id = id,
        mediaRecommendation = MediaSummaryRecord(
            id = 42L,
            titleUserPreferred = title,
            titleRomaji = title,
            titleEnglish = null,
            titleOriginal = null,
            coverImage = null,
            type = "ANIME",
            format = "TV",
            episodes = 12,
            chapters = 0,
            volumes = 0,
            status = "FINISHED",
            siteUrl = null,
            isFavourite = false,
            startDate = FuzzyDateRecord(year = 2024, month = 1, day = 1),
            nextAiringEpisode = null,
            averageScore = 80,
        ),
        rating = null,
        user = null,
        userRating = null,
    )
}
