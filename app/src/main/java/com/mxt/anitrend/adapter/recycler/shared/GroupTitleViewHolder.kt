package com.mxt.anitrend.adapter.recycler.shared

import android.view.View
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem

/**
 * Created by max on 2018/02/18.
 */
class GroupTitleViewHolder(
    private val binding: AdapterEntityGroupBinding,
) : RecyclerViewHolder<RecyclerItem>(binding.root) {
    override fun onBindViewHolder(model: RecyclerItem) {
        val header = model as? RecyclerHeaderItem ?: return
        binding.catalogHeaderTitle.text = header.getTitle()
        binding.catalogHeaderCount.text = header.size.toString()
        if (header.size < 1) {
            binding.catalogHeaderCount.visibility = View.GONE
        } else {
            binding.catalogHeaderCount.visibility = View.VISIBLE
        }
    }

    override fun onViewRecycled() {
    }

    override fun onClick(v: View) = Unit

    override fun onLongClick(v: View): Boolean = false
}
