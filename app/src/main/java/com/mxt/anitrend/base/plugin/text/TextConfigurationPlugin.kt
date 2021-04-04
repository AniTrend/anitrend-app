package com.mxt.anitrend.base.plugin.text

import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.view.sheet.BottomSheetSpoiler
import io.noties.markwon.*

internal class TextConfigurationPlugin private constructor() : AbstractMarkwonPlugin() {

    private fun decodeDataIn(link: String): String? {
        val uri = link.toUri()
        return uri.getQueryParameter("data")
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.linkResolver(object : LinkResolverDef() {
            override fun resolve(view: View, link: String) {
                if (link.startsWith("app.anitrend")) {
                    val context = view.context
                    if (context is FragmentActivity) {
                        val sheet = BottomSheetSpoiler.Builder()
                            .setText(decodeDataIn(link))
                            .setTitle(R.string.title_bottom_sheet_spoiler)
                            .build()
                        sheet.show(context.supportFragmentManager, sheet.TAG)
                    }
                } else {
                    super.resolve(view, link)
                }
            }
        })
    }

    companion object {
        fun create() =
            TextConfigurationPlugin()
    }
}