package co.anitrend.support.markdown.core

import android.text.Layout
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import io.noties.markwon.simple.ext.SimpleExtPlugin

/**
 * Provides basic plugin for mention decoration and centering
 *
 * @param colorTint color to use for tinting mentioned users
 * @since 0.1.0
 */
class CoreDelimiterPlugin private constructor(
    @ColorInt private val colorTint: Int
) {

    fun delimiterPlugins() = SimpleExtPlugin.create { plugin ->
        plugin.addExtension(1, MENTION_DELIMITER) { _, _ ->
            ForegroundColorSpan(colorTint)
        }
        plugin.addExtension(3, CENTER_DELIMITER) { _, _ ->
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)
        }
    }

    companion object {
        /**
         * Mention user delimiter, only starts with `@`
         */
        private const val MENTION_DELIMITER = '\u0040'
        /**
         * Center text delimited by `~`
         * To center-align text, surround it with either `˜˜˜...˜˜˜` or `<center>...</center>`
         */
        private const val CENTER_DELIMITER = '\u007E'

        fun create(@ColorInt colorInt: Int) =
            CoreDelimiterPlugin(colorInt).delimiterPlugins()
    }
}