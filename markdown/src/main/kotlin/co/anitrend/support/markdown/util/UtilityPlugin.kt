package co.anitrend.support.markdown.util

import android.content.Intent
import android.net.Uri
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.SoftLineBreak


/**
 * Provides basic plugin for handling link clicking
 *
 * @since 0.1.0
 */
class UtilityPlugin private constructor(): AbstractMarkwonPlugin() {

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.linkResolver { view, link ->
            val context = view.context
            runCatching {
                context.startActivity(
                    Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(link)
                    }
                )
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(SoftLineBreak::class.java) { visitor, _ ->
            visitor.forceNewLine()
        }
    }

    companion object {
        fun create() = UtilityPlugin()
    }
}