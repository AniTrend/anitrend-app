package com.mxt.anitrend.util;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EpisodeUtilTests {


    @Test
    public void episodeSupport_givenCrunchyrollUrl_shouldReturnTheFeedUrl() {
        String show = "my-hero-academia";
        ExternalLink link = mock(ExternalLink.class);
        when(link.getUrl()).thenReturn(BuildConfig.CRUNCHY_LINK + show);

        List<ExternalLink> links = Collections.singletonList(link);

        assertThat(EpisodeUtil.episodeSupport(links), equalTo(show + ".rss"));

    }

    @Test
    public void episodeSupport_givenFeedUrl_shouldReturnTheSameLink() {
        String url = BuildConfig.FEEDS_LINK + "my-hero-academia.rss";
        ExternalLink link = mock(ExternalLink.class);

        when(link.getUrl()).thenReturn(url);
        List<ExternalLink> links = Collections.singletonList(link);

        assertThat(EpisodeUtil.episodeSupport(links), equalTo(url));
    }

    @Test
    public void episodeSupport_givenCrunchyrollAndFeedUrl_shouldReturnEither() {
        String show = "my-hero-academia";
        String crunchyUrl = BuildConfig.CRUNCHY_LINK + show;
        String feedUrl = BuildConfig.FEEDS_LINK + show + ".rss";

        ExternalLink link1 = mock(ExternalLink.class);
        ExternalLink link2 = mock(ExternalLink.class);

        when(link1.getUrl()).thenReturn(crunchyUrl);
        when(link2.getUrl()).thenReturn(feedUrl);
        List<ExternalLink> links = Arrays.asList(link1, link2);

        assertThat(EpisodeUtil.episodeSupport(links),
                is(anyOf(
                        equalTo(show + ".rss"),
                        equalTo(feedUrl)
                )));
    }

    @Test
    public void episodeSupport_notGivenASupportedLink_shouldReturnNull() {
        String website = "https://heroaca.com/";
        String twitter = "https://twitter.com/heroaca_anime";

        ExternalLink link1 = mock(ExternalLink.class);
        ExternalLink link2 = mock(ExternalLink.class);

        when(link1.getUrl()).thenReturn(website);
        when(link2.getUrl()).thenReturn(twitter);
        List<ExternalLink> links = Arrays.asList(link1, link2);

        assertThat(EpisodeUtil.episodeSupport(links), nullValue());
    }
}