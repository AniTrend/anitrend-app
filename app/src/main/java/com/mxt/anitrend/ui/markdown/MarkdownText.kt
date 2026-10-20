/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.markdown

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import io.noties.markwon.Markwon
import me.saket.bettermovementmethod.BetterLinkMovementMethod

/*
 * The only application-authored AndroidView. It renders markdown into a single
 * TextView; it is never a RecyclerView, Fragment or screen host.
 */

/**
 * Renders [markdown] with the configured [markwon] renderer. Link targets are
 * dispatched to [onLinkClick] and long-pressed links to [onLinkLongClick];
 * the latest callbacks are always used even across lazy reuse. [maxLines] and
 * [fontStyle] preserve existing clamped/italic host styling, and [onViewClick]
 * restores whole-text click dispatch on the underlying selectable TextView,
 * which consumes taps before any Compose wrapper.
 */
@Composable
internal fun MarkdownText(
    markdown: String,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
    onLinkLongClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    fontStyle: FontStyle? = null,
    onViewClick: (() -> Unit)? = null,
) {
    val currentOnLinkClick by rememberUpdatedState(onLinkClick)
    val currentOnLinkLongClick by rememberUpdatedState(onLinkLongClick)
    val currentOnViewClick by rememberUpdatedState(onViewClick)

    if (LocalInspectionMode.current) {
        Text(text = markdown, modifier = modifier, style = LocalTextStyle.current)
        return
    }

    AndroidView(
        factory = { context ->
            RichMarkdownTextView(context).apply {
                this.maxLines = maxLines
                if (fontStyle == FontStyle.Italic) {
                    setTypeface(typeface, android.graphics.Typeface.ITALIC)
                }
                movementMethod =
                    BetterLinkMovementMethod
                        .newInstance()
                        .setOnLinkClickListener { _, url ->
                            currentOnLinkClick(url)
                            true
                        }
                        .setOnLinkLongClickListener { _, url ->
                            currentOnLinkLongClick(url)
                            true
                        }
            }
        },
        update = { view ->
            view.setOnClickListener { currentOnViewClick?.invoke() }
            view.setMarkDownText(markdown, markwon)
        },
        onRelease = { view ->
            view.movementMethod = null
            view.setOnLongClickListener(null)
            view.setOnClickListener(null)
            view.text = ""
        },
        modifier = modifier,
    )
}
