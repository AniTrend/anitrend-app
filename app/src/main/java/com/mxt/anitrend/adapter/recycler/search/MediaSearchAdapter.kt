package com.mxt.anitrend.adapter.recycler.search

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterSeriesBinding
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.domain.model.toRenderModel
import com.mxt.anitrend.extension.getLayoutInflater

/**
 * Dedicated [PagingDataAdapter] for the media search screen.
 *
 * Renders immutable [MediaSearchItemUiModel] items into the shared
 * [R.layout.adapter_series] card, forwards click and long-click actions, and
 * uses stable media ids for diffing. Paging positions without a loaded item
 * (placeholders are disabled, but transitions can still expose empty slots)
 * are bound as no-ops, and clicks guard against [RecyclerView.NO_POSITION].
 */
class MediaSearchAdapter(
    context: Context,
    private val onOpenMedia: (View, MediaSearchItemUiModel) -> Unit,
    private val onLongPressMedia: (View, MediaSearchItemUiModel) -> Boolean,
) : PagingDataAdapter<MediaSearchItemUiModel, MediaSearchAdapter.MediaSearchViewHolder>(
    DIFF_CALLBACK,
) {
    private val appContext = context.applicationContext

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MediaSearchViewHolder = MediaSearchViewHolder(
        AdapterSeriesBinding.inflate(
            parent.context.getLayoutInflater(),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(
        holder: MediaSearchViewHolder,
        position: Int,
    ) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

    override fun onViewRecycled(holder: MediaSearchViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    inner class MediaSearchViewHolder(
        private val binding: AdapterSeriesBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { view ->
                val item = currentItem() ?: return@setOnClickListener
                onOpenMedia(view, item)
            }
            binding.root.setOnLongClickListener { view ->
                val item = currentItem() ?: return@setOnLongClickListener false
                onLongPressMedia(view, item)
            }
        }

        fun bind(model: MediaSearchItemUiModel) {
            val renderModel = model.toRenderModel()
            AspectImageView.setImage(binding.seriesImage, model.coverImage)
            SeriesStatusWidget.setStatus(binding.seriesStatus, model.mediaStatus)
            SeriesYearTypeTextView.htmlText(binding.seriesYearType, renderModel)
            binding.customRatingWidget.setAverageRating(renderModel)
            binding.seriesTitle.text = model.title
        }

        fun recycle() {
            Glide.with(appContext).clear(binding.seriesImage)
        }

        private fun currentItem(): MediaSearchItemUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<MediaSearchItemUiModel>() {
                override fun areItemsTheSame(
                    oldItem: MediaSearchItemUiModel,
                    newItem: MediaSearchItemUiModel,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: MediaSearchItemUiModel,
                    newItem: MediaSearchItemUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
