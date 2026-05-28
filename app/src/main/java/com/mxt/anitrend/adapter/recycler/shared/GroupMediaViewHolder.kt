package com.mxt.anitrend.adapter.recycler.shared

import android.view.View
import com.annimon.stream.IntPair
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.AdapterMediaHeaderBinding
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.util.CompatUtil

/**
 * Created by max on 2018/03/26.
 * Group header for media items
 */
class GroupMediaViewHolder(
    private val binding: AdapterMediaHeaderBinding,
    private val clickListener: ItemClickListener<RecyclerItem>
) : RecyclerViewHolder<RecyclerItem>(binding.root) {

    private var boundModel: MediaBase? = null

    init {
        bindClickListeners(R.id.container)
        bindLongClickListeners(R.id.container)
    }

    override fun onBindViewHolder(recyclerItem: RecyclerItem) {
        val model = recyclerItem as? MediaBase ?: return
        boundModel = model
        AspectImageView.setImage(binding.seriesImage, model.coverImage)
        SeriesStatusWidget.setStatus(binding.seriesStatus, model)
        binding.seriesTitle.setTitle(model)
        val subGroupTitle = model.subGroupTitle
        binding.seriesSubgroupTitle.text =
            if (subGroupTitle.isNullOrBlank()) "" else CompatUtil.capitalizeWords(subGroupTitle)
        val format = model.format
        binding.seriesFormat.text =
            if (format.isNullOrBlank()) "" else CompatUtil.capitalizeWords(format)
    }

    override fun onViewRecycled() {
        Glide.with(getContext()).clear(binding.seriesImage)
        boundModel = null
    }

    override fun onClick(v: View) {
        val pair = isValidIndexPair()
        val model = boundModel
        if (model != null && isClickable(model) && pair.second) {
            clickListener.onItemClick(v, IntPair(pair.first, model))
        }
    }

    override fun onLongClick(v: View): Boolean {
        val pair = isValidIndexPair()
        val model = boundModel
        if (model != null && isLongClickable(model) && pair.second) {
            clickListener.onItemLongClick(v, IntPair(pair.first, model))
            return true
        }
        return false
    }
}
