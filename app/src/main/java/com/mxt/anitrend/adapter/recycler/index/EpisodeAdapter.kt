package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.episode.EpisodeRow
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Binds episode cards and forwards their image actions to the existing list owner. */
class EpisodeAdapter(
    context: Context,
) : RecyclerViewAdapter<Episode>(context) {
    private fun formatDuration(duration: String?): String {
        val timeSpan = duration?.toLongOrNull() ?: return "00:00"
        val minutes = TimeUnit.SECONDS.toMinutes(timeSpan)
        val seconds = timeSpan - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), if (seconds < 10) "%d:0%d" else "%d:%d", minutes, seconds)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<Episode> {
        val cardMargin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
        val composeView =
            ComposeView(parent.context).apply {
                id = R.id.series_image
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        setMargins(cardMargin, cardMargin, cardMargin, cardMargin)
                    }
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            }
        return EpisodeViewHolder(composeView)
    }

    override fun getFilter(): Filter? = null

    private inner class EpisodeViewHolder(
        private val composeView: ComposeView,
    ) : RecyclerViewHolder<Episode>(composeView) {
        private val episode = mutableStateOf<Episode?>(null)

        init {
            composeView.setContent {
                AniTrendTheme {
                    episode.value?.let { model ->
                        EpisodeRow(
                            title = model.title,
                            duration = formatDuration(model.content?.duration),
                            imageUrl = model.thumbnail.firstOrNull()?.url,
                            onClick = { this@EpisodeViewHolder.onClick(itemView) },
                            onLongClick = { this@EpisodeViewHolder.onLongClick(itemView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: Episode) {
            episode.value = model
        }

        override fun onViewRecycled() {
            episode.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, imageClickTarget(v))
        }

        override fun onLongClick(view: View): Boolean = performLongClick(clickListener, data, imageClickTarget(view))

        private fun imageClickTarget(view: View): View = requireNotNull(view.findViewById(R.id.series_image)) {
            "EpisodeRow must expose the existing series_image action target."
        }
    }
}
