package com.mxt.anitrend.adapter.recycler.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.databinding.AdapterSeriesBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/12/31.
 */
class GroupSeriesAdapter(context: Context) : RecyclerViewAdapter<RecyclerItem>(context) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        @KeyUtil.RecyclerViewType viewType: Int
    ): RecyclerViewHolder<RecyclerItem> {
        return if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
            GroupTitleViewHolder(
                AdapterEntityGroupBinding.inflate(parent.context.getLayoutInflater(), parent, false)
            )
        } else {
            SeriesViewHolder(
                AdapterSeriesBinding.inflate(parent.context.getLayoutInflater(), parent, false)
            )
        }
    }

    @KeyUtil.RecyclerViewType
    override fun getItemViewType(position: Int): Int {
        return data[position].contentType
    }

    override fun getFilter(): Filter? = null

    inner class SeriesViewHolder(private val binding: AdapterSeriesBinding) :
        RecyclerViewHolder<RecyclerItem>(binding.root) {

        init {
            bindClickListeners(R.id.container)
            bindLongClickListeners(R.id.container)
        }

        override fun onBindViewHolder(recyclerItem: RecyclerItem) {
            val model = recyclerItem as? MediaBase ?: return
            AspectImageView.setImage(binding.seriesImage, model.coverImage)
            SeriesStatusWidget.setStatus(binding.seriesStatus, model)
            SeriesYearTypeTextView.htmlText(binding.seriesYearType, model)
            binding.customRatingWidget.setAverageRating(model)
            binding.seriesTitle.setTitle(model)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.seriesImage)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }
    }
}
