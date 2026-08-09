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

    /**
     * View holder for one media search card.
     *
     * Binds an immutable [MediaSearchItemUiModel] into the shared series card
     * and forwards click and long-click actions to the adapter callbacks when
     * the holder has a current item.
     */
    inner class MediaSearchViewHolder(
        private val binding: AdapterSeriesBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener(::handleRootClick)
            binding.root.setOnLongClickListener(::handleRootLongClick)
        }

        /** Binds [model] into the card: cover image, status, year/type line, rating and title. */
        fun bind(model: MediaSearchItemUiModel) {
            val renderModel = model.toRenderModel()
            AspectImageView.setImage(binding.seriesImage, model.coverImage)
            SeriesStatusWidget.setStatus(binding.seriesStatus, model.mediaStatus)
            SeriesYearTypeTextView.htmlText(binding.seriesYearType, renderModel)
            binding.customRatingWidget.setAverageRating(renderModel)
            binding.seriesTitle.text = model.title
        }

        /** Releases the card's image resources so a recycled card never shows stale art. */
        fun recycle() {
            Glide.with(appContext).clear(binding.seriesImage)
        }

        private fun handleRootClick(view: View) {
            val item = currentItem() ?: return
            onOpenMedia(view, item)
        }

        private fun handleRootLongClick(view: View): Boolean {
            val item = currentItem() ?: return false
            return onLongPressMedia(view, item)
        }

        private fun currentItem(): MediaSearchItemUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }
    }

    /**
     * Shared adapter configuration: the [DiffUtil.ItemCallback] that diffs
     * submitted media items.
     */
    companion object {
        /** [DiffUtil.ItemCallback] diffing on stable media ids, with full equality for content. */
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
