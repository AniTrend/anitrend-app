package co.anitrend.support.markdown.text.node

import androidx.annotation.ColorInt
import co.anitrend.support.markdown.text.span.SpoilerContentSpan
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Text

class SpoilerNode(
    private val regex: Regex,
    @ColorInt private val backgroundColor: Int,
    @ColorInt private val textColor: Int
) : MarkwonVisitor.NodeVisitor<Text> {

    private fun isSpoiler(text: CharSequence) = regex.containsMatchIn(text)

    override fun visit(visitor: MarkwonVisitor, text: Text) {
        if (isSpoiler(text.literal)) {
            visitor.setSpans(
                0,
                SpoilerContentSpan(
                    backgroundColor,
                    textColor,
                    text.literal
                )
            )
        }
    }
}