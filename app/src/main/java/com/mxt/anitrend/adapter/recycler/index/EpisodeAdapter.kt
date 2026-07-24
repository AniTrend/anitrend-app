package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.databinding.AdapterEpisodeBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.crunchy.Episode
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by max on 2017/11/04.
 */
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
    ): RecyclerViewHolder<Episode> = EpisodeViewHolder(
        AdapterEpisodeBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    inner class EpisodeViewHolder(
        private val binding: AdapterEpisodeBinding,
    ) : RecyclerViewHolder<Episode>(binding.root) {
        init {
            bindClickListeners(R.id.series_image)
            bindLongClickListeners(R.id.series_image)
        }

        override fun onBindViewHolder(model: Episode) {
            WideImageView.setImage(binding.seriesImage, model.thumbnail?.getOrNull(0)?.url)
            binding.seriesDuration.text = formatDuration(model.content?.duration)
            binding.seriesTitle.text = model.title
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.seriesImage)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
