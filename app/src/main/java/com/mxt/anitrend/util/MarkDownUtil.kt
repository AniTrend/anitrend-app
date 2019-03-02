package com.mxt.anitrend.util

import android.content.Context
import android.os.Build
import android.support.v7.widget.AppCompatTextView
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.util.Log

import com.github.rjeschke.txtmark.Processor

/**
 * Created by max on 2017/03/26.
 * Moved markdown processor to global location
 */
object MarkDownUtil {

    private fun fromMD(content: String): SpannableStringBuilder {
        return try {
            val processedText = Processor.process(content)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(processedText,
                        Html.FROM_HTML_MODE_LEGACY)
                else -> Html.fromHtml(processedText)
            } as SpannableStringBuilder
        } catch (e: Exception) {
            e.printStackTrace()
            SpannableStringBuilder("Unable to process content")
        }
    }

    fun convert(input: String?): Spanned {
        var result: SpannableStringBuilder
        result = if (input.isNullOrBlank())
            fromMD("<b>No content available</b>")
        else
            fromMD(RegexUtil.findUserTags(input))

        try {
            if (result.isNotEmpty())
                while (result[result.length - 1] == '\n')
                    result = result.delete(result.length - 1, result.length)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("convert(input)", e.message)
        }

        return result
    }

    fun convert(input: String?, context: Context, source: AppCompatTextView): Spanned {
        var result: SpannableStringBuilder
        result = when {
            input.isNullOrBlank() -> fromMD("<b>No content available</b>")
            else -> fromMD(RegexUtil.findUserTags(input))
        }
        // result = fromMD(RegexUtil.findUserTags(input), context, source);

        try {
            if (result.isNotEmpty())
                while (result[result.length - 1] == '\n')
                    result = result.delete(result.length - 1, result.length)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("convert(input...)", e.message)
        }

        return result
    }

    fun convertLink(text: String) = RegexUtil.createLinkStandard(text)

    fun convertImage(text: String) = RegexUtil.createImageStandard(text)

    fun convertYoutube(text: String) = RegexUtil.createYoutubeStandard(text)

    fun convertVideo(text: String) = RegexUtil.createWebMStandard(text)
}
