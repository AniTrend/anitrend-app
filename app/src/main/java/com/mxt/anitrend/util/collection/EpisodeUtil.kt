package com.mxt.anitrend.util.collection

import com.mxt.anitrend.model.entity.anilist.ExternalLink

/**
 * Created by max on 2017/11/04.
 * Episode list helper that check for crunchy feeds
 */

object EpisodeUtil {

    private const val crunchyStandard = "crunchyroll"
    private const val feedStandard = "feedburner"

    fun episodeSupport(links: List<ExternalLink>): String? {
        for (link in links) {
            val url = link.url
            if (url?.contains(crunchyStandard) == true)
                return linkStripper(url)
            else if (url?.contains(feedStandard) == true)
                return url
        }
        return null
    }

    private fun linkStripper(link: String): String {
        val lastIndex = link.lastIndexOf("/") + 1
        val stripped = link.substring(lastIndex)
        return String.format("%s.rss", stripped)
    }

    /**
     * Gets the series title without description
     */
    fun getActualTile(episodeTitle: String): String {
        return episodeTitle.replace("((.- Episode)|(.Season)).*".toRegex(), "")
    }
}
