package com.mxt.anitrend.adapter.recycler.shared

import android.view.View
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.CustomRecyclerLoadingFooterBinding

/**
 * Created by max on 2018/03/25.
 * LoadingFooterViewHolder
 */
class RecyclerStateFooterViewHolder<T>(
    binding: CustomRecyclerLoadingFooterBinding,
) : RecyclerViewHolder<T>(binding.root) {
    override fun onBindViewHolder(model: T) = Unit

    override fun onViewRecycled() = Unit

    override fun onClick(v: View) = Unit

    override fun onLongClick(v: View): Boolean = false
}
