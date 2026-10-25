package com.mxt.anitrend.view.fragment.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.navigation.extension.navigateToCharacter
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToStaff
import com.mxt.anitrend.navigation.extension.navigateToStudio
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.favourite.FavouriteGridHost
import com.mxt.anitrend.ui.favourite.FavouriteGridSection
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.ui.media.StudioRowUiState
import com.mxt.anitrend.ui.media.appendMediaSeriesPage
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import com.mxt.anitrend.ui.staff.StaffRowUiState
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.CharacterFavouritesViewModel
import com.mxt.anitrend.viewmodel.MediaFavouritesViewModel
import com.mxt.anitrend.viewmodel.StaffFavouritesViewModel
import com.mxt.anitrend.viewmodel.StudioFavouritesViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Single Navigation 2 destination for a user's favourite sections. */
@Suppress("TooManyFunctions") // Lifecycle, navigation, and favourite section rendering stay centralized.
class FavouriteFragment : FragmentBaseList<MediaBase, Any>() {
    private enum class Section(
        val title: Int,
    ) {
        ANIME(R.string.favorites_page_title_anime),
        CHARACTERS(R.string.favorites_page_title_characters),
        MANGA(R.string.favorites_page_title_manga),
        STAFF(R.string.favorites_page_title_staff),
        STUDIOS(R.string.favorites_page_title_studios),
    }

    private var listBinding: FragmentListBinding? = null
    private lateinit var sectionSelector: ChipGroup
    private var section by mutableStateOf(Section.ANIME)
    private var userId = 0L
    private var composeView: ComposeView? = null

    // Compose-grid paging state for the character, staff, and studios sections.
    private var composePage = 1
    private var composeHasMore = true
    private var composeLoading = false
    private val characterItems = mutableStateOf(emptyList<CharacterRowUiState>(), referentialEqualityPolicy())
    private val staffItems = mutableStateOf(emptyList<StaffRowUiState>(), referentialEqualityPolicy())
    private val studioItems = mutableStateOf(emptyList<StudioRowUiState>(), referentialEqualityPolicy())
    private val animeGridState = LazyGridState()
    private val mangaGridState = LazyGridState()
    private val characterGridState = LazyGridState()
    private val staffGridState = LazyGridState()
    private val studioGridState = LazyGridState()
    private val mediaItems = mutableStateOf(emptyList<MediaSeriesCardUiState>(), referentialEqualityPolicy())
    private val mediaAppendGate = ManualGridAppendGate()
    private var mediaLoading = false
    private var lastMediaState: MediaFavouritesViewModel.UiState? = null
    private var lastCharacterState: CharacterFavouritesViewModel.UiState? = null
    private var lastStaffState: StaffFavouritesViewModel.UiState? = null
    private var lastStudioState: StudioFavouritesViewModel.UiState? = null

    private val settings: Settings by inject()
    private val mediaViewModel: MediaFavouritesViewModel by viewModel()
    private val characterViewModel: CharacterFavouritesViewModel by viewModel()
    private val staffViewModel: StaffFavouritesViewModel by viewModel()
    private val studioViewModel: StudioFavouritesViewModel by viewModel()

