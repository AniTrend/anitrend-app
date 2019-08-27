package co.anitrend.support.markdown.image

import androidx.annotation.VisibleForTesting
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.image.ImageProps
import org.commonmark.node.*
import java.lang.reflect.Modifier

/**
 * Images look like links, but with a `!` in front:
 * __`![fallback text](https://anilist.co/img/icons/icon.svg)`__
 *
 * You can also use HTML:
 * __<img alt="fallback text" src="https://anilist.co/img/icons/icon.svg">__
 *
 * There's also an (Anilist-specific) way to specify a size:
 * __img###(https://anilist.co/img/icons/icon.svg) where `###` is the width in pixels, such as `420`__
 *
 * Note that the `img` code is __always__ converted (even within code blocks) so be careful when trying to explain how it works to other users!
 *
 * @since 0.1.0
 */
class ImageRenderPlugin private constructor(): IMarkdownPlugin, AbstractMarkwonPlugin() {

    /**
     * Regular expression that should be used for the implementing classing
     */
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    override val regex = Regex(
        pattern = PATTERN_IMAGE,
        option = RegexOption.IGNORE_CASE
    )

    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(CorePlugin::class.java) { corePlugin: CorePlugin ->
            corePlugin.addOnTextAddedListener { visitor, text, start ->
                constructImageHandler(visitor, text, start)
            }
        }
    }

    private fun constructImageHandler(visitor: MarkwonVisitor, text: String, start: Int) {
        if (text.contains(regex)) {
            val match = regex.findAll(text)
            match.forEach { matchResult ->
                val groups = matchResult.groups
                val target = groups[GROUP_ORIGINAL_MATCH]?.value
                if (target != null) {
                    val imageSource = groups[GROUP_IMAGE_SRC]?.value?.let {
                        val first = it.indexOfFirst { char -> char == '(' }
                        val last = it.indexOfLast { char -> char == ')' }
                        it.subSequence(IntRange(first + 1, last - 1)).toString()
                    }

                    ImageProps.DESTINATION.set(visitor.renderProps(), imageSource)

                    /*ImageProps.IMAGE_SIZE.set(
                    visitor.renderProps(),
                    ImageSize(imageWidth, null)
                )*/
                    // it's important to use `start` parameter for correct spans placement
                    // oops, cannot use that here
                    // visitor.setSpansForNode(Image::class.java, start + matchResult.start())

                    val matchRange = matchResult.range

                    val spanFactory =
                        visitor.configuration().spansFactory().require(Image::class.java)
                    SpannableBuilder.setSpans(
                        visitor.builder(),
                        spanFactory.getSpans(
                            visitor.configuration(),
                            visitor.renderProps()
                        ),
                        start + matchRange.first,
                        start + matchRange.last + 1
                    )
                }
            }
        }
    }

    companion object {

        @VisibleForTesting(otherwise = Modifier.PRIVATE)
        const val PATTERN_IMAGE = "(img)(\\d*|\\d*px|\\d*%)(\\([^)]+\\))"

        private const val GROUP_ORIGINAL_MATCH = 0
        private const val GROUP_IMAGE_SIZE = 2
        private const val GROUP_IMAGE_SRC = 3

        fun create() = ImageRenderPlugin()
    }
}