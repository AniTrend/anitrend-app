package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterFeedSlideBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.markdown.RegexUtil
import java.util.Locale

/**
 * Created by max on 2017/11/25.
 * image preview adapter
 */
class ImagePreviewAdapter(
    private val contentTypes: List<String>,
    context: Context
) : RecyclerViewAdapter<String>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<String> {
        return PreviewHolder(
            AdapterFeedSlideBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        )
    }

    override fun getFilter(): Filter? = null

    inner class PreviewHolder(protected val binding: AdapterFeedSlideBinding) :
        RecyclerViewHolder<String>(binding.root) {

        init {
            binding.feedStatusImage.setOnClickListener(this)
        }

        override fun onBindViewHolder(model: String) {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return
            }

            val targetModel: String
            var isCenterCrop = false
            when (contentTypes[position].lowercase(Locale.getDefault())) {
                RegexUtil.KEY_IMG -> {
                    targetModel = model
                    ViewCompat.setTransitionName(binding.feedStatusImage, model)
                    binding.feedPlayBack.visibility = View.GONE
                }
                RegexUtil.KEY_YOU -> {
                    targetModel = RegexUtil.getYoutubeThumb(model)
                    binding.feedPlayBack.visibility = View.VISIBLE
                    isCenterCrop = true
                }
                else -> {
                    targetModel = RegexUtil.NO_THUMBNAIL
                    binding.feedPlayBack.visibility = View.VISIBLE
                    isCenterCrop = true
                }
            }

            val roundedCorners = RoundedCorners(
                this@ImagePreviewAdapter.context.resources.getDimensionPixelSize(R.dimen.md_margin)
            )
            val centerTransform = if (isCenterCrop) CenterCrop() else CenterInside()
            Glide.with(getContext()).load(targetModel)
                .transition(DrawableTransitionOptions.withCrossFade(250))
                .transform(centerTransform, roundedCorners)
                .into(binding.feedStatusImage)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.feedStatusImage)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }
    }
}
