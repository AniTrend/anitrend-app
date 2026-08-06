package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity
import com.mxt.anitrend.view.activity.detail.StudioActivity
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment
import com.mxt.anitrend.viewmodel.MediaOverviewViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.annotation.VisibleForTesting

/**
 * Created by max on 2017/12/31.
 */

class MediaOverviewFragment : Fragment() {

    private var _binding: FragmentSeriesOverviewBinding? = null
    private val binding get() = _binding!!

    private var overviewRecord: MediaOverviewRecord? = null

    private var tagItems: List<MediaTag> = emptyList()

    private var genreAdapter: GenreAdapter? = null
    private var tagAdapter: TagAdapter? = null

    private var mediaId: Long = 0

    @KeyUtil.MediaType
    private var mediaType: String? = null

    private val settings: Settings by inject()

    private val mediaOverviewViewModel: MediaOverviewViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Resolve the destination through the typed media parameter, falling back to
        // the legacy wire keys forwarded by the pager/activity for pre-bridge callers.
        fromBundle(arguments)?.let { args ->
            mediaId = args.mediaId
            mediaType = args.mediaType
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeriesOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.genreRecycler.layoutManager = StaggeredGridLayoutManager(
            resources.getInteger(R.integer.grid_list_x2),
            StaggeredGridLayoutManager.VERTICAL,
        )
        binding.genreRecycler.isNestedScrollingEnabled = false
        binding.genreRecycler.setHasFixedSize(true)

        binding.tagsRecycler.layoutManager = StaggeredGridLayoutManager(
            resources.getInteger(R.integer.grid_list_x2),
            StaggeredGridLayoutManager.VERTICAL,
        )
        binding.tagsRecycler.isNestedScrollingEnabled = false
        binding.tagsRecycler.setHasFixedSize(true)

        listOf(
            R.id.series_image,
            R.id.anime_main_studio_container,
            R.id.show_spoiler_tags,
        ).map {
            binding.root.findViewById<View>(it)
        }.forEach { it?.setOnClickListener(::onClick) }

        binding.stateLayout.showLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaOverviewViewModel.state.collect { state ->
                    when (state) {
                        is MediaOverviewViewModel.UiState.Loading -> {
                            binding.stateLayout.showLoading()
                        }
                        is MediaOverviewViewModel.UiState.Success -> {
                            overviewRecord = state.record
                            val data = mediaOverviewViewModel.displayData.value
                            if (data != null) {
                                updateUI(state.record, data)
                            }
                        }
                        is MediaOverviewViewModel.UiState.Error -> {
                            binding.stateLayout.showError(
                                requireContext().getCompatDrawable(R.drawable.ic_emoji_sweat),
                                state.message,
                                getString(R.string.try_again),
                            ) { loadOverview() }
                        }
                    }
                }
            }
        }

        loadOverview()
    }

    private fun loadOverview() {
        binding.stateLayout.showLoading()
        mediaOverviewViewModel.load(mediaId = mediaId, mediaType = mediaType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(record: MediaOverviewRecord, displayData: MediaOverviewViewModel.MediaOverviewDisplayData) {
        val ctx = requireContext()
        val trailer = record.trailer?.let { MediaTrailer(id = it.id, site = it.site) }
        if (activity != null && trailer != null && CompatUtil.equals(trailer.site, "youtube")) {
            // Identity-only navigation: the trailer entity is never parceled; only the
            // stable trailer id and site travel in TrailerScreenParam.
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.youtube_view,
                    YouTubeEmbedFragment.newInstance(
                        TrailerScreenParam(trailerId = trailer.id.orEmpty(), site = trailer.site.orEmpty()),
                    ),
                )
                .commit()
        } else {
            binding.youtubeView.visibility = View.GONE
        }

        binding.seriesInfoSection.let { bindSeriesInfo(it, record, displayData) }
        binding.seriesDescriptionSection.let { bindSeriesDescription(it, record) }
        binding.seriesDetailsSection.let { bindSeriesDetails(it, record, displayData) }

        binding.genreRecycler.visibility =
            if (displayData.genres.isNotEmpty()) View.VISIBLE else View.GONE
        binding.tagsRecycler.visibility =
            if (!record.tags.isNullOrEmpty()) View.VISIBLE else View.GONE

        if (record.tags != null) {
            // The legacy overview lane never populated isMediaSpoiler, so all tags
            // are shown up front and the reveal toggle stays hidden.
            binding.showSpoilerTags.visibility = View.GONE
        }

        if (genreAdapter == null) {
            genreAdapter = GenreAdapter(ctx)
            genreAdapter?.onItemsInserted(displayData.genres)
            genreAdapter?.setClickListener(object : ItemClickListener<Genre> {
                override fun onItemClick(target: View, data: IndexedValue<Genre>) {
                    when (target.id) {
                        R.id.container -> {
                            val host = activity ?: return
                            val args = Bundle()
                            val intent = Intent(host, MediaBrowseActivity::class.java)
                            args.putString(KeyUtil.arg_mediaType, mediaType)
                            args.putStringArrayList(KeyUtil.arg_genres, arrayListOf(data.value.genre))
                            args.putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                            if (!settings.displayAdultContent) {
                                args.putBoolean(KeyUtil.arg_isAdult, false)
                            }
                            args.putString(KeyUtil.arg_activity_tag, data.value.genre)
                            args.putParcelable(
                                KeyUtil.arg_media_util,
                                MediaBrowseUtil()
                                    .setCompactType(true)
                                    .setBasicFilter(true)
                                    .setFilterEnabled(true),
                            )
                            intent.putExtras(args)
                            startActivity(intent)
                        }
                    }
                }

                override fun onItemLongClick(target: View, data: IndexedValue<Genre>) {
                }
            })
        }
        binding.genreRecycler.adapter = genreAdapter
        tagItems = record.tags.orEmpty().map { it.toMediaTag() }
        if (tagAdapter == null) {
            tagAdapter = TagAdapter(ctx)
            tagAdapter?.onItemsInserted(tagItems)
            tagAdapter?.setClickListener(object : ItemClickListener<MediaTag> {
                override fun onItemClick(target: View, data: IndexedValue<MediaTag>) {
                    when (target.id) {
                        R.id.container -> activity?.let { host ->
                            DialogUtil.createTagMessage(
                                host,
                                data.value.name.orEmpty(),
                                data.value.description.orEmpty(),
                                data.value.isMediaSpoiler,
                                R.string.More,
                                R.string.Close,
                            ) { _, _ ->
                                val args = Bundle()
                                val intent = Intent(host, MediaBrowseActivity::class.java)
                                args.putString(KeyUtil.arg_mediaType, mediaType)
                                args.putStringArrayList(
                                    KeyUtil.arg_tags,
                                    arrayListOf(data.value.name.orEmpty()),
                                )
                                args.putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                                if (!settings.displayAdultContent) {
                                    args.putBoolean(KeyUtil.arg_isAdult, false)
                                }
                                args.putString(KeyUtil.arg_activity_tag, data.value.name.orEmpty())
                                args.putParcelable(
                                    KeyUtil.arg_media_util,
                                    MediaBrowseUtil()
                                        .setCompactType(true)
                                        .setBasicFilter(true)
                                        .setFilterEnabled(true),
                                )
                                intent.putExtras(args)
                                startActivity(intent)
                            }
                        }
                    }
                }

                override fun onItemLongClick(target: View, data: IndexedValue<MediaTag>) {
                }
            })
        }
        binding.tagsRecycler.adapter = tagAdapter

        binding.stateLayout.showContent()
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.series_image -> CompatUtil.imagePreview(
                v,
                overviewRecord?.coverImage?.extraLarge,
                R.string.image_preview_error_series_cover,
            )
            R.id.anime_main_studio_container -> {
                val studioId = overviewRecord?.studios?.firstOrNull()?.id
                if (studioId != null) {
                    val host = activity ?: return
                    val intent = StudioActivity.newIntent(host, studioId)
                    startActivity(intent)
                }
            }
            R.id.show_spoiler_tags -> {
                if (tagItems.isNotEmpty()) {
                    tagAdapter?.onItemRangeChanged(tagItems)
                    tagAdapter?.notifyDataSetChanged()
                    v.visibility = View.GONE
                }
            }
        }
    }

    private fun bindSeriesInfo(
        sectionBinding: SectionSeriesInfoBinding,
        record: MediaOverviewRecord?,
        displayData: MediaOverviewViewModel.MediaOverviewDisplayData,
    ) {
        sectionBinding.seriesTitleRomaji.text = record?.titleRomaji.orEmpty()
        sectionBinding.seriesTitleEnglish.text = record?.titleEnglish.orEmpty()
        SeriesTypeView.setSeriesType(
            sectionBinding.seriesType,
            displayData.formatText ?: getString(R.string.tba_placeholder),
        )
        SeriesStatusWidget.setStatus(sectionBinding.seriesStatus, record?.status)
        AspectImageView.setImage(
            sectionBinding.seriesImage,
            record?.coverImage?.extraLarge ?: record?.coverImage?.large,
        )
    }

    private fun bindSeriesDescription(sectionBinding: SectionSeriesDescriptionBinding, record: MediaOverviewRecord?) {
        RangeDateTextView.setStartDate(sectionBinding.seriesStartDate, record?.startDate)
        RangeDateTextView.setEndDate(sectionBinding.seriesEndDate, record?.endDate)
        sectionBinding.seriesTitleOriginal.text = record?.titleOriginal.orEmpty()
        sectionBinding.seriesDescriptionText.htmlText(record?.description)
    }

    private fun bindSeriesDetails(
        sectionBinding: SectionSeriesDetailsBinding,
        record: MediaOverviewRecord?,
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

        sectionBinding.seriesSeasonValue.text = displayData.seasonText ?: getString(R.string.TBA)
        sectionBinding.seriesOriginValue.text = displayData.sourceText ?: getString(R.string.TBA)
        sectionBinding.totalChaptersValue.text = displayData.chapterCount?.let {
            getString(R.string.text_manga_chapters, it)
        } ?: getString(R.string.TBA)
        sectionBinding.totalVolumesValue.text = displayData.volumeCount?.let {
            getString(R.string.text_manga_volumes, it)
        } ?: getString(R.string.TBA)
        sectionBinding.totalEpisodesValue.text = displayData.episodeCount?.let {
            getString(R.string.text_anime_episodes, it)
        } ?: getString(R.string.TBA)
        sectionBinding.episodeDurationValue.text = displayData.episodeDuration?.let {
            getString(R.string.text_anime_length, it)
        } ?: getString(R.string.TBA)
        sectionBinding.hashTagValue.text = displayData.hashTagHtml?.let {
            Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
        } ?: Html.fromHtml(getString(R.string.TBA), Html.FROM_HTML_MODE_LEGACY)
        sectionBinding.seriesScoreValue.text = displayData.meanScore?.let {
            getString(R.string.text_anime_score, it)
        } ?: getString(R.string.tba_placeholder)
        sectionBinding.mainStudioValue.text = displayData.mainStudioName ?: getString(R.string.TBA)
        sectionBinding.seriesStatusValue.text = displayData.statusText ?: getString(R.string.TBA)
        AiringTextView.setAiring(sectionBinding.nextEpisodeValue, record?.nextAiringEpisode, record?.status)
    }

    private fun MediaOverviewTagRecord.toMediaTag(): MediaTag = MediaTag(
        name = name,
        description = description,
        category = category,
        rank = rank,
        // The legacy overview lane never populated isMediaSpoiler, so the tags
        // keep the exact display behavior of the previous Media-backed path.
        isMediaSpoiler = false,
        isAdult = isAdult,
    )

    companion object {

        fun newInstance(args: Bundle): MediaOverviewFragment {
            val fragment = MediaOverviewFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * Resolves the media identity from the fragment arguments.
         *
         * The typed [MediaScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] / [KeyUtil.arg_mediaType] extras are bridged with
         * their exact raw values (0 or negative ids pass through, mirroring the
         * pre-refactor getter). A typed param present but invalid falls back to the
         * legacy raw values.
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(typed: MediaScreenParam?, legacyId: Long, legacyType: String?): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy values.
            }
            return MediaScreenParam(mediaId = legacyId, mediaType = legacyType)
        }
    }
}
