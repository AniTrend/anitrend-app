package com.mxt.anitrend.base.plugin.text

import android.content.Intent
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.base.VideoPlayerActivity
import com.mxt.anitrend.view.sheet.BottomSheetSpoiler
import io.noties.markwon.*

internal class TextConfigurationPlugin private constructor() : AbstractMarkwonPlugin() {

    private val regex = Regex("mp4|webm", RegexOption.IGNORE_CASE)

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
                    val mediaType = MimeTypeMap.getFileExtensionFromUrl(link)
                    if (mediaType.matches(regex)) {
                        val intent = Intent(view.context, VideoPlayerActivity::class.java)
                        intent.putExtra(KeyUtil.arg_model, link)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        view.context.startActivity(intent)
                    } else
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