package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterLatestAnimeBinding
import com.mxt.anitrend.databinding.AdapterLatestMangaBinding
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.KeyUtil.RecyclerViewType

/**
 * Adapter for rendering the latest anime and manga media cards.
 */
class MediaLatestAdapter(context: Context) : RecyclerViewAdapter<MediaBase>(context) {

    private val cardInteractionIds = intArrayOf(
        R.id.container,
        R.id.series_image,
        R.id.series_status,
        R.id.series_airing,
        R.id.series_title,
        R.id.series_year_type,
        R.id.custom_rating_widget,
    )

    override fun onCreateViewHolder(parent: ViewGroup, @RecyclerViewType viewType: Int): RecyclerViewHolder<MediaBase> = if (viewType == KeyUtil.RECYCLER_TYPE_ANIME) {
        LatestAnimeViewHolder(AdapterLatestAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    } else {
        LatestMangaViewHolder(AdapterLatestMangaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RecyclerViewType
    override fun getItemViewType(position: Int): Int = if (data[position].type == KeyUtil.ANIME) {
        KeyUtil.RECYCLER_TYPE_ANIME
    } else {
        KeyUtil.RECYCLER_TYPE_MANGA
    }

    override fun getFilter(): Filter? = null

    /**
     * View holder for the latest anime card layout.
     */
    inner class LatestAnimeViewHolder(
        private val binding: AdapterLatestAnimeBinding,
    ) : RecyclerViewHolder<MediaBase>(binding.root) {

        init {
            cardInteractionIds.forEach { viewId ->
                bindClickListeners(viewId)
                bindLongClickListeners(viewId)
            }
        }

        override fun onBindViewHolder(model: MediaBase) {
            AspectImageView.setImage(binding.seriesImage, model.coverImage)
            SeriesStatusWidget.setStatus(binding.seriesStatus, model)
            AiringTextView.setAiring(binding.seriesAiring, model)
            SeriesYearTypeTextView.htmlText(binding.seriesYearType, model)
            binding.customRatingWidget.setAverageRating(model)
            binding.seriesTitle.setTitle(model)
        }

        override fun onViewRecycled() {
            Glide.with(context).clear(binding.seriesImage)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, itemView)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, itemView)
    }

    /**
     * View holder for the latest manga card layout.
     */
    inner class LatestMangaViewHolder(
        private val binding: AdapterLatestMangaBinding,
    ) : RecyclerViewHolder<MediaBase>(binding.root) {

        init {
            cardInteractionIds.forEach { viewId ->
                bindClickListeners(viewId)
                bindLongClickListeners(viewId)
            }
        }

        override fun onBindViewHolder(model: MediaBase) {
            AspectImageView.setImage(binding.seriesImage, model.coverImage)
            SeriesStatusWidget.setStatus(binding.seriesStatus, model)
            SeriesYearTypeTextView.htmlText(binding.seriesYearType, model)
            binding.customRatingWidget.setAverageRating(model)
            binding.seriesTitle.setTitle(model)
            binding.seriesAiring.text = binding.root.context.getString(R.string.label_latest_release)
        }

        override fun onViewRecycled() {
            Glide.with(context).clear(binding.seriesImage)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, itemView)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, itemView)
    }
}
