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

import android.content.Context
import android.webkit.WebSettings
import co.anitrend.support.markdown.center.CenterPlugin
import co.anitrend.support.markdown.core.CorePlugin
import co.anitrend.support.markdown.ephasis.EmphasisPlugin
import co.anitrend.support.markdown.heading.HeadingPlugin
import co.anitrend.support.markdown.horizontal.HorizontalLinePlugin
import co.anitrend.support.markdown.image.ImagePlugin
import co.anitrend.support.markdown.italics.ItalicsPlugin
import co.anitrend.support.markdown.mention.MentionPlugin
import co.anitrend.support.markdown.spoiler.SpoilerPlugin
import co.anitrend.support.markdown.strike.StrikeThroughPlugin
import co.anitrend.support.markdown.webm.WebMPlugin
import co.anitrend.support.markdown.youtube.YouTubePlugin
import com.bumptech.glide.Glide
import com.mxt.anitrend.base.plugin.image.GlideImagePlugin
import com.mxt.anitrend.base.plugin.image.ImageConfigurationPlugin
import com.mxt.anitrend.base.plugin.text.TextConfigurationPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/*
 * The single Markwon renderer configuration for the whole application: the
 * legacy View hosts and Compose routes all share one plugin stack; only the
 * link dispatch callback varies per owner.
 */

/**
 * Builds the application markdown renderer. [onLinkClick] receives raw link
 * targets; the owning route decides how decoded spoiler text, native video and
 * external links are handled (see [markdownLinkHandler] for the shared default).
 */
fun createMarkwon(context: Context, onLinkClick: (String) -> Unit): Markwon = Markwon.builder(context)
    .usePlugin(HtmlPlugin.create())
    .usePlugin(CorePlugin.create())
    .usePlugin(HorizontalLinePlugin.create())
    .usePlugin(HeadingPlugin.create())
    .usePlugin(EmphasisPlugin.create())
    .usePlugin(CenterPlugin.create())
    .usePlugin(ImagePlugin.create())
    .usePlugin(WebMPlugin.create())
    .usePlugin(YouTubePlugin.create())
    .usePlugin(LinkifyPlugin.create())
    .usePlugin(SpoilerPlugin.create())
    .usePlugin(MentionPlugin.create())
    .usePlugin(StrikeThroughPlugin.create())
    .usePlugin(StrikethroughPlugin.create())
    .usePlugin(TaskListPlugin.create(context))
    .usePlugin(ItalicsPlugin.create())
    .usePlugin(
        GlideImagesPlugin.create(
            GlideImagePlugin.create(
                Glide.with(context),
                WebSettings.getDefaultUserAgent(context),
            ),
        ),
    )
    .usePlugin(ImageConfigurationPlugin.create())
    .usePlugin(TextConfigurationPlugin.create(onLinkClick))
    .build()
