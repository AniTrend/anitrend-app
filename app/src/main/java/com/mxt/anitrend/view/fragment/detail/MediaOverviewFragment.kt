package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.GenreAdapter
import com.mxt.anitrend.adapter.recycler.detail.TagAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBase
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
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.view.activity.detail.MediaBrowseActivity
import com.mxt.anitrend.view.activity.detail.StudioActivity
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment

/**
 * Created by max on 2017/12/31.
 */

class MediaOverviewFragment : FragmentBase<Media, MediaPresenter, Media>() {

    private var binding: FragmentSeriesOverviewBinding? = null
    private var model: Media? = null

    private var genreAdapter: GenreAdapter? = null
    private var tagAdapter: TagAdapter? = null

    private var mediaId: Long = 0

    @KeyUtil.MediaType
    private var mediaType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        if (arguments != null) {
            mediaId = arguments?.getLong(KeyUtil.arg_id) ?: 0
            mediaType = arguments?.getString(KeyUtil.arg_mediaType)
        }
        isMenuDisabled = true
        mColumnSize = R.integer.grid_list_x2
        setPresenter(MediaPresenter(ctx))
        setViewModel(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSeriesOverviewBinding.inflate(inflater, container, false).apply {
            stateLayout.showLoading()

            genreRecycler.layoutManager = StaggeredGridLayoutManager(resources.getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL)
            genreRecycler.isNestedScrollingEnabled = false
            genreRecycler.setHasFixedSize(true)

            tagsRecycler.layoutManager = StaggeredGridLayoutManager(resources.getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL)
            tagsRecycler.isNestedScrollingEnabled = false
            tagsRecycler.setHasFixedSize(true)

            listOf(
                R.id.series_image,
                R.id.anime_main_studio_container,
                R.id.show_spoiler_tags,
            ).map {
                root.findViewById<View>(it)
            }.forEach { it?.setOnClickListener(this@MediaOverviewFragment) }
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        makeRequest()
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    override fun updateUI() {
        val ctx = context ?: return
        val trailer = model?.trailer
        if (activity != null && trailer != null && CompatUtil.equals(trailer.site, "youtube")) {
            childFragmentManager.beginTransaction()
                .replace(R.id.youtube_view, YouTubeEmbedFragment.newInstance(trailer))
                .commit()
        } else {
            binding?.youtubeView?.visibility = View.GONE
        }

        binding?.seriesInfoSection?.let { bindSeriesInfo(it, model) }
        binding?.seriesDescriptionSection?.let { bindSeriesDescription(it, model) }
        binding?.seriesDetailsSection?.let { bindSeriesDetails(it, model) }

        binding?.genreRecycler?.visibility =
            if (!CompatUtil.isEmpty(model?.genres)) View.VISIBLE else View.GONE
        binding?.tagsRecycler?.visibility =
            if (!CompatUtil.isEmpty(model?.tags)) View.VISIBLE else View.GONE

        if (model?.tags != null && model?.tagsNoSpoilers != null) {
            if (model?.tagsNoSpoilers?.size == model?.tags?.size) {
                binding?.showSpoilerTags?.visibility = View.GONE
            } else {
                binding?.showSpoilerTags?.visibility = View.VISIBLE
            }
        }

        if (genreAdapter == null) {
            genreAdapter = GenreAdapter(ctx)
            genreAdapter?.onItemsInserted(presenter.buildGenres(model))
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
                            if (!presenter.settings.displayAdultContent) {
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
        binding?.genreRecycler?.adapter = genreAdapter
        model?.tagsNoSpoilers?.also {
            if (tagAdapter == null) {
                tagAdapter = TagAdapter(ctx)
                tagAdapter?.onItemsInserted(it)
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
                                    if (!presenter.settings.displayAdultContent) {
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
            binding?.tagsRecycler?.adapter = tagAdapter
        }

        binding?.stateLayout?.showContent()
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    override fun makeRequest() {
        val model = viewModel ?: return
        val ctx = context ?: return
        model.params.apply {
            putLong(KeyUtil.arg_id, mediaId)
            putString(KeyUtil.arg_mediaType, mediaType)
            putBoolean(KeyUtil.arg_asHtml, false)
            if (presenter.settings.displayAdultContent) {
                remove(KeyUtil.arg_isAdult)
            } else {
                putBoolean(KeyUtil.arg_isAdult, false)
            }
        }
        model.requestData(KeyUtil.MEDIA_OVERVIEW_REQ, ctx)
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    override fun onChanged(value: Media?) {
        if (value != null) {
            this.model = value
            updateUI()
        } else {
            binding?.stateLayout?.showError(
                context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
                getString(R.string.layout_empty_response),
                getString(R.string.try_again),
            ) { _ ->
                binding?.stateLayout?.showLoading()
                makeRequest()
            }
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        val intent: Intent
        when (v.id) {
            R.id.series_image -> CompatUtil.imagePreview(v, model?.coverImage?.extraLarge, R.string.image_preview_error_series_cover)
            R.id.anime_main_studio_container -> {
                val studioBase = presenter.getMainStudioObject(model)
                if (studioBase != null) {
                    val host = activity ?: return
                    val intent = StudioActivity.newIntent(host, studioBase.id)
                    startActivity(intent)
                }
            }
            R.id.show_spoiler_tags -> {
                model?.tags?.also {
                    tagAdapter?.onItemRangeChanged(it)
                    tagAdapter?.notifyDataSetChanged()
                    v.visibility = View.GONE
                }
            }
            else -> super.onClick(v)
        }
    }

    private fun bindSeriesInfo(sectionBinding: SectionSeriesInfoBinding, media: Media?) {
        val title = media?.title
        sectionBinding.seriesTitleRomaji.text = title?.romaji.orEmpty()
        sectionBinding.seriesTitleEnglish.text = title?.english.orEmpty()
        SeriesTypeView.setSeriesType(sectionBinding.seriesType, presenter.getMediaFormat(media))
        SeriesStatusWidget.setStatus(sectionBinding.seriesStatus, media)
        AspectImageView.setImage(sectionBinding.seriesImage, media?.coverImage)
    }

    private fun bindSeriesDescription(sectionBinding: SectionSeriesDescriptionBinding, media: Media?) {
        RangeDateTextView.setStartDate(sectionBinding.seriesStartDate, media?.startDate)
        RangeDateTextView.setEndDate(sectionBinding.seriesEndDate, media?.endDate)
        sectionBinding.seriesTitleOriginal.text = media?.title?.original.orEmpty()
        sectionBinding.seriesDescriptionText.htmlText(media?.description)
    }

    private fun bindSeriesDetails(sectionBinding: SectionSeriesDetailsBinding, media: Media?) {
        val mangaVisibility = presenter.isManga(media)
        val animeVisibility = presenter.isAnime(media)

        sectionBinding.mangaSectionSpace.visibility = mangaVisibility
        sectionBinding.mangaSection.visibility = mangaVisibility
        sectionBinding.animeSectionSpace.visibility = animeVisibility
        sectionBinding.animeSection.visibility = animeVisibility
        sectionBinding.animeDetailsSpace.visibility = animeVisibility
        sectionBinding.animeDetailsSection.visibility = animeVisibility

        sectionBinding.seriesSeasonValue.text = presenter.getMediaSeason(media)
        sectionBinding.seriesOriginValue.text = presenter.getMediaSource(media)
        sectionBinding.totalChaptersValue.text = presenter.getChapterCount(media)
        sectionBinding.totalVolumesValue.text = presenter.getVolumeCount(media)
        sectionBinding.totalEpisodesValue.text = presenter.getEpisodeCount(media)
        sectionBinding.episodeDurationValue.text = presenter.getEpisodeDuration(media)
        sectionBinding.hashTagValue.text = presenter.getHashTag(media)
        sectionBinding.seriesScoreValue.text = presenter.getMediaScore(media)
        sectionBinding.mainStudioValue.text = presenter.getMainStudio(media)
        sectionBinding.seriesStatusValue.text = presenter.getMediaStatus(media)
        AiringTextView.setAiring(sectionBinding.nextEpisodeValue, media)
    }

    companion object {

        fun newInstance(args: Bundle): MediaOverviewFragment {
            val fragment = MediaOverviewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
