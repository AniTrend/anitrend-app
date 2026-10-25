@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewTagRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.detail.MediaOverviewSection
import com.mxt.anitrend.viewmodel.MediaOverviewViewModel
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaOverviewTagInteractionTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun tagMoreActionKeepsExistingBrowseCallback() {
        val tag =
            MediaOverviewTagRecord(
                id = 7L,
                name = TAG_NAME,
                description = TAG_DESCRIPTION,
                category = "Theme",
                rank = 80,
                isGeneralSpoiler = false,
                isAdult = false,
            )
        val record =
            MediaOverviewRecord(
                id = MEDIA_ID,
                titleUserPreferred = "No Game No Life",
                titleRomaji = "No Game No Life",
                titleEnglish = "No Game No Life",
                titleOriginal = "No Game No Life",
                bannerImage = null,
                coverImage = null,
                type = MediaType.ANIME.name,
                format = "TV",
                season = "FALL",
                status = "FINISHED",
                meanScore = null,
                averageScore = null,
                startDate = null,
                endDate = null,
                episodes = 12,
                chapters = null,
                volumes = null,
                isAdult = false,
                isFavourite = false,
                nextAiringEpisode = null,
                mediaListEntry = null,
                siteUrl = null,
                updatedAt = null,
                genres = emptyList(),
                tags = listOf(tag),
                trailer = null,
                duration = 24,
                hashtag = null,
                source = "ORIGINAL",
                studios = emptyList(),
                description = "Overview description",
            )
        val repository = mockk<MediaRepository>()
        coEvery {
            repository.getMediaOverviewRecord(MEDIA_ID, MediaType.ANIME, false)
        } returns Result.success(record)
        val settings = mockk<Settings>()
        every { settings.displayAdultContent } returns false
        val viewModel =
            MediaOverviewViewModel(
                repository = repository,
                settings = settings,
                ioDispatcher = Dispatchers.Main.immediate,
            )
        val openedTag = AtomicReference<String?>()
        val moreLabel = ApplicationProvider.getApplicationContext<Context>().getString(R.string.More)
        val section =
            MediaOverviewSection(
                viewModel = viewModel,
                mediaId = MEDIA_ID,
                mediaType = MediaType.ANIME.name,
                onOpenGenre = {},
                onOpenTag = openedTag::set,
                onOpenStudio = {},
                onOpenTrailer = {},
            )

        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContentView(section.inflate(LayoutInflater.from(activity), null))
                section.start(activity)
                section.select()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val tagsComposeView = activity.findViewById<View>(R.id.tags_compose_view)
                findNestedScrollView(tagsComposeView)?.fullScroll(View.FOCUS_DOWN)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            composeRule.onNodeWithText(TAG_NAME).performClick()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            InstrumentationRegistry.getInstrumentation().waitForFocusedWindowContainingText(
                TAG_DESCRIPTION,
                AccessibilityWindowInfo.TYPE_APPLICATION,
            )

            onView(withText(TAG_DESCRIPTION)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(moreLabel)).inRoot(isDialog()).perform(click())
            assertEquals(TAG_NAME, openedTag.get())
            scenario.onActivity { section.clear() }
        }
    }

    private fun findNestedScrollView(view: View): NestedScrollView? {
        var parent: ViewParent? = view.parent
        while (parent is View) {
            if (parent is NestedScrollView) return parent
            parent = parent.parent
        }
        return null
    }

    private companion object {
        const val MEDIA_ID = 21L
        const val TAG_NAME = "High School"
        const val TAG_DESCRIPTION = "A sample tag description"
    }
}
