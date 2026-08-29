package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toUserBase
import com.mxt.anitrend.databinding.FragmentMediaBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaDetailRecord
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.navigation.extension.navigateToCharacter
import com.mxt.anitrend.navigation.extension.navigateToComment
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToMediaBrowse
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.extension.navigateToStaff
import com.mxt.anitrend.navigation.extension.navigateToStudio
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.view.sheet.BottomReviewReader
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.view.fragment.youtube.YouTubeEmbedFragment
import com.mxt.anitrend.viewmodel.MediaCharacterViewModel
import com.mxt.anitrend.viewmodel.MediaFeedViewModel
import com.mxt.anitrend.viewmodel.MediaOverviewViewModel
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import com.mxt.anitrend.viewmodel.MediaRelationViewModel
import com.mxt.anitrend.viewmodel.MediaStaffViewModel
import com.mxt.anitrend.viewmodel.MediaStatsViewModel
import com.mxt.anitrend.viewmodel.MediaViewModel
import com.mxt.anitrend.viewmodel.ReviewViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Unified media destination. The former eight pager pages are local sections of this
 * Fragment. Section controllers render ordinary views and never own application navigation.
 */
class MediaFragment : Fragment() {
    companion object {
        private const val KEY_SECTION = "media.section"

        @VisibleForTesting
        internal enum class PrimaryLoadPresentation { LOADING, CONTENT, ERROR }

        @VisibleForTesting
        internal fun primaryLoadPresentation(state: MediaViewModel.UiState): PrimaryLoadPresentation = when (state) {
            MediaViewModel.UiState.Loading -> PrimaryLoadPresentation.LOADING
            is MediaViewModel.UiState.Success -> PrimaryLoadPresentation.CONTENT
            is MediaViewModel.UiState.Error -> PrimaryLoadPresentation.ERROR
        }

        /**
         * Canonical order of section views inside [FragmentMediaBinding.mediaSectionContainer].
         * View creation and section selection both resolve through this single list, so the
         * container child order can never drift from the section selector. Kept in sync with
         * [MediaSection.entries] and enforced by [MediaFragmentSectionOrderTest].
         */
        @VisibleForTesting
        internal val sectionViewOrder: List<MediaSection> = listOf(
            MediaSection.OVERVIEW,
            MediaSection.RELATIONS,
            MediaSection.RECOMMENDATIONS,
            MediaSection.STATS,
            MediaSection.CHARACTERS,
            MediaSection.STAFF,
            MediaSection.FEED,
            MediaSection.REVIEWS,
        )

        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(
            typed: MediaScreenParam?,
            legacyId: Long,
            legacyType: String?,
        ): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0L) return param
            }
            return legacyId.takeIf { it > 0L }?.let { MediaScreenParam(it, legacyType) }
        }
    }

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()
    private val mediaViewModel: MediaViewModel by viewModel()
    private val overviewViewModel: MediaOverviewViewModel by viewModel()
    private val statsViewModel: MediaStatsViewModel by viewModel()
    private val relationViewModel: MediaRelationViewModel by viewModel()
    private val staffViewModel: MediaStaffViewModel by viewModel()
    private val characterViewModel: MediaCharacterViewModel by viewModel()
    private val recommendationsViewModel: MediaRecommendationsViewModel by viewModel()
    private val feedViewModel: MediaFeedViewModel by viewModel()
    private val reviewViewModel: ReviewViewModel by viewModel()

    private var param: MediaScreenParam? = null
    private var mediaType: String? = null
    private var model: MediaDetailRecord? = null
    private var selectedSection = MediaSection.OVERVIEW
    private var favouriteWidget: FavouriteToolbarWidget? = null
    private var malMenuItem: MenuItem? = null
    private var mediaActionUtil: MediaActionUtil? = null

    private lateinit var overviewSection: MediaOverviewSection
    private lateinit var statsSection: MediaStatsSection
    private lateinit var relationSection: MediaRelationSection
    private lateinit var staffSection: MediaStaffSection
    private lateinit var characterSection: MediaCharacterSection
    private lateinit var recommendationsSection: MediaRecommendationsSection
    private lateinit var feedSection: MediaFeedSection
    private lateinit var reviewSection: MediaReviewSection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        param = fromBundle(arguments)
        mediaType = param?.mediaType
        selectedSection = MediaSection.fromOrdinal(
            savedInstanceState?.getInt(KEY_SECTION, -1) ?: -1,
            settings.isAuthenticated,
        )
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMediaBinding.inflate(inflater, container, false)
        val current = param ?: return binding.root
        createSections(current)
        sectionViewOrder.forEach { section ->
            addSectionView(createSectionView(section, inflater))
        }
        buildSectionSelector()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        overviewSection.start(viewLifecycleOwner)
        statsSection.start(viewLifecycleOwner)
        relationSection.start(viewLifecycleOwner)
        staffSection.start(viewLifecycleOwner)
        characterSection.start(viewLifecycleOwner)
        recommendationsSection.start(viewLifecycleOwner)
        feedSection.start(viewLifecycleOwner)
        reviewSection.start(viewLifecycleOwner)
        observeMedia()
        selectSection(selectedSection)
        loadMedia()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SECTION, selectedSection.ordinal)
        super.onSaveInstanceState(outState)
    }

    private fun createSections(current: MediaScreenParam) {
        val context = requireContext()
        val mediaId = current.mediaId
        val type = current.mediaType
        val adult = settings.displayAdultContent
        overviewSection = MediaOverviewSection(
            viewModel = overviewViewModel,
            mediaId = mediaId,
            mediaType = type,
            onOpenGenre = { value -> openBrowse(KeyUtil.arg_genres, value) },
            onOpenTag = { value -> openBrowse(KeyUtil.arg_tags, value) },
            onOpenStudio = { id -> navigateToStudio(StudioScreenParam(id)) },
            onOpenTrailer = ::openTrailer,
        )
        statsSection = MediaStatsSection(
            mediaStatsViewModel = statsViewModel,
            mediaId = mediaId,
            mediaType = type,
            isAdultContent = adult,
            onOpenRank = ::openRank,
            onOpenExternalLink = { url -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
            onCopyExternalLink = { showCopiedMessage() },
        )
        relationSection = MediaRelationSection(
            context = context,
            viewModel = relationViewModel,
            mediaId = mediaId,
            mediaType = type,
            isAdultContent = adult,
            onOpenMedia = { _, media -> navigateToMedia(MediaScreenParam(media.id, media.type)) },
            onLongPressMedia = ::longPressMedia,
        )
        staffSection = MediaStaffSection(
            context = context,
            viewModel = staffViewModel,
            mediaId = mediaId,
            mediaType = type,
            isAdultContent = adult,
            onOpenStaff = { _, staff -> navigateToStaff(StaffScreenParam(staff.id)) },
        )
        characterSection = MediaCharacterSection(
            context = context,
            viewModel = characterViewModel,
            mediaId = mediaId,
            mediaType = type,
            isAdultContent = adult,
            onOpenCharacter = { _, character -> navigateToCharacter(CharacterScreenParam(character.id)) },
        )
        recommendationsSection = MediaRecommendationsSection(
            context = context,
            viewModel = recommendationsViewModel,
            mediaId = mediaId,
            mediaType = type,
            isAdultContent = adult,
            onOpenMedia = { _, item -> navigateToMedia(MediaScreenParam(item.mediaId, item.mediaType)) },
            onLongPressMedia = { _, item ->
                longPressMedia(item.mediaId)
                true
            },
        )
        feedSection = MediaFeedSection(
            context = context,
            viewModel = feedViewModel,
            mediaId = mediaId,
            isFollowing = true,
            pageLimit = KeyUtil.PAGING_LIMIT,
            currentUserId = databaseHelper.currentUser?.id,
            onToggleLike = feedViewModel::toggleLike,
            onDeleteFeed = feedViewModel::deleteFeed,
            onOpenMedia = { _, item -> item.mediaId?.let { navigateToMedia(MediaScreenParam(it, item.mediaType)) } },
            onOpenComments = { id -> navigateToComment(com.mxt.anitrend.navigation.model.CommentScreenParam(id)) },
            onEditFeed = ::editFeed,
            onShowLikes = ::showLikes,
            onOpenProfile = { _, id -> navigateToProfile(UserScreenParam(id)) },
            onLongPressMedia = { _, item ->
                item.mediaId?.let {
                    longPressMedia(it)
                    true
                } ?: false
            },
        )
        reviewSection = MediaReviewSection(
            context = context,
            databaseHelper = databaseHelper,
            viewModel = reviewViewModel,
            mediaId = mediaId,
            mediaType = type,
            onReviewClick = ::openReview,
            onReviewLongClick = ::longPressReview,
        )
    }

    private fun createSectionView(section: MediaSection, inflater: LayoutInflater): View = when (section) {
        MediaSection.OVERVIEW -> overviewSection.inflate(inflater, binding.mediaSectionContainer)
        MediaSection.RELATIONS -> relationSection.createView(inflater, binding.mediaSectionContainer)
        MediaSection.RECOMMENDATIONS -> recommendationsSection.createView(inflater, binding.mediaSectionContainer)
        MediaSection.STATS -> statsSection.inflate(inflater, binding.mediaSectionContainer)
        MediaSection.CHARACTERS -> characterSection.createView(inflater, binding.mediaSectionContainer)
        MediaSection.STAFF -> staffSection.createView(inflater, binding.mediaSectionContainer)
        MediaSection.FEED -> feedSection.createView(inflater, binding.mediaSectionContainer)
        MediaSection.REVIEWS -> reviewSection.createView(inflater, binding.mediaSectionContainer)
    }

    private fun addSectionView(view: View) {
        view.isVisible = false
        binding.mediaSectionContainer.addView(view)
    }

    private fun buildSectionSelector() {
        binding.mediaSectionSelector.removeAllViews()
        MediaSection.visibleSections(settings.isAuthenticated).forEach { section ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(section.titleRes)
                isCheckable = true
                setOnClickListener { selectSection(section) }
            }
            binding.mediaSectionSelector.addView(chip)
        }
        selectSection(selectedSection)
    }

    private fun selectSection(section: MediaSection) {
        val visible = MediaSection.visibleSections(settings.isAuthenticated)
        if (section !in visible) return selectSection(visible.first())
        selectedSection = section
        val index = sectionViewOrder.indexOf(section)
        for (i in 0 until binding.mediaSectionContainer.childCount) {
            binding.mediaSectionContainer.getChildAt(i).isVisible = i == index
        }
        binding.mediaSectionSelector.getChildAt(visible.indexOf(section))?.let { chip ->
            binding.mediaSectionSelector.check(chip.id)
        }
        when (section) {
            MediaSection.OVERVIEW -> overviewSection.select()
            MediaSection.RELATIONS -> relationSection.select()
            MediaSection.RECOMMENDATIONS -> recommendationsSection.select()
            MediaSection.STATS -> statsSection.select()
            MediaSection.CHARACTERS -> characterSection.select()
            MediaSection.STAFF -> staffSection.select()
            MediaSection.FEED -> feedSection.select()
            MediaSection.REVIEWS -> reviewSection.select()
        }
    }

    private fun observeMedia() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mediaViewModel.state.collect { state ->
                        when (primaryLoadPresentation(state)) {
                            PrimaryLoadPresentation.LOADING -> binding.mediaStateLayout.showLoading()
                            PrimaryLoadPresentation.CONTENT -> {
                                val success = state as MediaViewModel.UiState.Success
                                model = success.media
                                mediaType = success.media.type
                                (activity as? AppCompatActivity)?.supportActionBar?.title = success.media.titleUserPreferred
                                com.mxt.anitrend.base.custom.view.image.WideImageView.setImage(binding.mediaBanner, success.media.bannerImage)
                                activity?.invalidateOptionsMenu()
                                updateMenu()
                                binding.mediaStateLayout.showContent()
                            }
                            PrimaryLoadPresentation.ERROR -> {
                                val error = state as MediaViewModel.UiState.Error
                                binding.mediaStateLayout.showError(
                                    drawable = requireContext().getCompatDrawable(R.drawable.ic_emoji_cry),
                                    message = error.message,
                                    actionText = getString(R.string.try_again),
                                    action = View.OnClickListener {
                                        binding.mediaStateLayout.showLoading()
                                        loadMedia()
                                    },
                                )
                            }
                        }
                    }
                }
                launch {
                    combine(mediaViewModel.favouriteFlag, mediaViewModel.favouriteLoading) { flag, loading ->
                        FavouriteWidgetRenderState.fromFlag(flag, model?.isFavourite ?: false, loading)
                    }.collect { favouriteWidget?.render(it) }
                }
            }
        }
    }

    private fun loadMedia() {
        param?.let { mediaViewModel.load(it.mediaId, it.mediaType, settings.displayAdultContent) }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.media_base_menu, menu)
        val authenticated = settings.isAuthenticated
        menu.findItem(R.id.action_favourite).isVisible = authenticated
        menu.findItem(R.id.action_manage).isVisible = authenticated
        malMenuItem = menu.findItem(R.id.action_mal)
        favouriteWidget = menu.findItem(R.id.action_favourite).actionView as? FavouriteToolbarWidget
        favouriteWidget?.setOnToggleAction {
            param?.let { mediaViewModel.toggleFavouriteMedia(it.mediaId, mediaType) }
        }
        updateMenu()
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return true
        }
        val current = model ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_manage -> {
                mediaActionUtil = MediaActionUtil.Builder().setId(current.id).build(requireActivity())
                mediaActionUtil?.startSeriesAction()
                return true
            }
            R.id.action_share -> {
                val share = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), "%s - %s", current.titleUserPreferred ?: "", current.siteUrl))
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(share, getString(R.string.abc_shareactionprovider_share_with)))
                return true
            }
            R.id.action_mal -> {
                mediaType?.let { type ->
                    val url = "https://myanimelist.net/${type.lowercase(Locale.getDefault())}/${current.idMal}"
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(url)), getString(R.string.abc_shareactionprovider_share_with)))
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateMenu() {
        model?.let { current ->
            malMenuItem?.isVisible = (current.idMal ?: 0) > 0
            favouriteWidget?.render(FavouriteWidgetRenderState.fromFlag(mediaViewModel.favouriteFlag.value, current.isFavourite, mediaViewModel.favouriteLoading.value))
        }
    }

    private fun openBrowse(key: String, value: String) {
        val args = Bundle().apply {
            putString(KeyUtil.arg_mediaType, mediaType)
            putStringArrayList(key, arrayListOf(value))
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            if (!settings.displayAdultContent) putBoolean(KeyUtil.arg_isAdult, false)
            putString(KeyUtil.arg_activity_tag, value)
            putParcelable(KeyUtil.arg_media_util, MediaBrowseUtil().setCompactType(true).setBasicFilter(true).setFilterEnabled(true))
        }
        findNavController().navigateToMediaBrowse(args)
    }

    private fun openRank(rank: MediaRank, media: Media) {
        val args = Bundle().apply {
            putString(KeyUtil.arg_mediaType, mediaType)
            putString(KeyUtil.arg_format, rank.format)
            putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
            if (!settings.displayAdultContent) putBoolean(KeyUtil.arg_isAdult, false)
            putString(KeyUtil.arg_activity_tag, rank.typeHtmlPlainTitle)
            putParcelable(KeyUtil.arg_media_util, MediaBrowseUtil().setCompactType(true).setFilterEnabled(false))
        }
        findNavController().navigateToMediaBrowse(args)
    }

    private fun openTrailer(param: TrailerScreenParam) {
        // This is an embedded media preview, not an application navigation stack. The
        // section itself remains an ordinary view controller; only the preview owns a
        // child Fragment because YouTubeEmbedFragment is a platform-backed player surface.
        childFragmentManager.beginTransaction()
            .replace(R.id.youtube_view, YouTubeEmbedFragment.newInstance(param))
            .commit()
    }

    private fun longPressMedia(media: MediaBase) = longPressMedia(media.id)

    private fun longPressMedia(mediaId: Long) {
        if (!settings.isAuthenticated) {
            NotifyUtil.makeText(requireContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show()
            return
        }
        mediaActionUtil = MediaActionUtil.Builder().setId(mediaId).build(requireActivity())
        mediaActionUtil?.startSeriesAction()
    }

    private fun openReview(target: View, review: ReviewRecord) {
        when (target.id) {
            R.id.series_image -> review.media?.let { navigateToMedia(MediaScreenParam(it.id, it.type)) }
            R.id.user_avatar -> review.user?.id?.let { navigateToProfile(UserScreenParam(it)) }
            R.id.review_read_more -> BottomReviewReader.Builder()
                .setReview(review)
                .setOnUserClick(::navigateToProfile)
                .setTitle(R.string.drawer_title_reviews)
                .build()
                .show(parentFragmentManager, "review_reader")
        }
    }

    private fun longPressReview(target: View, review: ReviewRecord) {
        if (target.id == R.id.series_image) review.media?.id?.let(::longPressMedia)
    }

    private fun editFeed(item: FeedItemUiModel) {
        BottomSheetComposer.Builder().setUserActivity(item).setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED).setTitle(R.string.edit_status_title).build().show(parentFragmentManager, "feed_editor")
    }

    private fun showLikes(item: FeedItemUiModel) {
        val likes = item.likes.orEmpty().map { it.toUserBase() }
        if (likes.isEmpty()) {
            NotifyUtil.makeText(requireContext(), R.string.text_no_likes, Toast.LENGTH_SHORT).show()
        } else {
            BottomSheetUsers.Builder()
                .setModel(likes)
                .setOnUserClick(::navigateToProfile)
                .setTitle(R.string.title_bottom_sheet_likes)
                .build()
                .show(parentFragmentManager, "feed_likes")
        }
    }

    private fun showCopiedMessage() {
        NotifyUtil.makeText(requireContext(), R.string.text_url_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        favouriteWidget?.setOnToggleAction(null)
        favouriteWidget = null
        mediaActionUtil?.onDestroy()
        mediaActionUtil = null
        overviewSection.clear()
        statsSection.clear()
        relationSection.destroyView()
        staffSection.destroyView()
        characterSection.destroyView()
        recommendationsSection.destroyView()
        feedSection.destroyView()
        reviewSection.destroyView()
        _binding = null
        super.onDestroyView()
    }
}
