package com.mxt.anitrend.util

import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrend
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.media.MediaUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.arrayContainingInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.junit.MockitoJUnitRunner
import java.util.ArrayList

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class MediaUtilTests {

    @Mock
    private lateinit var list: MediaList

    @Mock
    private lateinit var media: MediaBase

    @Mock
    private lateinit var mediaTitle: MediaTitle

    @Before
    fun setupMocks() {
        `when`(list.media).thenReturn(media)
        `when`(media.title).thenReturn(mediaTitle)
    }

    @After
    fun resetMocks() {
        reset(list, media, mediaTitle)
    }

    @Test
    fun isAnimeType_givenNull_shouldReturnFalse() {
        assertThat(MediaUtil.isAnimeType(null), `is`(false))
    }

    @Test
    fun isAnimeType_givenAnime_shouldReturnTrue() {
        `when`(media.type).thenReturn(KeyUtil.ANIME)
        assertThat(MediaUtil.isAnimeType(media), `is`(true))
    }

    @Test
    fun isAnimeType_givenManga_shouldReturnFalse() {
        `when`(media.type).thenReturn(KeyUtil.MANGA)
        assertThat(MediaUtil.isAnimeType(media), `is`(false))
    }

    @Test
    fun isMangaType_givenNull_shouldReturnFalse() {
        assertThat(MediaUtil.isMangaType(null), `is`(false))
    }

    @Test
    fun isMangaType_givenAnime_shouldReturnFalse() {
        `when`(media.type).thenReturn(KeyUtil.ANIME)
        assertThat(MediaUtil.isMangaType(media), `is`(false))
    }

    @Test
    fun isMangaType_givenManga_shouldReturnTrue() {
        `when`(media.type).thenReturn(KeyUtil.MANGA)
        assertThat(MediaUtil.isMangaType(media), `is`(true))
    }

    @Test
    fun isIncrementLimitReached_ifProgressEqualToAnimeEpisodes_shouldReturnTrue() {
        val episodes = 10
        val progress = episodes

        `when`(media.type).thenReturn(KeyUtil.ANIME)
        `when`(list.progress).thenReturn(progress)
        `when`(media.episodes).thenReturn(episodes)

        assertThat(MediaUtil.isIncrementLimitReached(list), `is`(true))
    }

    @Test
    fun isIncrementLimitReached_ifProgressLessThanAnimeEpisodes_shouldReturnFalse() {
        val episodes = 10
        val progress = 3

        `when`(media.type).thenReturn(KeyUtil.ANIME)
        `when`(list.progress).thenReturn(progress)
        `when`(media.episodes).thenReturn(episodes)

        assertThat(MediaUtil.isIncrementLimitReached(list), `is`(false))
    }

    @Test
    fun isIncrementLimitReached_ifProgressGreaterThanAnimeEpisodes_shouldReturnFalse() {
        val episodes = 10
        val progress = 15

        `when`(media.type).thenReturn(KeyUtil.ANIME)
        `when`(list.progress).thenReturn(progress)
        `when`(media.episodes).thenReturn(episodes)

        assertThat(MediaUtil.isIncrementLimitReached(list), `is`(false))
    }

    @Test
    fun isIncrementLimitReached_ifProgressEqualToMangaChapters_shouldReturnTrue() {
        val chapters = 10
        val progress = chapters

        `when`(media.type).thenReturn(KeyUtil.MANGA)
        `when`(list.progress).thenReturn(progress)
        `when`(media.chapters).thenReturn(chapters)

        assertThat(MediaUtil.isIncrementLimitReached(list), `is`(true))
    }

    @Test
    fun isIncrementLimitReached_ifProgressLessThanMangaChapters_shouldReturnFalse() {
        val chapters = 10
        val progress = 7

        `when`(media.type).thenReturn(KeyUtil.MANGA)
        `when`(list.progress).thenReturn(progress)
        `when`(media.chapters).thenReturn(chapters)

        assertThat(MediaUtil.isIncrementLimitReached(list), `is`(false))
    }

    @Test
    fun isIncrementLimitReached_ifProgressGreaterThanMangaChapters_shouldReturnFalse() {
        val chapters = 10
        val progress = 20

        `when`(media.type).thenReturn(KeyUtil.MANGA)
        `when`(list.progress).thenReturn(progress)
        `when`(media.chapters).thenReturn(chapters)

        assertThat(MediaUtil.isIncrementLimitReached(list), `is`(false))
    }

    @Test
    fun isAllowedStatus_ifMediaIsNotYetReleased_shouldReturnFalse() {
        `when`(media.status).thenReturn(KeyUtil.NOT_YET_RELEASED)
        assertThat(MediaUtil.isAllowedStatus(list), `is`(false))
    }

    @Test
    fun isAllowedStatus_forAnyOtherStatus_shouldReturnTrue() {
        for (status in KeyUtil.MediaStatusValues.filterNotNull()) {
            if (KeyUtil.NOT_YET_RELEASED != status) {
                `when`(media.status).thenReturn(status)
                assertThat(
                    "Incrementing should be allowed for status: $status",
                    MediaUtil.isAllowedStatus(list),
                    `is`(true)
                )
            }
        }
    }

    @Test
    fun getMediaTitle_shouldReturnUserPreferredTitle() {
        val title = "Gintama"
        `when`(mediaTitle.userPreferred).thenReturn(title)
        assertThat(MediaUtil.getMediaTitle(media), equalTo(title))
    }

    @Test
    fun getMediaListTitle_shouldReturnUserPreferredTitle() {
        val title = "Gintama"
        `when`(mediaTitle.userPreferred).thenReturn(title)
        assertThat(MediaUtil.getMediaListTitle(list), equalTo(title))
    }

    @Test
    fun mapMediaTrend_shouldReturnCorrespondingMedia() {
        val media1 = mock(MediaBase::class.java)
        val media2 = mock(MediaBase::class.java)
        val media3 = mock(MediaBase::class.java)
        val mediaList = listOf(media1, media2, media3)

        val trendList = mediaList.map { media ->
            mock(MediaTrend::class.java).apply {
                `when`(this.media).thenReturn(media)
            }
        }

        assertThat(
            MediaUtil.mapMediaTrend(trendList).toTypedArray(),
            arrayContainingInAnyOrder(media1, media2, media3)
        )
    }

    @Test
    fun mapMediaTrend_givenNull_shouldReturnEmptyList() {
        assertThat(MediaUtil.mapMediaTrend(null), empty())
    }

    @Test
    fun getAiringMedia_shouldReturnReleasingMediaOnly() {
        val releasing = listOf(
            mock(MediaBase::class.java),
            mock(MediaBase::class.java)
        ).map { media ->
            mock(MediaList::class.java).apply {
                `when`(this.media).thenReturn(media)
            }
        }

        releasing.forEach { list ->
            `when`(list.media.status).thenReturn(KeyUtil.RELEASING)
        }

        val notReleasing = KeyUtil.MediaStatusValues
            .filterNotNull()
            .filter { it != KeyUtil.RELEASING }
            .map { status ->
                mock(MediaBase::class.java).apply {
                    `when`(this.status).thenReturn(status)
                }
            }
            .map { media ->
                mock(MediaList::class.java).apply {
                    `when`(this.media).thenReturn(media)
                }
            }

        val allMedia = ArrayList<MediaList>(releasing)
        allMedia.addAll(notReleasing)

        assertThat(
            MediaUtil.getAiringMedia(allMedia).toTypedArray(),
            equalTo(releasing.toTypedArray())
        )
    }

    @Test
    fun getAiringMedia_givenNull_shouldReturnEmptyList() {
        assertThat(MediaUtil.getAiringMedia(null), empty())
    }
}