    /** Argument and saved-state helpers for the favourites destination. */
    companion object {
        private const val STATE_SECTION = "favourite_section"

        /** Reads the user identity from typed or legacy arguments. */
        fun fromBundle(bundle: Bundle?): UserScreenParam? = bundle?.screenParam<UserScreenParam>() ?: resolveLegacyUser(
            legacyId = bundle?.getLong(KeyUtil.arg_id, 0L) ?: 0L,
            legacyName = bundle?.getString(KeyUtil.arg_userName),
        )

        @VisibleForTesting
        internal fun resolveLegacyUser(legacyId: Long, legacyName: String?): UserScreenParam? = UserScreenParam(legacyId, legacyName).takeIf { legacyId > 0L || !legacyName.isNullOrBlank() }

        @VisibleForTesting
        internal fun resolveSection(value: String?): String = value ?: Section.ANIME.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromBundle(arguments)?.let { param ->
            userId = param.userId
        }
        section = savedInstanceState?.getString(STATE_SECTION)?.let { value ->
            runCatching { Section.valueOf(value) }.getOrDefault(Section.ANIME)
        } ?: Section.ANIME
        isPager = true
        selectSection(section, reload = false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_favourite, container, false)
        sectionSelector = root.findViewById(R.id.favourite_section_selector)
        val listContainer = root.findViewById<ViewGroup>(R.id.favourite_list_container)
        listBinding = FragmentListBinding.inflate(inflater, listContainer, true)
        val binding = requireNotNull(listBinding)
        swipeRefreshLayout = binding.refreshLayout
        stateLayout = binding.stateLayout
        composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme {
                    FavouriteGridHost(
                        section = section.toGridSection(),
                        mediaItems = mediaItems.value,
                        characterItems = characterItems.value,
                        staffItems = staffItems.value,
                        studioItems = studioItems.value,
                        gridState = gridStateFor(section),
                        mediaColumnsResource = columnSizeFor(section),
                        onMediaClick = ::openMedia,
                        onMediaLongClick = ::openMediaActions,
                        onCharacterClick = { navigateToCharacter(CharacterScreenParam(it)) },
                        onStaffClick = { navigateToStaff(StaffScreenParam(it)) },
                        onStudioClick = { navigateToStudio(StudioScreenParam(it)) },
                        onLoadMore = ::onGridLoadMore,
                    )
                }
            }
        }
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, it) }
        setSwipeRefreshLayoutEnabled(section == Section.ANIME || section == Section.MANGA)
        swapRefreshTarget(requireNotNull(composeView), gridStateFor(section))
        buildSectionSelector()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mediaViewModel.state.collect { state ->
                        // Non-media tabs share this ViewModel; a late media result
                        // must not repaint the characters/staff/studios surface.
                        val onMediaTab = section == Section.ANIME || section == Section.MANGA
                        if (onMediaTab && state.mediaType == activeMediaType() && state !== lastMediaState) {
                            lastMediaState = state
                            renderMedia(state)
                        }
                    }
                }
                launch {
                    characterViewModel.state.collect { state ->
                        if (section == Section.CHARACTERS && state !== lastCharacterState) {
                            lastCharacterState = state
                            renderCharacter(state)
                        }
                    }
                }
                launch {
                    staffViewModel.state.collect { state ->
                        if (section == Section.STAFF && state !== lastStaffState) {
                            lastStaffState = state
                            renderStaff(state)
                        }
                    }
                }
                launch {
                    studioViewModel.state.collect { state ->
                        if (section == Section.STUDIOS && state !== lastStudioState) {
                            lastStudioState = state
                            renderStudio(state)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (hasActiveItems()) {
            updateUI()
        } else {
            onRefresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SECTION, section.name)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        swipeRefreshLayout.scrollBoundary = null
        composeView = null
        listBinding = null
        super.onDestroyView()
    }

    override fun onRefresh() {
        if (section.isComposeGrid()) resetComposePaging()
        super.onRefresh()
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(section == Section.ANIME || section == Section.MANGA)
        showContent()
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false)
        } else if (swipeRefreshLayout.isLoading()) {
            swipeRefreshLayout.setLoading(false)
        }
    }

    override fun makeRequest() {
        when (section) {
            Section.ANIME, Section.MANGA -> {
                mediaLoading = true
                mediaAppendGate.onRequestStarted()
                mediaViewModel.load(userId, pageState.currentPage, activeMediaType())
            }
            Section.CHARACTERS -> characterViewModel.load(userId, composePage)
            Section.STAFF -> staffViewModel.load(userId, composePage)
            Section.STUDIOS -> studioViewModel.load(userId, composePage)
        }
        if (section.isComposeGrid()) composeLoading = true
    }

    override fun onChanged(value: Any?) = Unit

    private fun buildSectionSelector() {
        Section.values().forEach { value ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(value.title)
                isCheckable = true
                setOnClickListener { selectSection(value) }
            }
            sectionSelector.addView(chip)
        }
        sectionSelector.check(sectionSelector.getChildAt(section.ordinal).id)
    }

    private fun selectSection(value: Section, reload: Boolean = true) {
        section = value
        if (::sectionSelector.isInitialized) {
            sectionSelector.getChildAt(value.ordinal)?.let { sectionSelector.check(it.id) }
        }
        if (listBinding != null) {
            setSwipeRefreshLayoutEnabled(value == Section.ANIME || value == Section.MANGA)
            composeView?.let { swapRefreshTarget(it, gridStateFor(value)) }
        }
        if (reload && view != null) {
            pageState.onRefreshPage()
            clearComposeItems()
            resetComposePaging()
            mediaLoading = false
            showLoading()
            makeRequest()
        }
    }

    private fun Section.isComposeGrid(): Boolean = this == Section.CHARACTERS || this == Section.STAFF || this == Section.STUDIOS

    private fun Section.toGridSection(): FavouriteGridSection = when (this) {
        Section.ANIME -> FavouriteGridSection.ANIME
        Section.MANGA -> FavouriteGridSection.MANGA
        Section.CHARACTERS -> FavouriteGridSection.CHARACTERS
        Section.STAFF -> FavouriteGridSection.STAFF
        Section.STUDIOS -> FavouriteGridSection.STUDIOS
    }

    private fun gridStateFor(value: Section): LazyGridState = when (value) {
        Section.ANIME -> animeGridState
        Section.MANGA -> mangaGridState
        Section.CHARACTERS -> characterGridState
        Section.STAFF -> staffGridState
        Section.STUDIOS -> studioGridState
    }

    private fun onGridLoadMore() {
        if (section.isComposeGrid()) onComposeLoadMore() else onMediaLoadMore()
    }

    private fun onComposeLoadMore() {
        if (composeLoading || !composeHasMore) return
        composeLoading = true
        composePage++
        makeRequest()
    }

    private fun onMediaLoadMore() {
        if (
            mediaLoading ||
            !mediaAppendGate.canRequestNextPage ||
            pageState.getPageInfo()?.hasNextPage() == false
        ) {
            return
        }
        mediaLoading = true
        pageState.currentPage++
        pageState.currentOffset += KeyUtil.PAGING_LIMIT
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }

    private fun resetComposePaging() {
        composePage = 1
        composeHasMore = true
        composeLoading = false
    }

    private fun clearComposeItems() {
        mediaItems.value = emptyList()
        characterItems.value = emptyList()
        staffItems.value = emptyList()
        studioItems.value = emptyList()
    }

    private fun hasActiveItems(): Boolean = when (section) {
        Section.ANIME, Section.MANGA -> mediaItems.value.isNotEmpty()
        Section.CHARACTERS -> characterItems.value.isNotEmpty()
        Section.STAFF -> staffItems.value.isNotEmpty()
        Section.STUDIOS -> studioItems.value.isNotEmpty()
    }

    private fun commitComposePage(newCount: Int, totalCount: Int, hasNextPage: Boolean) {
        composeLoading = false
        composeHasMore = hasNextPage
        if (newCount == 0) {
            composeHasMore = false
            if (totalCount == 0) {
                showEmpty(getString(R.string.layout_empty_response))
            } else {
                updateUI()
            }
        } else {
            updateUI()
        }
    }

    private fun swapRefreshTarget(host: View, gridState: LazyGridState) {
        if (host.parent !== swipeRefreshLayout) {
            (host.parent as? ViewGroup)?.removeView(host)
            swipeRefreshLayout.addView(
                host,
                0,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
        }
        swipeRefreshLayout.scrollBoundary = { direction ->
            if (direction < 0) gridState.canScrollBackward else gridState.canScrollForward
        }
    }

    private fun <T> appendUniqueById(
        existing: List<T>,
        incoming: List<T>,
        id: (T) -> Long,
    ): List<T> {
        val ids = HashSet<Long>(existing.size + incoming.size)
        val result = ArrayList<T>(existing.size + incoming.size)
        existing.forEach { item ->
            ids.add(id(item))
            result.add(item)
        }
        incoming.forEach { item ->
            if (ids.add(id(item))) result.add(item)
        }
        return result
    }

    private fun activeMediaType(): String = if (section == Section.MANGA) KeyUtil.MANGA else KeyUtil.ANIME

    private fun columnSizeFor(value: Section): Int = if (value == Section.STUDIOS) R.integer.grid_list_x2 else R.integer.grid_giphy_x3

    private fun renderMediaPage(content: List<MediaBase>?) {
        mediaLoading = false
        val mapped = content.orEmpty().map { MediaSeriesCardUiState.from(requireContext(), it) }
        if (mapped.isEmpty()) {
            if (pageState.currentPage <= 1) {
                mediaItems.value = emptyList()
            }
            mediaAppendGate.onRequestSucceeded(itemsInPage = 0)
            setLimitReached()
            showEmpty(getString(R.string.layout_empty_response))
            return
        }
        val previous = if (pageState.currentPage <= 1) emptyList() else mediaItems.value
        val nextItems = appendMediaSeriesPage(previous, mapped)
        mediaItems.value = nextItems
        mediaAppendGate.onRequestSucceeded(itemsInPage = nextItems.size - previous.size)
        updateUI()
    }

    private fun openMedia(item: MediaSeriesCardUiState) {
        navigateToMedia(MediaScreenParam(item.id, item.renderModel.mediaType))
    }

    private fun openMediaActions(item: MediaSeriesCardUiState) {
        openMediaActions(item.id)
    }

    private fun openMediaActions(item: MediaBase) {
        openMediaActions(item.id)
    }

    private fun openMediaActions(mediaId: Long) {
        if (settings.isAuthenticated) {
            mediaActionUtil = MediaActionUtil.Builder().setId(mediaId).build(requireActivity())
            mediaActionUtil.startSeriesAction()
        } else {
            NotifyUtil.makeText(
                requireContext(),
                R.string.info_login_req,
                R.drawable.ic_group_add_grey_600_18dp,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun renderMedia(state: MediaFavouritesViewModel.UiState) {
        when (state) {
            is MediaFavouritesViewModel.UiState.Loading -> {
                mediaLoading = true
                showLoading()
            }
            is MediaFavouritesViewModel.UiState.Success -> {
                mediaLoading = false
                val container = if (state.mediaType == KeyUtil.ANIME) state.content.connection.anime else state.content.connection.manga
                container?.pageInfo?.let(pageState::setPageInfo)
                renderMediaPage(container?.pageData)
            }
            is MediaFavouritesViewModel.UiState.Error -> {
                mediaLoading = false
                mediaAppendGate.onRequestFailed()
                showError(state.message)
            }
        }
    }

    private fun renderCharacter(state: CharacterFavouritesViewModel.UiState) {
        when (state) {
            is CharacterFavouritesViewModel.UiState.Loading -> showLoading()
            is CharacterFavouritesViewModel.UiState.Success -> {
                val page = state.content.connection.characters
                page?.pageInfo?.let(pageState::setPageInfo)
                val mapped = page?.pageData.orEmpty().map { CharacterRowUiState.from(it) }
                val merged = if (composePage <= 1) mapped else appendUniqueById(characterItems.value, mapped) { it.id }
                characterItems.value = merged
                commitComposePage(mapped.size, merged.size, page?.pageInfo?.hasNextPage() != false)
            }
            is CharacterFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderStaff(state: StaffFavouritesViewModel.UiState) {
        when (state) {
            is StaffFavouritesViewModel.UiState.Loading -> showLoading()
            is StaffFavouritesViewModel.UiState.Success -> {
                val page = state.content.connection.staff
                page?.pageInfo?.let(pageState::setPageInfo)
                val mapped = page?.pageData.orEmpty().map { StaffRowUiState.from(it) }
                val merged = if (composePage <= 1) mapped else appendUniqueById(staffItems.value, mapped) { it.id }
                staffItems.value = merged
                commitComposePage(mapped.size, merged.size, page?.pageInfo?.hasNextPage() != false)
            }
            is StaffFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderStudio(state: StudioFavouritesViewModel.UiState) {
        when (state) {
            is StudioFavouritesViewModel.UiState.Loading -> showLoading()
            is StudioFavouritesViewModel.UiState.Success -> {
                val page = state.content.connection.studios
                page?.pageInfo?.let(pageState::setPageInfo)
                val mapped = page?.pageData.orEmpty().map { StudioRowUiState.from(it) }
                val merged = if (composePage <= 1) mapped else appendUniqueById(studioItems.value, mapped) { it.id }
                studioItems.value = merged
                commitComposePage(mapped.size, merged.size, page?.pageInfo?.hasNextPage() != false)
            }
            is StudioFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }
}
