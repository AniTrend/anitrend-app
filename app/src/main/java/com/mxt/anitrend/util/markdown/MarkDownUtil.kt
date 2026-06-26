package com.mxt.anitrend.util.markdown

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import io.noties.markwon.Markwon
import timber.log.Timber

/**
 * Created by max on 2017/03/26.
 * Moved markdown processor to global location
 */
object MarkDownUtil {
    private fun fromMD(context: Context, content: String): SpannableStringBuilder = try {
        val rendered = Markwon.create(context).toMarkdown(content)
        SpannableStringBuilder.valueOf(rendered)
    } catch (e: Exception) {
        Timber.e(e)
        SpannableStringBuilder("Unable to process content")
    }

    fun convert(context: Context, input: String?): Spanned {
        var result =
            when (input.isNullOrBlank()) {
                true -> fromMD(context, "<b>No content available</b>")
                else ->
                    fromMD(
                        context,
                        RegexUtil.findUserTags(
                            input,
                        ),
                    )
            }

        try {
            if (result.isNotEmpty()) {
                while (result.last() == '\n') {
                    result = result.delete(result.lastIndex, result.length)
                }
            }
        } catch (e: Exception) {
            Timber.tag("convert(input)").w(e)
        }

        return result
    }

    fun convertLink(text: String) = RegexUtil.createLinkStandard(text)

    fun convertImage(text: String) = RegexUtil.createImageStandard(text)

    fun convertYoutube(text: String) = RegexUtil.createYoutubeStandard(text)

    fun convertVideo(text: String) = RegexUtil.createWebMStandard(text)
}
