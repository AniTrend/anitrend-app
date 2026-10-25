package com.mxt.anitrend.view.fragment.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.extension.navigateToCharacter
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.extension.navigateToStaff
import com.mxt.anitrend.navigation.extension.navigateToStudio
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.character.CharacterGrid
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.StudioGrid
import com.mxt.anitrend.ui.media.StudioRowUiState
import com.mxt.anitrend.ui.mediasearch.MediaSearchPagingList
import com.mxt.anitrend.ui.paging.PagingLoadStateRenderer
import com.mxt.anitrend.ui.staff.StaffGrid
import com.mxt.anitrend.ui.staff.StaffRowUiState
import com.mxt.anitrend.ui.user.UserList
import com.mxt.anitrend.ui.user.UserRowUiState
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.CharacterSearchViewModel
import com.mxt.anitrend.viewmodel.MediaSearchViewModel
import com.mxt.anitrend.viewmodel.StaffSearchViewModel
import com.mxt.anitrend.viewmodel.StudioSearchViewModel
import com.mxt.anitrend.viewmodel.UserSearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Single Navigation 2 destination for media, staff, studio, character, and user search. */
@Suppress("TooManyFunctions") // Lifecycle, navigation, and search section rendering stay centralized.
class SearchFragment : FragmentBaseList<Any, Any>() {
    private enum class Section(
        @StringRes val title: Int,
        val mediaType: String? = null,
    ) {
        ANIME(R.string.search_title_anime, KeyUtil.ANIME),
        MANGA(R.string.search_title_manga, KeyUtil.MANGA),
        STUDIO(R.string.search_title_studio),
        STAFF(R.string.search_title_staff),
        CHARACTERS(R.string.search_title_characters),
        USERS(R.string.search_titles_auth_users),
    }

    private var listBinding: FragmentListBinding? = null
    private lateinit var sectionSelector: ChipGroup
    private lateinit var mediaComposeView: ComposeView
    private lateinit var loadStateRenderer: PagingLoadStateRenderer
    private lateinit var userComposeView: ComposeView
    private val mediaRefreshSignal = mutableStateOf(0)

    // The refresh signal is one-shot across ComposeView recreation.
    private var consumedMediaRefreshSignal = 0
    private val mediaPaging = MutableStateFlow<MediaPagingSnapshot?>(null)
    private val mediaListState = LazyGridState()
    private val characterGridState = LazyGridState()
    private val staffGridState = LazyGridState()
    private val studioGridState = LazyGridState()
    private val userListState = LazyGridState()

    /**
     * Load state and item count from one paging snapshot: count-only changes must
     * re-trigger rendering, so both travel through the single observed holder.
     */
    private data class MediaPagingSnapshot(
        val states: CombinedLoadStates,
        val count: Int,
    )

    private var section by mutableStateOf(Section.ANIME)

    // Search identity changes only on submit or tab selection, never on keystrokes.
    private var searchQuery: String? = null
    private var draftQuery = ""
    private var draftQueryChanged = false
    private var queryInput: TextInputEditText? = null
    private lateinit var composeView: ComposeView

    // Compose-grid paging state for the character, staff, and studio sections.
    private var composeHasMore = true
    private var composeLoading = false
    private var lastCharacterSearch: String? = null
    private var lastStaffSearch: String? = null
    private var lastStudioSearch: String? = null
    private var lastUserSearch: String? = null
    private var lastCharacterPage = 0
    private var lastStaffPage = 0
    private var lastStudioPage = 0
    private var lastUserPage = 0
    private val characterItems = mutableStateOf(emptyList<CharacterRowUiState>(), referentialEqualityPolicy())
    private val staffItems = mutableStateOf(emptyList<StaffRowUiState>(), referentialEqualityPolicy())
    private val studioItems = mutableStateOf(emptyList<StudioRowUiState>(), referentialEqualityPolicy())
    private val userItems = mutableStateOf(emptyList<UserRowUiState>())
    private var usersLoading = false
    private var usersHaveMore = true

    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()
    private val mediaViewModel: MediaSearchViewModel by viewModel()
    private val characterViewModel: CharacterSearchViewModel by viewModel()
    private val staffViewModel: StaffSearchViewModel by viewModel()
    private val studioViewModel: StudioSearchViewModel by viewModel()
    private val userViewModel: UserSearchViewModel by viewModel()

