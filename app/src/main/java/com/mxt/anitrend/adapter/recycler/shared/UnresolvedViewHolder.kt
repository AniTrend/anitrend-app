package com.mxt.anitrend.adapter.recycler.shared

import android.view.View
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding

class UnresolvedViewHolder<T>(
    binding: CustomRecyclerUnresolvedBinding
) : RecyclerViewHolder<T>(binding.root) {

    override fun onBindViewHolder(model: T) = Unit

    override fun onViewRecycled() = Unit

    override fun onClick(v: View) = Unit

    override fun onLongClick(v: View): Boolean = false
}
