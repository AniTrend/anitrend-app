package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.text.SpoilerTagTextView
import com.mxt.anitrend.databinding.AdapterTagBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.MediaTag

/**
 * Created by max on 2018/01/01.
 */
class TagAdapter(context: Context) : RecyclerViewAdapter<MediaTag>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<MediaTag> {
        return TagViewHolder(
            AdapterTagBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        )
    }

    override fun getFilter(): Filter? = null

    inner class TagViewHolder(private val binding: AdapterTagBinding) :
        RecyclerViewHolder<MediaTag>(binding.root) {

        init {
            bindClickListeners(R.id.container)
            bindLongClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: MediaTag) {
            binding.tagName.text = model.name
            SpoilerTagTextView.setIsSpoiler(binding.tagName, model.isMediaSpoiler)
            binding.tagRank.text = String.format("%d%%", model.rank)
        }

        override fun onViewRecycled() = Unit

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }
    }
}
