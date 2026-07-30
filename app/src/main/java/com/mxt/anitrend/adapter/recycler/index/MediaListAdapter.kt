package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout.LayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.AutoIncrementWidget
import com.mxt.anitrend.base.custom.view.widget.AutoIncrementWidgetState
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.databinding.AdapterSeriesAiringBinding
import com.mxt.anitrend.databinding.AdapterSeriesAiringCompactBinding
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.domain.model.matchesFilter
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.KeyUtil

class MediaListAdapter(
    context: Context,
    private val mediaListStyle: Int,
    private val onIncrement: (MediaListItemUiModel) -> Unit,
    private val onOpenMedia: (View, MediaListItemUiModel) -> Unit,
    private val onOpenManage: (MediaListItemUiModel) -> Unit,
    private val onDelete: (MediaListItemUiModel) -> Unit = {},
) : ListAdapter<MediaListItemUiModel, RecyclerView.ViewHolder>(DIFF_CALLBACK), Filterable {

    private val appContext = context.applicationContext
    private var sourceItems: List<MediaListItemUiModel> = emptyList()
    private var currentFilterQuery: String = ""

    fun submitItems(items: List<MediaListItemUiModel>) {
        sourceItems = items
        submitFilteredList()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            when (mediaListStyle) {
                KeyUtil.LIST_VIEW_STYLE_COMPACT_X1,
                KeyUtil.LIST_VIEW_STYLE_COMPACT_X2,
                -> AdapterSeriesAiringCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                else -> AdapterSeriesAiringBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }

        return SeriesListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as? SeriesListViewHolder)?.bind(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? SeriesListViewHolder)?.recycle()
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults().apply {
            val query = constraint?.toString().orEmpty().trim().lowercase()
            values = if (query.isBlank()) {
                sourceItems
            } else {
                sourceItems.filter { item -> item.matchesFilter(query) }
            }
        }

        override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults,
        ) {
            currentFilterQuery = constraint?.toString().orEmpty()
            submitList((results.values as? List<*>)?.filterIsInstance<MediaListItemUiModel>().orEmpty())
        }
    }

    private fun submitFilteredList() {
        filter.filter(currentFilterQuery)
    }

    private inner class SeriesListViewHolder(
        private val binding: ViewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: MediaListItemUiModel) {
            itemView.setOnClickListener { onOpenMedia(itemView, model) }
            itemView.setOnLongClickListener {
                onOpenManage(model)
                true
            }
            itemView.findViewById<View?>(R.id.series_image)?.setOnClickListener { target ->
                onOpenMedia(target, model)
            }
            itemView.findViewById<View?>(R.id.series_image)?.setOnLongClickListener {
                onOpenManage(model)
                true
            }

            when (binding) {
                is AdapterSeriesAiringBinding -> bindExpanded(binding, model)
                is AdapterSeriesAiringCompactBinding -> bindCompact(binding, model)
            }
        }

        fun recycle() {
            when (binding) {
                is AdapterSeriesAiringBinding -> {
                    Glide.with(appContext).clear(binding.seriesImage)
                    binding.seriesEpisodes.setOnIncrementListener(null)
                    binding.seriesEpisodes.onViewRecycled()
                    binding.customRatingWidget.onViewRecycled()
                }
                is AdapterSeriesAiringCompactBinding -> {
                    Glide.with(appContext).clear(binding.seriesImage)
                    binding.seriesEpisodes.setOnIncrementListener(null)
                    binding.seriesEpisodes.onViewRecycled()
                    binding.customRatingWidget.onViewRecycled()
                }
            }
        }
    }

    private fun bindExpanded(
        binding: AdapterSeriesAiringBinding,
        model: MediaListItemUiModel,
    ) {
        val renderModel = model.toRenderModel()
        AspectImageView.setImage(binding.seriesImage, renderModel.media.coverImage)
        SeriesStatusWidget.setAiringStatus(binding.seriesStatus, renderModel)
        AiringTextView.setAiring(binding.seriesAiring, renderModel.media)
        SeriesYearTypeTextView.htmlText(binding.seriesYearType, renderModel.media)
        binding.customRatingWidget.setAverageRating(renderModel)
        binding.seriesTitle.text = model.mediaTitle
        binding.seriesEpisodes.render(model.toAutoIncrementWidgetState())
        binding.seriesEpisodes.setOnIncrementListener {
            onIncrement(model)
        }
    }

    private fun bindCompact(
        binding: AdapterSeriesAiringCompactBinding,
        model: MediaListItemUiModel,
    ) {
        val renderModel = model.toRenderModel()
        AspectImageView.setImage(binding.seriesImage, renderModel.media.coverImage)
        SeriesStatusWidget.setAiringStatus(binding.seriesStatus, renderModel)
        AiringTextView.setAiring(binding.seriesAiring, renderModel.media)
        SeriesYearTypeTextView.htmlText(binding.seriesYearType, renderModel.media)
        binding.customRatingWidget.setAverageRating(renderModel)
        binding.seriesTitle.text = model.mediaTitle
        binding.seriesEpisodes.render(model.toAutoIncrementWidgetState())
        binding.seriesEpisodes.setOnIncrementListener {
            onIncrement(model)
        }

        when (mediaListStyle) {
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X1 -> {
                val margin = appContext.resources.getDimension(R.dimen.series_title_margin)
                val layout = binding.seriesTitle.layoutParams as? LayoutParams ?: return
                layout.marginEnd = margin.toInt()
            }
            KeyUtil.LIST_VIEW_STYLE_COMPACT_X2 -> {
                binding.seriesEpisodes.visibility = View.GONE
                binding.customRatingWidget.visibility = View.GONE
            }
            else -> Unit
        }
    }

    private fun MediaListItemUiModel.toAutoIncrementWidgetState(): AutoIncrementWidgetState {
        val maxProgress = if (mediaType == KeyUtil.ANIME) mediaEpisodes else mediaChapters
        return AutoIncrementWidgetState(
            progress = progress,
            maxProgress = maxProgress,
            isEnabled = canIncrement && !isIncrementPending && !isDeletePending,
            isLoading = isIncrementPending,
            status = mediaStatus,
            mediaType = mediaType,
        )
    }

    private fun MediaListItemUiModel.toRenderModel(): MediaList = MediaList().apply {
        id = this@toRenderModel.id
        mediaId = this@toRenderModel.mediaId
        status = this@toRenderModel.status
        score = this@toRenderModel.score.toFloat()
        progress = this@toRenderModel.progress
        progressVolumes = this@toRenderModel.progressVolumes
        repeat = this@toRenderModel.repeat
        media = MediaBase().apply {
            id = this@toRenderModel.mediaId
            title = MediaTitle(
                romajiRaw = this@toRenderModel.mediaTitle,
                englishRaw = this@toRenderModel.mediaTitleEnglish,
                originalRaw = this@toRenderModel.mediaTitleOriginal,
                userPreferredRaw = this@toRenderModel.mediaTitle,
            )
            coverImage = ImageBase(
                extraLarge = this@toRenderModel.mediaCoverImage,
                large = this@toRenderModel.mediaCoverImage,
                medium = this@toRenderModel.mediaCoverImage,
            )
            type = this@toRenderModel.mediaType
            format = this@toRenderModel.mediaFormat
            status = this@toRenderModel.mediaStatus
            episodes = this@toRenderModel.mediaEpisodes
            chapters = this@toRenderModel.mediaChapters
            volumes = this@toRenderModel.mediaVolumes
            startDate = this@toRenderModel.mediaStartDate?.let { start ->
                FuzzyDate(
                    day = start.day ?: 0,
                    month = start.month ?: 0,
                    year = start.year ?: 0,
                )
            }
            nextAiringEpisode = this@toRenderModel.nextAiringEpisode?.let { airing ->
                AiringSchedule(
                    airingAt = airing.airingAt,
                    timeUntilAiring = airing.timeUntilAiring,
                    episode = airing.episode,
                )
            }
            isFavourite = this@toRenderModel.mediaIsFavourite
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaListItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: MediaListItemUiModel,
                newItem: MediaListItemUiModel,
            ): Boolean = oldItem.id == newItem.id && oldItem.mediaId == newItem.mediaId

            override fun areContentsTheSame(
                oldItem: MediaListItemUiModel,
                newItem: MediaListItemUiModel,
            ): Boolean = oldItem == newItem
        }
    }
}
