package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.LinearLayout.LayoutParams
import androidx.viewbinding.ViewBinding
import com.annimon.stream.Stream
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterSeriesAiringBinding
import com.mxt.anitrend.databinding.AdapterSeriesAiringCompactBinding
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.media.MediaListUtil.isFilterMatch
import java.util.*

/**
 * Created by max on 2017/11/03.
 * adapter for series lists
 */
class MediaListAdapter(context: Context) :
    RecyclerViewAdapter<MediaList>(context) {
    private var currentUser: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder<MediaList> {
        val adapter = when (presenter.settings.mediaListStyle) {
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X1,
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X2 -> {
                AdapterSeriesAiringCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
            else -> {
                AdapterSeriesAiringBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        }

        return SeriesListViewHolder(adapter)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                if (CompatUtil.isEmpty(clone)) clone = data

                val filter = constraint.toString()
                if (TextUtils.isEmpty(filter)) {
                    results.values = ArrayList<MediaList>(clone.orEmpty())
                    clone = null
                } else {
                    results.values = Stream.of<MediaList>(clone.orEmpty())
                        .filter { c: MediaList ->
                            isFilterMatch(c, filter)
                        }
                        .toList()
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                val filtered = (results.values as? List<*>)?.filterIsInstance<MediaList>() ?: return
                data = filtered.toMutableList()
                notifyDataSetChanged()
            }
        }
    }

    fun setCurrentUser(currentUser: String?) {
        this.currentUser = currentUser
    }

    inner class SeriesListViewHolder
    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
      */(private val binding: ViewBinding) :
        RecyclerViewHolder<MediaList>(binding.root) {

        init {
            bindClickListeners(R.id.series_image, R.id.container)
            bindLongClickListeners(R.id.series_image, R.id.container)
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br></br>
         *
         * @param model Is the model at the current adapter position
         */
        override fun onBindViewHolder(model: MediaList) {
            when (binding) {
                is AdapterSeriesAiringBinding -> {
                    AspectImageView.setImage(binding.seriesImage, model.media.coverImage)
                    SeriesStatusWidget.setAiringStatus(binding.seriesStatus, model)
                    AiringTextView.setAiring(binding.seriesAiring, model.media)
                    SeriesYearTypeTextView.htmlText(binding.seriesYearType, model.media)
                    binding.customRatingWidget.setAverageRating(model)
                    binding.seriesTitle.setTitle(model)
                    binding.seriesEpisodes.setModel(model, currentUser)
                }
                is AdapterSeriesAiringCompactBinding -> {
                    AspectImageView.setImage(binding.seriesImage, model.media.coverImage)
                    SeriesStatusWidget.setAiringStatus(binding.seriesStatus, model)
                    AiringTextView.setAiring(binding.seriesAiring, model.media)
                    SeriesYearTypeTextView.htmlText(binding.seriesYearType, model.media)
                    binding.customRatingWidget.setAverageRating(model)
                    binding.seriesTitle.setTitle(model)
                    binding.seriesEpisodes.setModel(model, currentUser)

                    when (presenter.settings.mediaListStyle) {
                        KeyUtil.LIST_VIEW_STYLE_COMPACT_X1 -> {
                            val margin = context.resources.getDimension(R.dimen.series_title_margin)
                            val layout = binding.seriesTitle.layoutParams as? LayoutParams ?: return
                            layout.marginEnd = margin.toInt()
                        }
                        KeyUtil.LIST_VIEW_STYLE_COMPACT_X2 -> {
                            binding.seriesEpisodes.visibility = View.GONE
                            binding.customRatingWidget.visibility = View.GONE
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
         * <br></br>
         *
         * @see Glide
         */
        override fun onViewRecycled() {
            when (binding) {
                is AdapterSeriesAiringBinding -> {
                    Glide.with(context).clear(binding.seriesImage)
                    binding.seriesEpisodes.onViewRecycled()
                    binding.customRatingWidget.onViewRecycled()
                }
                is AdapterSeriesAiringCompactBinding -> {
                    Glide.with(context).clear(binding.seriesImage)
                    binding.seriesEpisodes.onViewRecycled()
                    binding.customRatingWidget.onViewRecycled()
                }
                else -> {}
            }
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }

    }
}