    /** Saved-state and query normalization helpers for search. */
    companion object {
        private const val STATE_SECTION = "search_section"
        private const val STATE_QUERY = "search_query"
        private const val STATE_DRAFT_QUERY = "search_draft_query"
        private const val STATE_DRAFT_QUERY_CHANGED = "search_draft_query_changed"
        private const val USER_LIST_VISIBLE_THRESHOLD = 9

        @VisibleForTesting
        internal fun resolveLegacyQuery(raw: String?): String? = raw

        @VisibleForTesting
        internal fun normalizeSubmittedQuery(raw: String?): String? = raw?.trim()?.takeIf { it.isNotEmpty() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchQuery = savedInstanceState?.getString(STATE_QUERY)
            ?: resolveLegacyQuery(arguments?.getString(KeyUtil.arg_search))
        draftQuery = savedInstanceState?.getString(STATE_DRAFT_QUERY) ?: searchQuery.orEmpty()
        draftQueryChanged = savedInstanceState?.getBoolean(STATE_DRAFT_QUERY_CHANGED) ?: false
        section = savedInstanceState?.getString(STATE_SECTION)?.let { value ->
            runCatching { Section.valueOf(value) }.getOrDefault(Section.ANIME)
        }?.takeIf { it in visibleSections() } ?: Section.ANIME
        isPager = section.mediaType == null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        queryInput = root.findViewById<TextInputEditText>(R.id.search_query_input).apply {
            setText(draftQuery)
            setSelection(text?.length ?: 0)
            addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        draftQuery = s?.toString().orEmpty()
                        draftQueryChanged = true
                    }

                    override fun afterTextChanged(s: Editable?) = Unit
                },
            )
            setOnEditorActionListener { _, actionId, event ->
                val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                val isEnterAction = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
                if (isSearchAction || isEnterAction) {
                    submitSearchQuery()
                    true
                } else {
                    false
                }
            }
        }
        sectionSelector = root.findViewById(R.id.search_section_selector)
        val listContainer = root.findViewById<ViewGroup>(R.id.search_list_container)
        listBinding = FragmentListBinding.inflate(inflater, listContainer, true)
        val binding = requireNotNull(listBinding)
        swipeRefreshLayout = binding.refreshLayout
        stateLayout = binding.stateLayout
        composeView = ComposeView(requireContext()).apply {
            visibility = View.GONE
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme {
                    when (section) {
                        Section.CHARACTERS -> CharacterGrid(
                            items = characterItems.value,
                            gridState = characterGridState,
                            onCharacterClick = { navigateToCharacter(CharacterScreenParam(it)) },
                            onLoadMore = ::onComposeLoadMore,
                        )
                        Section.STAFF -> StaffGrid(
                            items = staffItems.value,
                            gridState = staffGridState,
                            onStaffClick = { navigateToStaff(StaffScreenParam(it)) },
                            onLoadMore = ::onComposeLoadMore,
                        )
                        Section.STUDIO -> StudioGrid(
                            items = studioItems.value,
                            gridState = studioGridState,
                            onStudioClick = { navigateToStudio(StudioScreenParam(it)) },
                            onLoadMore = ::onComposeLoadMore,
                        )
                        else -> Unit
                    }
                }
            }
        }
        mediaComposeView = ComposeView(requireContext()).apply {
            visibility = View.GONE
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val lazyPagingItems = mediaViewModel.pagingDataFlow.collectAsLazyPagingItems()
                LaunchedEffect(mediaRefreshSignal.value) {
                    val refreshSignal = mediaRefreshSignal.value
                    if (refreshSignal > consumedMediaRefreshSignal) {
                        consumedMediaRefreshSignal = refreshSignal
                        lazyPagingItems.refresh()
                    }
                }
                LaunchedEffect(lazyPagingItems) {
                    snapshotFlow { lazyPagingItems.loadState to lazyPagingItems.itemCount }
                        .collect { (states, count) ->
                            mediaPaging.value = MediaPagingSnapshot(states, count)
                        }
                }
                AniTrendTheme {
                    MediaSearchPagingList(
                        lazyPagingItems = lazyPagingItems,
                        listState = mediaListState,
                        onMediaClick = ::openMedia,
                        onMediaLongClick = ::longPressMedia,
                    )
                }
            }
        }
        userComposeView = ComposeView(requireContext()).apply {
            visibility = View.GONE
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LaunchedEffect(section, userListState) {
                    if (section == Section.USERS) {
                        snapshotFlow {
                            val layoutInfo = userListState.layoutInfo
                            layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { lastVisibleIndex ->
                                layoutInfo.totalItemsCount to lastVisibleIndex
                            }
                        }.distinctUntilChanged().collect { visibleRange ->
                            if (
                                visibleRange != null &&
                                visibleRange.first - visibleRange.second - 1 <= USER_LIST_VISIBLE_THRESHOLD
                            ) {
                                onUserLoadMore()
                            }
                        }
                    }
                }
                AniTrendTheme {
                    UserList(
                        items = userItems.value,
                        listState = userListState,
                        currentUser = databaseHelper.currentUser,
                        onUserClick = { navigateToProfile(UserScreenParam(it)) },
                        onToggleFollow = userViewModel::toggleFollow,
                    )
                }
            }
        }
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, it) }
        buildSectionSelector()
        attachSectionSurface()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // FragmentBaseList restores these in onViewStateRestored, after this request decision.
        savedInstanceState?.let { state ->
            pageState.currentPage = state.getInt(KeyUtil.arg_page, pageState.currentPage)
            pageState.currentOffset = state.getInt(KeyUtil.arg_page_offset, pageState.currentOffset)
        }
        super.onViewCreated(view, savedInstanceState)
        loadStateRenderer =
            PagingLoadStateRenderer(
                itemCount = { mediaPaging.value?.count ?: 0 },
                callbacks = PagingLoadStateRenderer.Callbacks(
                    showLoading = ::showLoading,
                    showContent = ::showContent,
                    showError = ::showError,
                    showEmpty = ::showEmpty,
                    stopRefreshIndicators = {
                        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false)
                        if (swipeRefreshLayout.isLoading()) swipeRefreshLayout.setLoading(false)
                    },
                    messages = PagingLoadStateRenderer.Callbacks.Messages(
                        errorMessage = { getString(R.string.text_error_request) },
                        emptyMessage = { getString(R.string.layout_empty_response) },
                    ),
                ),
            )
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // Re-renders on load-state changes and on section changes: the
                    // query-keyed Pager is idempotent, so returning to the media
                    // tabs emits no new load state and the cached state must drive
                    // the surfaces again. Both signals stay reactive (snapshotFlow
                    // alone would not observe the StateFlow).
                    combine(mediaPaging, snapshotFlow { section }) { paging, currentSection ->
                        paging to currentSection
                    }.collect { (paging, currentSection) ->
                        if (paging != null && currentSection.mediaType != null) {
                            loadStateRenderer.render(paging.states)
                        }
                    }
                }
                launch { characterViewModel.state.collect { state -> if (section == Section.CHARACTERS) renderCharacter(state) } }
                launch { staffViewModel.state.collect { state -> if (section == Section.STAFF) renderStaff(state) } }
                launch { studioViewModel.state.collect { state -> if (section == Section.STUDIO) renderStudio(state) } }
                launch { userViewModel.state.collect { state -> if (section == Section.USERS) renderUser(state) } }
                launch { userViewModel.followStates.collect(::rebindFollowStates) }
            }
        }
        if (!searchQuery.isNullOrBlank()) {
            resetComposePagingForQueryChange()
            if (!hasCurrentComposePage()) {
                if (pageState.currentPage > 1) resetComposePaging()
                makeRequest()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (section.isComposeGrid() || section == Section.USERS) {
            hasCurrentComposePage()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SECTION, section.name)
        outState.putString(STATE_QUERY, searchQuery)
        outState.putString(STATE_DRAFT_QUERY, queryInput?.text?.toString() ?: draftQuery)
        outState.putBoolean(STATE_DRAFT_QUERY_CHANGED, draftQueryChanged)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        queryInput = null
        listBinding = null
        super.onDestroyView()
    }

    override fun onRefresh() {
        if (section.mediaType != null) {
            mediaRefreshSignal.value++
        } else {
            if (section.isComposeGrid()) resetComposePaging()
            if (section == Section.USERS) {
                usersLoading = false
                usersHaveMore = true
            }
            super.onRefresh()
        }
    }

    override fun updateUI() {
        if (section.mediaType != null) return
        setSwipeRefreshLayoutEnabled(false)
        showContent()
    }

    override fun makeRequest() {
        val query = searchQuery?.takeIf { it.isNotBlank() } ?: return
        val type = section.mediaType?.let { MediaType.valueOf(it) }
        if (section.mediaType != null) {
            val isAdult: Boolean? = if (settings.displayAdultContent) null else false
            mediaViewModel.load(query, type, isAdult)
            return
        }
        if (section.isComposeGrid()) composeLoading = true
        when (section) {
            Section.CHARACTERS -> {
                lastCharacterSearch = query
                lastCharacterPage = pageState.currentPage
                characterViewModel.load(query, pageState.currentPage)
            }
            Section.STAFF -> {
                lastStaffSearch = query
                lastStaffPage = pageState.currentPage
                staffViewModel.load(query, pageState.currentPage)
            }
            Section.STUDIO -> {
                lastStudioSearch = query
                lastStudioPage = pageState.currentPage
                studioViewModel.load(query, pageState.currentPage)
            }
            Section.USERS -> {
                lastUserSearch = query
                lastUserPage = pageState.currentPage
                usersLoading = true
                userViewModel.load(query, pageState.currentPage)
            }
            Section.ANIME, Section.MANGA -> Unit
        }
    }

    override fun onChanged(value: Any?) = Unit

    private fun buildSectionSelector() {
        visibleSections().forEach { value ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(value.title)
                isCheckable = true
                setOnClickListener { selectSection(value) }
            }
            sectionSelector.addView(chip)
        }
        val initialIndex = visibleSections().indexOf(section).coerceAtLeast(0)
        sectionSelector.check(sectionSelector.getChildAt(initialIndex).id)
    }

    private fun submitSearchQuery() {
        val query = normalizeSubmittedQuery(queryInput?.text?.toString())
        if (query == null) {
            Toast.makeText(requireContext(), R.string.text_search_empty, Toast.LENGTH_SHORT).show()
            return
        }
        searchQuery = query
        draftQueryChanged = false
        pageState.onRefreshPage()
        if (section.mediaType != null) {
            makeRequest()
            mediaRefreshSignal.value++
        } else {
            clearComposeItems()
            resetComposePaging()
            showLoading()
            makeRequest()
        }
    }

    private fun selectSection(value: Section, reload: Boolean = true) {
        if (section == value && reload) return
        section = value
        isPager = value.mediaType == null
        sectionSelector.getChildAt(visibleSections().indexOf(value))?.let { sectionSelector.check(it.id) }
        attachSectionSurface()
        if (reload && view != null) {
            // Tab changes remain an explicit search action for the current draft.
            if (draftQueryChanged) {
                searchQuery = draftQuery.takeIf { it.isNotEmpty() }
                draftQueryChanged = false
            }
            clearComposeItems()
            resetComposePaging()
            showLoading()
            makeRequest()
        }
        // Returning to a media tab emits no new paging state (the query-keyed
        // Pager is idempotent), so the cached state must re-drive the surfaces
        // synchronously or the selection loading state never dismisses.
        if (section.mediaType != null) {
            mediaPaging.value?.let { loadStateRenderer.render(it.states) }
        }
    }

    private fun attachSectionSurface() {
        if (listBinding == null) return
        val composeGrid = section.isComposeGrid()
        val mediaTab = section.mediaType != null
        val usersTab = section == Section.USERS
        composeView.visibility = if (composeGrid) View.VISIBLE else View.GONE
        mediaComposeView.visibility = if (mediaTab) View.VISIBLE else View.GONE
        userComposeView.visibility = if (usersTab) View.VISIBLE else View.GONE
        when {
            mediaTab -> {
                swipeRefreshLayout.setPermitLoad(false)
                swapRefreshTarget(mediaComposeView)
            }
            composeGrid -> {
                swipeRefreshLayout.setPermitLoad(false)
                swapRefreshTarget(composeView)
            }
            usersTab -> {
                swipeRefreshLayout.setPermitLoad(false)
                swapRefreshTarget(userComposeView)
            }
        }
    }

    /**
     * CustomSwipeRefreshLayout measures and scroll-gates only its single cached
     * target, so the active Compose host owns that slot and inactive hosts leave
     * the container (the container re-resolves a target that left).
     */
    private fun swapRefreshTarget(host: View) {
        if (host !== composeView && composeView.parent === swipeRefreshLayout) {
            swipeRefreshLayout.removeView(composeView)
        }
        if (host !== mediaComposeView && mediaComposeView.parent === swipeRefreshLayout) {
            swipeRefreshLayout.removeView(mediaComposeView)
        }
        if (host !== userComposeView && userComposeView.parent === swipeRefreshLayout) {
            swipeRefreshLayout.removeView(userComposeView)
        }
        swipeRefreshLayout.scrollBoundary = { direction ->
            when (host) {
                mediaComposeView -> if (direction < 0) mediaListState.canScrollBackward else mediaListState.canScrollForward
                userComposeView -> if (direction < 0) userListState.canScrollBackward else userListState.canScrollForward
                composeView -> when (section) {
                    Section.CHARACTERS ->
                        if (direction < 0) characterGridState.canScrollBackward else characterGridState.canScrollForward
                    Section.STAFF ->
                        if (direction < 0) staffGridState.canScrollBackward else staffGridState.canScrollForward
                    Section.STUDIO ->
                        if (direction < 0) studioGridState.canScrollBackward else studioGridState.canScrollForward
                    else -> false
                }
                else -> host.canScrollVertically(direction)
            }
        }
        if (host.parent !== swipeRefreshLayout) {
            swipeRefreshLayout.addView(
                host,
                0,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
        }
    }

    private fun renderUserPage(content: List<UserBase>?) {
        usersLoading = false
        val mapped = content.orEmpty().map { it.toUserRowUiState() }
        userItems.value = mapped
        rebindFollowStates(userViewModel.followStates.value)
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false)
        if (swipeRefreshLayout.isLoading()) swipeRefreshLayout.setLoading(false)
        if (mapped.isNotEmpty()) {
            updateUI()
        } else {
            usersHaveMore = false
            if (isPager) setLimitReached()
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    private fun onUserLoadMore() {
        if (section != Section.USERS || usersLoading || !usersHaveMore || isLimit || searchQuery.isNullOrBlank()) return
        usersLoading = true
        pageState.currentPage++
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }

    private fun Section.isComposeGrid(): Boolean = this == Section.CHARACTERS || this == Section.STAFF || this == Section.STUDIO

    // A matching ViewModel page is authoritative; without one, a fresh process reloads page one.
    private fun hasCurrentComposePage(): Boolean {
        val query = searchQuery ?: return false
        val requestedPage = when (section) {
            Section.CHARACTERS ->
                when (val state = characterViewModel.state.value) {
                    is CharacterSearchViewModel.UiState.Success -> state.page.takeIf { state.search == query }
                    is CharacterSearchViewModel.UiState.Error -> state.page.takeIf { state.search == query }
                    CharacterSearchViewModel.UiState.Loading ->
                        lastCharacterPage.takeIf { lastCharacterSearch == query && it > 0 }
                }
            Section.STAFF ->
                when (val state = staffViewModel.state.value) {
                    is StaffSearchViewModel.UiState.Success -> state.page.takeIf { state.search == query }
                    is StaffSearchViewModel.UiState.Error -> state.page.takeIf { state.search == query }
                    StaffSearchViewModel.UiState.Loading ->
                        lastStaffPage.takeIf { lastStaffSearch == query && it > 0 }
                }
            Section.STUDIO ->
                when (val state = studioViewModel.state.value) {
                    is StudioSearchViewModel.UiState.Success -> state.page.takeIf { state.search == query }
                    is StudioSearchViewModel.UiState.Error -> state.page.takeIf { state.search == query }
                    StudioSearchViewModel.UiState.Loading ->
                        lastStudioPage.takeIf { lastStudioSearch == query && it > 0 }
                }
            Section.USERS ->
                when (val state = userViewModel.state.value) {
                    is UserSearchViewModel.UiState.Success -> state.page.takeIf { state.search == query }
                    is UserSearchViewModel.UiState.Error -> state.page.takeIf { state.search == query }
                    UserSearchViewModel.UiState.Loading ->
                        lastUserPage.takeIf { lastUserSearch == query && it > 0 }
                }
            else -> null
        } ?: return false
        pageState.currentPage = requestedPage
        pageState.currentOffset = (requestedPage - 1) * KeyUtil.PAGING_LIMIT
        return true
    }

    private fun resetComposePagingForQueryChange() {
        if ((!section.isComposeGrid() && section != Section.USERS) || pageState.currentPage <= 1) return
        val query = searchQuery ?: return
        val loadedQuery = when (section) {
            Section.CHARACTERS -> when (val state = characterViewModel.state.value) {
                is CharacterSearchViewModel.UiState.Success -> state.search
                is CharacterSearchViewModel.UiState.Error -> state.search
                is CharacterSearchViewModel.UiState.Loading -> lastCharacterSearch
            }
            Section.STAFF -> when (val state = staffViewModel.state.value) {
                is StaffSearchViewModel.UiState.Success -> state.search
                is StaffSearchViewModel.UiState.Error -> state.search
                is StaffSearchViewModel.UiState.Loading -> lastStaffSearch
            }
            Section.STUDIO -> when (val state = studioViewModel.state.value) {
                is StudioSearchViewModel.UiState.Success -> state.search
                is StudioSearchViewModel.UiState.Error -> state.search
                is StudioSearchViewModel.UiState.Loading -> lastStudioSearch
            }
            Section.USERS -> when (val state = userViewModel.state.value) {
                is UserSearchViewModel.UiState.Success -> state.search
                is UserSearchViewModel.UiState.Error -> state.search
                is UserSearchViewModel.UiState.Loading -> lastUserSearch
            }
            else -> null
        }
        if (loadedQuery != null && loadedQuery != query) resetComposePaging()
    }

    private fun onComposeLoadMore() {
        if (composeLoading || !composeHasMore) return
        if (searchQuery.isNullOrBlank()) return
        composeLoading = true
        pageState.currentPage++
        makeRequest()
    }

    private fun resetComposePaging() {
        pageState.onRefreshPage()
        composeHasMore = true
        composeLoading = false
    }

    private fun clearComposeItems() {
        characterItems.value = emptyList()
        staffItems.value = emptyList()
        studioItems.value = emptyList()
        userItems.value = emptyList()
        usersLoading = false
        usersHaveMore = true
        isLimit = false
    }

    private fun commitComposePage(newCount: Int, totalCount: Int, hasNextPage: Boolean) {
        composeLoading = false
        composeHasMore = hasNextPage
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false)
        if (swipeRefreshLayout.isLoading()) swipeRefreshLayout.setLoading(false)
        if (newCount == 0) {
            composeHasMore = false
            if (totalCount == 0) {
                showEmpty(getString(R.string.layout_empty_response))
            } else {
                showContent()
            }
        } else {
            showContent()
        }
    }

    private fun visibleSections(): List<Section> = if (settings.isAuthenticated) Section.values().toList() else Section.values().filterNot { it == Section.USERS }

    private fun renderCharacter(state: CharacterSearchViewModel.UiState) {
        when (state) {
            is CharacterSearchViewModel.UiState.Loading -> showLoading()
            is CharacterSearchViewModel.UiState.Success -> {
                if (state.search != searchQuery || section != Section.CHARACTERS) return
                state.content.pageInfo?.let(pageState::setPageInfo)
                val mapped = state.items.map { CharacterRowUiState.from(it) }
                characterItems.value = mapped
                commitComposePage(state.itemsInPage, mapped.size, state.content.pageInfo?.hasNextPage() != false)
            }
            is CharacterSearchViewModel.UiState.Error -> {
                if (state.search == searchQuery && section == Section.CHARACTERS) showError(state.message)
            }
        }
    }

    private fun renderStaff(state: StaffSearchViewModel.UiState) {
        when (state) {
            is StaffSearchViewModel.UiState.Loading -> showLoading()
            is StaffSearchViewModel.UiState.Success -> {
                if (state.search != searchQuery || section != Section.STAFF) return
                state.content.pageInfo?.let(pageState::setPageInfo)
                val mapped = state.items.map { StaffRowUiState.from(it) }
                staffItems.value = mapped
                commitComposePage(state.itemsInPage, mapped.size, state.content.pageInfo?.hasNextPage() != false)
            }
            is StaffSearchViewModel.UiState.Error -> {
                if (state.search == searchQuery && section == Section.STAFF) showError(state.message)
            }
        }
    }

    private fun renderStudio(state: StudioSearchViewModel.UiState) {
        when (state) {
            is StudioSearchViewModel.UiState.Loading -> showLoading()
            is StudioSearchViewModel.UiState.Success -> {
                if (state.search != searchQuery || section != Section.STUDIO) return
                state.content.pageInfo?.let(pageState::setPageInfo)
                val mapped = state.items.map { StudioRowUiState.from(it) }
                studioItems.value = mapped
                commitComposePage(state.itemsInPage, mapped.size, state.content.pageInfo?.hasNextPage() != false)
            }
            is StudioSearchViewModel.UiState.Error -> {
                if (state.search == searchQuery && section == Section.STUDIO) showError(state.message)
            }
        }
    }

    private fun renderUser(state: UserSearchViewModel.UiState) {
        when (state) {
            is UserSearchViewModel.UiState.Loading -> {
                usersLoading = true
                showLoading()
            }
            is UserSearchViewModel.UiState.Success -> {
                if (state.search != searchQuery || section != Section.USERS) return
                pageState.setPageInfo(state.content.pageInfo)
                usersHaveMore = state.content.pageInfo.hasNextPage() && state.itemsInPage > 0
                renderUserPage(state.content.pageData)
            }
            is UserSearchViewModel.UiState.Error -> {
                if (state.search != searchQuery || section != Section.USERS) return
                usersHaveMore = false
                usersLoading = false
                showError(state.message)
            }
        }
    }

    private fun UserBase.toUserRowUiState(): UserRowUiState = UserRowUiState(
        model = this,
        name = name,
        avatar = avatar,
        isFollowing = isFollowing,
    )

    private fun rebindFollowStates(states: Map<Long, Boolean>) {
        if (section != Section.USERS) return
        var changed = false
        val updated = userItems.value.map { item ->
            val isFollowing = states[item.model.id] ?: return@map item
            if (item.isFollowing == isFollowing) return@map item
            changed = true
            item.copy(isFollowing = isFollowing)
        }
        if (changed) userItems.value = updated
    }

    private fun openMedia(item: MediaSearchItemUiModel) {
        navigateToMedia(MediaScreenParam(item.id, item.mediaType))
    }

    private fun longPressMedia(item: MediaSearchItemUiModel): Boolean {
        if (settings.isAuthenticated) {
            mediaActionUtil = MediaActionUtil.Builder().setId(item.id).build(requireActivity())
            mediaActionUtil.startSeriesAction()
            return true
        }
        NotifyUtil.makeText(
            requireContext(),
            R.string.info_login_req,
            R.drawable.ic_group_add_grey_600_18dp,
            Toast.LENGTH_SHORT,
        ).show()
        return true
    }
}
