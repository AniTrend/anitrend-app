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

import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.mxt.anitrend.view.activity.base.VideoPlayerActivity

private val videoExtension = Regex("mp4|webm", RegexOption.IGNORE_CASE)

private fun decodeDataIn(link: String): String? = link.toUri().getQueryParameter("data")

/**
 * Compose markdown link dispatch: decoded spoiler links surface through
 * [onSpoiler] so the owning entry shows the entry-owned Compose spoiler
 * sheet, mp4/webm links open the in-app player, anything else opens
 * externally.
 */
@Composable
internal fun rememberMarkdownLinkHandler(onSpoiler: (String?) -> Unit): (String) -> Unit {
    val context = LocalContext.current
    val currentOnSpoiler by rememberUpdatedState(onSpoiler)
    return remember(context) {
        { link: String ->
            when {
                link.startsWith("app.anitrend") -> currentOnSpoiler(decodeDataIn(link))
                MimeTypeMap.getFileExtensionFromUrl(link).matches(videoExtension) ->
                    context.startActivity(
                        VideoPlayerActivity.newIntent(context, link),
                    )
                else -> {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, link.toUri()).apply {
                            if (context !is android.app.Activity) {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        },
                    )
                }
            }
        }
    }
}
