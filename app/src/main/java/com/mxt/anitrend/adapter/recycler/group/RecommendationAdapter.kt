package com.mxt.anitrend.adapter.recycler.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterSeriesBinding
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.domain.model.toRenderModel
import com.mxt.anitrend.extension.getLayoutInflater

/**
 * Dedicated [ListAdapter] for the media recommendations screen.
 *
 * Renders immutable [RecommendationItemUiModel] items into the shared
 * [R.layout.adapter_series] card, forwards click and long-click actions, and
 * uses stable recommendation ids for diffing.
 */
class RecommendationAdapter(
    context: Context,
    private val onOpenMedia: (View, RecommendationItemUiModel) -> Unit,
    private val onLongPressMedia: (View, RecommendationItemUiModel) -> Boolean,
) : ListAdapter<RecommendationItemUiModel, RecommendationAdapter.RecommendationViewHolder>(
    DIFF_CALLBACK,
) {
    private val appContext = context.applicationContext

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecommendationViewHolder = RecommendationViewHolder(
        AdapterSeriesBinding.inflate(
            parent.context.getLayoutInflater(),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(
        holder: RecommendationViewHolder,
        position: Int,
    ) = holder.bind(getItem(position))

    override fun onViewRecycled(holder: RecommendationViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    inner class RecommendationViewHolder(
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

        fun bind(model: RecommendationItemUiModel) {
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

        private fun currentItem(): RecommendationItemUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<RecommendationItemUiModel>() {
                override fun areItemsTheSame(
                    oldItem: RecommendationItemUiModel,
                    newItem: RecommendationItemUiModel,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: RecommendationItemUiModel,
                    newItem: RecommendationItemUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
