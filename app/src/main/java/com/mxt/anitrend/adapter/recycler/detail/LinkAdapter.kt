package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterLinkBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.ExternalLink

/**
 * Created by max on 2018/01/02.
 */
class LinkAdapter(context: Context) : RecyclerViewAdapter<ExternalLink>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<ExternalLink> {
        return LinkViewHolder(
            AdapterLinkBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        )
    }

    override fun getFilter(): Filter? = null

    inner class LinkViewHolder(private val binding: AdapterLinkBinding) :
        RecyclerViewHolder<ExternalLink>(binding.root) {

        init {
            bindClickListeners(R.id.container)
            bindLongClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: ExternalLink) {
            binding.linkText.text = model.site
        }

        override fun onViewRecycled() {
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(view: View): Boolean {
            return performLongClick(clickListener, data, view)
        }
    }
}
