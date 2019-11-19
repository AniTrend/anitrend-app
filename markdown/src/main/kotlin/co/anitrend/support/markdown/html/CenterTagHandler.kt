package co.anitrend.support.markdown.html

import android.text.Layout
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.tag.SimpleTagHandler

class CenterTagHandler : SimpleTagHandler() {

    override fun getSpans(
        configuration: MarkwonConfiguration,
        renderProps: RenderProps,
        tag: HtmlTag
    ): Any = Layout.Alignment.ALIGN_CENTER

    override fun supportedTags() = listOf("center")
}