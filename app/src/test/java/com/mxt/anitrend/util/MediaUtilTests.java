package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrend;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.util.media.MediaUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mxt.anitrend.util.KeyUtil.ANIME;
import static com.mxt.anitrend.util.KeyUtil.MANGA;
import static com.mxt.anitrend.util.KeyUtil.MediaStatus;
import static com.mxt.anitrend.util.KeyUtil.NOT_YET_RELEASED;
import static com.mxt.anitrend.util.KeyUtil.RELEASING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MediaUtilTests {

    @Mock
    private MediaList list;
    @Mock
    private MediaBase media;
    @Mock
    private MediaTitle mediaTitle;

    @Before
    public void setupMocks() {
        when(list.getMedia()).thenReturn(media);
        when(media.getTitle()).thenReturn(mediaTitle);
    }

    @After
    public void resetMocks() {
        reset(list, media, mediaTitle);
    }

    /*
    isAnimeType
     */

    @Test
    public void isAnimeType_givenNull_shouldReturnFalse() {
        assertThat(MediaUtil.isAnimeType(null), is(false));
    }

    @Test
    public void isAnimeType_givenAnime_shouldReturnTrue() {
        when(media.getType()).thenReturn(ANIME);
        assertThat(MediaUtil.isAnimeType(media), is(true));
    }

    @Test
    public void isAnimeType_givenManga_shouldReturnFalse() {
        when(media.getType()).thenReturn(MANGA);
        assertThat(MediaUtil.isAnimeType(media), is(false));
    }

    /*
    isMangaType
     */

    @Test
    public void isMangaType_givenNull_shouldReturnFalse() {
        assertThat(MediaUtil.isMangaType(null), is(false));
    }

    @Test
    public void isMangaType_givenAnime_shouldReturnFalse() {
        when(media.getType()).thenReturn(ANIME);
        assertThat(MediaUtil.isMangaType(media), is(false));
    }

    @Test
    public void isMangaType_givenManga_shouldReturnTrue() {
        when(media.getType()).thenReturn(MANGA);
        assertThat(MediaUtil.isMangaType(media), is(true));
    }

    /*
    isIncrementLimitReached
     */

    @Test
    public void isIncrementLimitReached_ifProgressEqualToAnimeEpisodes_shouldReturnTrue() {
        int episodes = 10;
        //noinspection UnnecessaryLocalVariable
        int progress = episodes;

        when(media.getType()).thenReturn(ANIME);
        when(list.getProgress()).thenReturn(progress);
        when(media.getEpisodes()).thenReturn(episodes);

        assertThat(MediaUtil.isIncrementLimitReached(list), is(true));
    }

    @Test
    public void isIncrementLimitReached_ifProgressLessThanAnimeEpisodes_shouldReturnFalse() {
        int episodes = 10;
        int progress = 3;

        when(media.getType()).thenReturn(ANIME);
        when(list.getProgress()).thenReturn(progress);
        when(media.getEpisodes()).thenReturn(episodes);

        assertThat(MediaUtil.isIncrementLimitReached(list), is(false));
    }

    @Test
    public void isIncrementLimitReached_ifProgressGreaterThanAnimeEpisodes_shouldReturnFalse() {
        int episodes = 10;
        int progress = 15;

        when(media.getType()).thenReturn(ANIME);
        when(list.getProgress()).thenReturn(progress);
        when(media.getEpisodes()).thenReturn(episodes);

        assertThat(MediaUtil.isIncrementLimitReached(list), is(false));
    }

    @Test
    public void isIncrementLimitReached_ifProgressEqualToMangaChapters_shouldReturnTrue() {
        int chapters = 10;
        //noinspection UnnecessaryLocalVariable
        int progress = chapters;

        when(media.getType()).thenReturn(MANGA);
        when(list.getProgress()).thenReturn(progress);
        when(media.getChapters()).thenReturn(chapters);

        assertThat(MediaUtil.isIncrementLimitReached(list), is(true));
    }

    @Test
    public void isIncrementLimitReached_ifProgressLessThanMangaChapters_shouldReturnFalse() {
        int chapters = 10;
        int progress = 7;

        when(media.getType()).thenReturn(MANGA);
        when(list.getProgress()).thenReturn(progress);
        when(media.getChapters()).thenReturn(chapters);

        assertThat(MediaUtil.isIncrementLimitReached(list), is(false));
    }

    @Test
    public void isIncrementLimitReached_ifProgressGreaterThanMangaChapters_shouldReturnFalse() {
        int chapters = 10;
        //noinspection UnnecessaryLocalVariable
        int progress = 20;

        when(media.getType()).thenReturn(MANGA);
        when(list.getProgress()).thenReturn(progress);
        when(media.getChapters()).thenReturn(chapters);

        assertThat(MediaUtil.isIncrementLimitReached(list), is(false));
    }

    /*
    isAllowedStatus
     */

    @Test
    public void isAllowedStatus_ifMediaIsNotYetReleased_shouldReturnFalse() {
        when(media.getStatus()).thenReturn(NOT_YET_RELEASED);
        assertThat(MediaUtil.isAllowedStatus(list), is(false));
    }

    @Test
    public void isAllowedStatus_forAnyOtherStatus_shouldReturnTrue() {
        for (String status : MediaStatus) {
            if (!NOT_YET_RELEASED.equals(status)) {
                when(media.getStatus()).thenReturn(status);
                assertThat("Incrementing should be allowed for status: " + status,
                        MediaUtil.isAllowedStatus(list), is(true));
            }
        }
    }

    /*
    getMediaTitle
     */

    @Test
    public void getMediaTitle_shouldReturnUserPreferredTitle() {
        String title = "Gintama";
        when(mediaTitle.getUserPreferred()).thenReturn(title);
        assertThat(MediaUtil.getMediaTitle(media), equalTo(title));
    }

    /*
    getMediaListTitle
     */

    @Test
    public void getMediaListTitle_shouldReturnUserPreferredTitle() {
        String title = "Gintama";
        when(mediaTitle.getUserPreferred()).thenReturn(title);
        assertThat(MediaUtil.getMediaListTitle(list), equalTo(title));
    }

    /*
    mapMediaTrend
     */

    @Test
    public void mapMediaTrend_shouldReturnCorrespondingMedia() {

        MediaBase media1 = mock(MediaBase.class);
        MediaBase media2 = mock(MediaBase.class);
        MediaBase media3 = mock(MediaBase.class);
        List<MediaBase> mediaList = Arrays.asList(media1, media2, media3);


        List<MediaTrend> trendList = mediaList.stream().map(media -> {
            MediaTrend trend = mock(MediaTrend.class);
            when(trend.getMedia()).thenReturn(media);
            return trend;
        }).collect(Collectors.toList());

        assertThat(MediaUtil.mapMediaTrend(trendList).toArray(),
                arrayContainingInAnyOrder(media1, media2, media3));
    }

    @Test
    public void mapMediaTrend_givenNull_shouldReturnEmptyList() {
        assertThat(MediaUtil.mapMediaTrend(null), empty());
    }

    /*
    getAiringMedia
     */

    @Test
    public void getAiringMedia_shouldReturnReleasingMediaOnly() {
        List<MediaList> releasing =
                Stream.of(
                        mock(MediaBase.class),
                        mock(MediaBase.class))
                .map(media -> {
                    MediaList list = mock(MediaList.class);
                    when(list.getMedia()).thenReturn(media);
                    return list;
                }).collect(Collectors.toList());

        releasing.forEach(media -> when(media.getMedia().getStatus()).thenReturn(RELEASING));

        List<MediaList> notReleasing =
                Stream.of(MediaStatus)
                        .filter(status -> !RELEASING.equals(status))
                        .map(status -> {
                            MediaBase media = mock(MediaBase.class);
                            when(media.getStatus()).thenReturn(status);
                            return media;
                        })
                        .map(media -> {
                            MediaList list = mock(MediaList.class);
                            when(list.getMedia()).thenReturn(media);
                            return list;
                        }).collect(Collectors.toList());

        List<MediaList> allMedia = new ArrayList<>(releasing);
        allMedia.addAll(notReleasing);

        assertThat(MediaUtil.getAiringMedia(allMedia).toArray(), equalTo(releasing.toArray()));
    }

    @Test
    public void getAiringMedia_givenNull_shouldReturnEmptyList() {
        assertThat(MediaUtil.getAiringMedia(null), empty());
    }
}