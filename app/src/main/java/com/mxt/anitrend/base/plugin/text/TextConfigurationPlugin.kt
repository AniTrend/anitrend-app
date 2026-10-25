package com.mxt.anitrend.base.plugin.text

import android.view.View
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.MarkwonConfiguration

/**
 * Emits clicked link targets to [onLinkClick]. The owning route performs the
 * dispatch for decoded spoiler text, native video and external links; this
 * plugin never resolves destinations itself.
 */
internal class TextConfigurationPlugin
private constructor(
    private val onLinkClick: (String) -> Unit,
) : AbstractMarkwonPlugin() {

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.linkResolver(object : LinkResolverDef() {
            override fun resolve(view: View, link: String) {
                onLinkClick(link)
            }
        })
    }

    companion object {
        fun create(onLinkClick: (String) -> Unit) = TextConfigurationPlugin(onLinkClick)
    }
}
