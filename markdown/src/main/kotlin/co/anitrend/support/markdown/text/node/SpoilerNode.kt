package co.anitrend.support.markdown.text.node

import co.anitrend.support.markdown.text.span.SpoilerContentSpan
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Text

class SpoilerNode(
    private val regex: Regex
) : MarkwonVisitor.NodeVisitor<Text> {

    private fun isSpoiler(text: CharSequence) = regex.containsMatchIn(text)

    override fun visit(visitor: MarkwonVisitor, text: Text) {
        if (isSpoiler(text.literal)) {
            visitor.setSpans(
                0,
                SpoilerContentSpan(
                    0,
                    0,
                    text.literal
                )
            )
        }
    }
}