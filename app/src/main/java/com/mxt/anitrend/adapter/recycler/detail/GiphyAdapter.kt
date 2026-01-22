package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterGiphyBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/12/09.
 */
class GiphyAdapter(context: Context) : RecyclerViewAdapter<Giphy>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<Giphy> {
        return GiphyViewHolder(
            AdapterGiphyBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        )
    }

    override fun getFilter(): Filter? = null

    inner class GiphyViewHolder(private val binding: AdapterGiphyBinding) :
        RecyclerViewHolder<Giphy>(binding.root) {

        init {
            bindClickListeners(R.id.giphy_image)
            bindLongClickListeners(R.id.giphy_image)
        }

        override fun onBindViewHolder(model: Giphy) {
            val giphy = model.images
            val giphyImage = giphy[KeyUtil.GIPHY_PREVIEW]
                ?: giphy[KeyUtil.GIPHY_ORIGINAL_ANIMATED]
            if (giphyImage != null) {
                Glide.with(getContext()).load(giphyImage.url)
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.giphyImage)
            }

        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.giphyImage)
            binding.giphyImage.onViewRecycled()
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(view: View): Boolean {
            return performLongClick(clickListener, data, view)
        }
    }
}
