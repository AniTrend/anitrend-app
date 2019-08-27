package co.anitrend.support.markdown.text.render

import androidx.annotation.ColorInt
import co.anitrend.support.markdown.text.span.SpoilerLabelSpan
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import org.commonmark.node.Text

class SpoilerRender(
    @ColorInt private val backgroundColor: Int
) : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): Any? {
        configuration.spansFactory().require(Text::class.java)
        return SpoilerLabelSpan(backgroundColor)
    }
}