package co.anitrend.support.markdown.video

import androidx.annotation.VisibleForTesting
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin
import io.noties.markwon.AbstractMarkwonPlugin
import java.lang.reflect.Modifier

/**
 * Surround the URL with __youtube(https://www.youtube.com/watch?v=D0q0QeQbw9U)__:
 *
 * Note that only the __D0q0QeQbw9U__ part is actually required:
 *
 * youtube(D0q0QeQbw9U)
 *
 * Again, this is _always_ converted - even in code blocks.
 *
 * @since 0.1.0
 */
class YouTubePlugin private constructor(): IMarkdownPlugin, AbstractMarkwonPlugin() {

    /**
     * Regular expression that should be used for the implementing classing
     */
    override val regex = Regex(
        pattern = PATTERN_YOUTUBE,
        option = RegexOption.IGNORE_CASE
    )

    /**
     * Creates a youtube thumbnail from a given youtube url
     *
     * @param link full youtube link derived from [FULL_LINK]
     */
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun buildYoutubeThumbnail(link: String): String {
        val mediaIdMatchResult = Regex(PATTERN_YOUTUBE_EXTRACT).find(link)
        if (mediaIdMatchResult != null) {
            val matcherGroups = mediaIdMatchResult.groups.size
            val matcherValueGroups = mediaIdMatchResult.groupValues
            val mediaId = matcherValueGroups[matcherGroups - 1]

            return String.format(THUMBNAIL, mediaId)
        }
        return IMarkdownPlugin.NO_THUMBNAIL
    }

    /**
     * Creates a standard youtube URL from the flavored markdown or short link
     *
     * @param markdown flavored markdown selector for youtube
     * @see PATTERN_YOUTUBE
     */
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun buildYoutubeFullLink(markdown: String): String {
        return if (!markdown.contains(TRIGGER)) {
            if (markdown.contains(SHORT_LINK))
                FULL_LINK + markdown.replace(SHORT_LINK, "")
            else
                FULL_LINK + markdown
        } else markdown
    }

    override fun processMarkdown(markdown: String): String {
        if (regex.containsMatchIn(markdown)) {
            val matches = regex.findAll(markdown)
            var replacement = markdown
            matches.forEach { matchResult ->

                val resourceUrl = matchResult.groupValues[GROUP_CONTENT].let {
                    val first = it.indexOfFirst { char -> char == '(' }
                    val last = it.indexOfLast { char -> char == ')' }
                    it.subSequence(IntRange(first + 1, last - 1)).toString()
                }

                val youtubeLink = buildYoutubeFullLink(resourceUrl)
                val thumbnailUrl = buildYoutubeThumbnail(youtubeLink)

                replacement = replacement.replace(
                    regex,
                    """<img src="$thumbnailUrl" width="100%" target="$resourceUrl"/>"""
                )
            }
            return replacement
        }
        return super.processMarkdown(markdown)
    }

    companion object {

        private const val TRIGGER = "youtube"

        private const val FULL_LINK = "https://www.youtube.com/watch?v="
        private const val SHORT_LINK = "https://youtu.be/"
        private const val THUMBNAIL = "https://img.youtube.com/vi/%s/hqdefault.jpg"

        @VisibleForTesting(otherwise = Modifier.PRIVATE)
        const val PATTERN_YOUTUBE = "youtube(\\d*|\\d*px|\\d*%)(\\([^)]+\\))"

        @VisibleForTesting(otherwise = Modifier.PRIVATE)
        const val PATTERN_YOUTUBE_EXTRACT = "(\\?v=)(.*)"

        private const val GROUP_ORIGINAL_MATCH = 0
        private const val GROUP_CONTENT_SIZE = 1
        private const val GROUP_CONTENT = 2

        fun create() =
            YouTubePlugin()
    }
}