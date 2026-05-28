package com.mxt.anitrend.util

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.util.collection.EpisodeUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class EpisodeUtilTests {

    @Test
    fun episodeSupport_givenCrunchyrollUrl_shouldReturnTheFeedUrl() {
        val show = "my-hero-academia"
        val link = mock(ExternalLink::class.java)
        `when`(link.url).thenReturn(BuildConfig.CRUNCHY_LINK + show)

        val links = listOf(link)

        assertThat(EpisodeUtil.episodeSupport(links), equalTo(show + ".rss"))
    }

    @Test
    fun episodeSupport_givenFeedUrl_shouldReturnTheSameLink() {
        val url = BuildConfig.FEEDS_LINK + "my-hero-academia.rss"
        val link = mock(ExternalLink::class.java)

        `when`(link.url).thenReturn(url)
        val links = listOf(link)

        assertThat(EpisodeUtil.episodeSupport(links), equalTo(url))
    }

    @Test
    fun episodeSupport_givenCrunchyrollAndFeedUrl_shouldReturnEither() {
        val show = "my-hero-academia"
        val crunchyUrl = BuildConfig.CRUNCHY_LINK + show
        val feedUrl = BuildConfig.FEEDS_LINK + show + ".rss"

        val link1 = mock(ExternalLink::class.java)
        val link2 = mock(ExternalLink::class.java)

        `when`(link1.url).thenReturn(crunchyUrl)
        `when`(link2.url).thenReturn(feedUrl)
        val links = listOf(link1, link2)

        assertThat(
            EpisodeUtil.episodeSupport(links),
            `is`(
                anyOf(
                    equalTo(show + ".rss"),
                    equalTo(feedUrl)
                )
            )
        )
    }

    @Test
    fun episodeSupport_notGivenASupportedLink_shouldReturnNull() {
        val website = "https://heroaca.com/"
        val twitter = "https://twitter.com/heroaca_anime"

        val link1 = mock(ExternalLink::class.java)
        val link2 = mock(ExternalLink::class.java)

        `when`(link1.url).thenReturn(website)
        `when`(link2.url).thenReturn(twitter)
        val links = listOf(link1, link2)

        assertThat(EpisodeUtil.episodeSupport(links), nullValue())
    }
}
