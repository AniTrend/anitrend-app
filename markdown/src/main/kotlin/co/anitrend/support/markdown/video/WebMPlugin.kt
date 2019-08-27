package co.anitrend.support.markdown.video

import androidx.annotation.VisibleForTesting
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin.Companion.NO_THUMBNAIL
import io.noties.markwon.AbstractMarkwonPlugin
import java.lang.reflect.Modifier

/**
 * Surround the URL with __webm(https://files.kiniro.uk/video/sonic.webm)__
 *
 * Note that, despite the name, _any_ audio or video file will work - but may not actually be supported by all browsers.
 *
 * PS: Does not interact well with code blocks.
 *
 * @since 0.1.0
 */
class WebMPlugin private constructor(): IMarkdownPlugin, AbstractMarkwonPlugin() {

    /**
     * Regular expression that should be used for the implementing classing
     */
    override val regex = Regex(
        pattern = PATTERN_WEB_M,
        option = RegexOption.IGNORE_CASE
    )

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

                replacement = replacement.replace(
                    regex,
                    """<img src="$NO_THUMBNAIL" width="100%" target="$resourceUrl"/>"""
                )
            }
            return replacement
        }
        return super.processMarkdown(markdown)
    }

    companion object {

        @VisibleForTesting(otherwise = Modifier.PRIVATE)
        const val PATTERN_WEB_M = "webm(\\d*|\\d*px|\\d*%)(\\([^)]+\\))"

        private const val GROUP_ORIGINAL_MATCH = 0
        private const val GROUP_CONTENT_SIZE = 1
        private const val GROUP_CONTENT = 2

        fun create() =
            WebMPlugin()
    }
}