package com.mxt.anitrend.view.fragment.detail

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.GenreAdapter
import com.mxt.anitrend.adapter.recycler.detail.TagAdapter
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.RangeDateTextView
import com.mxt.anitrend.base.custom.view.text.SeriesTypeView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.databinding.FragmentSeriesOverviewBinding
import com.mxt.anitrend.databinding.SectionSeriesDescriptionBinding
import com.mxt.anitrend.databinding.SectionSeriesDetailsBinding
import com.mxt.anitrend.databinding.SectionSeriesInfoBinding
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewTagRecord
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.viewmodel.MediaOverviewViewModel
import kotlinx.coroutines.launch

/**
 * View-only controller for the media overview section.
 *
 * The parent destination owns navigation and section selection. This controller
 * owns only overview rendering, its list adapters, and its ViewModel collection.
 * The callback-heavy constructor keeps the destination's existing overview
 * actions at this section boundary.
 */
@Suppress("LongParameterList")
class MediaOverviewSection(
    private val viewModel: MediaOverviewViewModel,
    private val mediaId: Long,
    private val mediaType: String?,
    private val onOpenGenre: (String) -> Unit,
    private val onOpenTag: (String) -> Unit,
    private val onOpenStudio: (Long) -> Unit,
    private val onOpenTrailer: (TrailerScreenParam) -> Unit,
) {
    private var binding: FragmentSeriesOverviewBinding? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var overviewRecord: MediaOverviewRecord? = null
    private var tagItems: List<MediaTag> = emptyList()
    private var genreAdapter: GenreAdapter? = null
    private var tagAdapter: TagAdapter? = null
    private var selected = false

    /** Inflates and initializes the media overview view. */
    fun inflate(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentSeriesOverviewBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.genreRecycler.layoutManager = StaggeredGridLayoutManager(
            sectionBinding.root.resources.getInteger(R.integer.grid_list_x2),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.genreRecycler.isNestedScrollingEnabled = false
        sectionBinding.genreRecycler.setHasFixedSize(true)
        sectionBinding.tagsRecycler.layoutManager = StaggeredGridLayoutManager(
            sectionBinding.root.resources.getInteger(R.integer.grid_list_x2),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.tagsRecycler.isNestedScrollingEnabled = false
        sectionBinding.tagsRecycler.setHasFixedSize(true)
        listOf(
            R.id.series_image,
            R.id.anime_main_studio_container,
            R.id.show_spoiler_tags,
        ).mapNotNull(sectionBinding.root::findViewById).forEach { it.setOnClickListener(::onClick) }
        sectionBinding.stateLayout.showLoading()
        return sectionBinding.root
    }

    /** Starts collecting overview state for [owner]. */
    fun start(owner: LifecycleOwner) {
        lifecycleOwner = owner
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is MediaOverviewViewModel.UiState.Loading -> binding?.stateLayout?.showLoading()
                        is MediaOverviewViewModel.UiState.Success -> {
                            overviewRecord = state.record
                            viewModel.displayData.value?.let { updateUi(state.record, it) }
                        }
                        is MediaOverviewViewModel.UiState.Error -> binding?.let { current ->
                            current.stateLayout.showError(
                                current.root.context.getCompatDrawable(R.drawable.ic_emoji_sweat),
                                state.message,
                                current.root.context.getString(R.string.try_again),
                            ) { viewModel.load(mediaId, mediaType) }
                        }
                    }
                }
            }
        }
    }

    /** Loads the overview the first time this section is selected. */
    fun select() {
        if (selected) return
        selected = true
        viewModel.load(mediaId, mediaType)
    }

    /** Releases the overview binding and cached rendering state. */
    fun clear() {
        binding = null
        lifecycleOwner = null
        overviewRecord = null
        tagItems = emptyList()
        genreAdapter = null
        tagAdapter = null
        selected = false
    }

    private fun updateUi(
        record: MediaOverviewRecord,
        displayData: MediaOverviewViewModel.MediaOverviewDisplayData,
    ) {
        val sectionBinding = binding ?: return
        val trailer = record.trailer?.let { MediaTrailer(id = it.id, site = it.site) }
        if (trailer != null && CompatUtil.equals(trailer.site, "youtube")) {
            onOpenTrailer(TrailerScreenParam(trailerId = trailer.id.orEmpty(), site = trailer.site.orEmpty()))
        } else {
            sectionBinding.youtubeView.visibility = View.GONE
        }
        bindSeriesInfo(sectionBinding.seriesInfoSection, record, displayData)
        bindSeriesDescription(sectionBinding.seriesDescriptionSection, record)
        bindSeriesDetails(sectionBinding.seriesDetailsSection, record, displayData)
        sectionBinding.genreRecycler.visibility = if (displayData.genres.isNotEmpty()) View.VISIBLE else View.GONE
        sectionBinding.tagsRecycler.visibility = if (!record.tags.isNullOrEmpty()) View.VISIBLE else View.GONE
        sectionBinding.showSpoilerTags.visibility = View.GONE

        if (genreAdapter == null) {
            genreAdapter = GenreAdapter(sectionBinding.root.context).apply {
                onItemsInserted(displayData.genres)
                setClickListener(object : ItemClickListener<Genre> {
                    override fun onItemClick(target: View, data: IndexedValue<Genre>) {
                        if (target.id == R.id.container) data.value.genre?.let(onOpenGenre)
                    }

                    override fun onItemLongClick(target: View, data: IndexedValue<Genre>) = Unit
                })
            }
        }
        sectionBinding.genreRecycler.adapter = genreAdapter

        tagItems = record.tags.orEmpty().map { it.toMediaTag() }
        if (tagAdapter == null) {
            tagAdapter = TagAdapter(sectionBinding.root.context).apply {
                onItemsInserted(tagItems)
                setClickListener(object : ItemClickListener<MediaTag> {
                    override fun onItemClick(target: View, data: IndexedValue<MediaTag>) {
                        if (target.id == R.id.container) {
                            DialogUtil.createTagMessage(
                                sectionBinding.root.context,
                                data.value.name.orEmpty(),
                                data.value.description.orEmpty(),
                                data.value.isMediaSpoiler,
                                R.string.More,
                                R.string.Close,
                            ) { _, _ -> onOpenTag(data.value.name.orEmpty()) }
                        }
                    }

                    override fun onItemLongClick(target: View, data: IndexedValue<MediaTag>) = Unit
                })
            }
        }
        sectionBinding.tagsRecycler.adapter = tagAdapter
        sectionBinding.stateLayout.showContent()
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.series_image -> CompatUtil.imagePreview(
                view,
                overviewRecord?.coverImage?.extraLarge,
                R.string.image_preview_error_series_cover,
            )
            R.id.anime_main_studio_container -> overviewRecord?.studios?.firstOrNull()?.id?.let(onOpenStudio)
            R.id.show_spoiler_tags -> {
                if (tagItems.isNotEmpty()) {
                    tagAdapter?.onItemRangeChanged(tagItems)
                    tagAdapter?.notifyDataSetChanged()
                    view.visibility = View.GONE
                }
            }
        }
    }

    private fun bindSeriesInfo(
        sectionBinding: SectionSeriesInfoBinding,
        record: MediaOverviewRecord,
        displayData: MediaOverviewViewModel.MediaOverviewDisplayData,
    ) {
        sectionBinding.seriesTitleRomaji.text = record.titleRomaji.orEmpty()
        sectionBinding.seriesTitleEnglish.text = record.titleEnglish.orEmpty()
        SeriesTypeView.setSeriesType(sectionBinding.seriesType, displayData.formatText ?: sectionBinding.root.context.getString(R.string.tba_placeholder))
        SeriesStatusWidget.setStatus(sectionBinding.seriesStatus, record.status)
        AspectImageView.setImage(sectionBinding.seriesImage, record.coverImage?.extraLarge ?: record.coverImage?.large)
    }

    private fun bindSeriesDescription(sectionBinding: SectionSeriesDescriptionBinding, record: MediaOverviewRecord) {
        RangeDateTextView.setStartDate(sectionBinding.seriesStartDate, record.startDate)
        RangeDateTextView.setEndDate(sectionBinding.seriesEndDate, record.endDate)
        sectionBinding.seriesTitleOriginal.text = record.titleOriginal.orEmpty()
        sectionBinding.seriesDescriptionText.htmlText(record.description)
    }

    private fun bindSeriesDetails(
        sectionBinding: SectionSeriesDetailsBinding,
        record: MediaOverviewRecord,
        displayData: MediaOverviewViewModel.MediaOverviewDisplayData,
    ) {
        val mangaVisibility = if (displayData.isManga) View.VISIBLE else View.GONE
        val animeVisibility = if (displayData.isAnime) View.VISIBLE else View.GONE
        sectionBinding.mangaSectionSpace.visibility = mangaVisibility
        sectionBinding.mangaSection.visibility = mangaVisibility
        sectionBinding.animeSectionSpace.visibility = animeVisibility
        sectionBinding.animeSection.visibility = animeVisibility
        sectionBinding.animeDetailsSpace.visibility = animeVisibility
        sectionBinding.animeDetailsSection.visibility = animeVisibility
        sectionBinding.seriesSeasonValue.text = displayData.seasonText ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.seriesOriginValue.text = displayData.sourceText ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.totalChaptersValue.text = displayData.chapterCount?.let { sectionBinding.root.context.getString(R.string.text_manga_chapters, it) } ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.totalVolumesValue.text = displayData.volumeCount?.let { sectionBinding.root.context.getString(R.string.text_manga_volumes, it) } ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.totalEpisodesValue.text = displayData.episodeCount?.let { sectionBinding.root.context.getString(R.string.text_anime_episodes, it) } ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.episodeDurationValue.text = displayData.episodeDuration?.let { sectionBinding.root.context.getString(R.string.text_anime_length, it) } ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.hashTagValue.text = displayData.hashTagHtml?.let { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY) } ?: Html.fromHtml(sectionBinding.root.context.getString(R.string.TBA), Html.FROM_HTML_MODE_LEGACY)
        sectionBinding.seriesScoreValue.text = displayData.meanScore?.let { sectionBinding.root.context.getString(R.string.text_anime_score, it) } ?: sectionBinding.root.context.getString(R.string.tba_placeholder)
        sectionBinding.mainStudioValue.text = displayData.mainStudioName ?: sectionBinding.root.context.getString(R.string.TBA)
        sectionBinding.seriesStatusValue.text = displayData.statusText ?: sectionBinding.root.context.getString(R.string.TBA)
        AiringTextView.setAiring(sectionBinding.nextEpisodeValue, record.nextAiringEpisode, record.status)
    }

    private fun MediaOverviewTagRecord.toMediaTag(): MediaTag = MediaTag(
        name = name,
        description = description,
        category = category,
        rank = rank,
        isMediaSpoiler = false,
        isAdult = isAdult,
    )
}
