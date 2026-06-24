package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterStudioBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.StudioBase

/**
 * Created by max on 2017/12/20.
 */
class StudioAdapter(
    context: Context,
) : RecyclerViewAdapter<StudioBase>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<StudioBase> = StudioViewHolder(
        AdapterStudioBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    inner class StudioViewHolder(
        private val binding: AdapterStudioBinding,
    ) : RecyclerViewHolder<StudioBase>(binding.root) {
        init {
            bindClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: StudioBase) {
            binding.studioName.text = model.name
            binding.studioFavourite.visibility = if (model.isFavourite) View.VISIBLE else View.GONE
        }

        override fun onViewRecycled() = Unit

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
