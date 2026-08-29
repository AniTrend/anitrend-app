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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.adapter.recycler.search.MediaSearchAdapter
import com.mxt.anitrend.adapter.recycler.shared.LoadStateFooterAdapter
import com.mxt.anitrend.adapter.recycler.shared.PagingLoadStateRenderer
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.custom.recycler.RecyclerSectionAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
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
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.CharacterSearchViewModel
import com.mxt.anitrend.viewmodel.MediaSearchViewModel
import com.mxt.anitrend.viewmodel.StaffSearchViewModel
import com.mxt.anitrend.viewmodel.StudioSearchViewModel
import com.mxt.anitrend.viewmodel.UserSearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Single Navigation 2 destination for media, staff, studio, character, and user search. */
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
    private lateinit var sectionAdapter: RecyclerSectionAdapter
    private lateinit var mediaSearchAdapter: MediaSearchAdapter
    private lateinit var mediaAdapterWithFooter: RecyclerView.Adapter<*>
    private var section = Section.ANIME
    private var searchQuery: String? = null
    private var queryInput: TextInputEditText? = null

    private val settings: Settings by inject()
    private val databaseHelper: DatabaseHelper by inject()
    private val mediaViewModel: MediaSearchViewModel by viewModel()
    private val characterViewModel: CharacterSearchViewModel by viewModel()
    private val staffViewModel: StaffSearchViewModel by viewModel()
    private val studioViewModel: StudioSearchViewModel by viewModel()
    private val userViewModel: UserSearchViewModel by viewModel()

    companion object {
        private const val STATE_SECTION = "search_section"
        private const val STATE_QUERY = "search_query"

        @VisibleForTesting
        internal fun resolveLegacyQuery(raw: String?): String? = raw

        @VisibleForTesting
        internal fun normalizeSubmittedQuery(raw: String?): String? = raw?.trim()?.takeIf { it.isNotEmpty() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchQuery = savedInstanceState?.getString(STATE_QUERY)
            ?: resolveLegacyQuery(arguments?.getString(KeyUtil.arg_search))
        section = savedInstanceState?.getString(STATE_SECTION)?.let { value ->
            runCatching { Section.valueOf(value) }.getOrDefault(Section.ANIME)
        }?.takeIf { it in visibleSections() } ?: Section.ANIME
        sectionAdapter = RecyclerSectionAdapter(requireContext()) { sectionClickListener() }
        mAdapter = sectionAdapter
        mediaSearchAdapter = MediaSearchAdapter(
            context = requireContext(),
            onOpenMedia = ::openMedia,
            onLongPressMedia = ::longPressMedia,
        )
        mediaAdapterWithFooter = mediaSearchAdapter.withLoadStateFooter(
            footer = LoadStateFooterAdapter(mediaSearchAdapter::retry),
        )
        isPager = false
        sectionAdapter.select(createAdapter(section))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        queryInput = root.findViewById<TextInputEditText>(R.id.search_query_input).apply {
            setText(searchQuery.orEmpty())
            setSelection(text?.length ?: 0)
            addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        searchQuery = s?.toString()?.takeIf { it.isNotEmpty() }
                    }

                    override fun afterTextChanged(s: Editable?) = Unit
                },
            )
            setOnEditorActionListener { _, actionId, event ->
                val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                val isEnterAction = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
                if (isSearchAction || isEnterAction) {
                    submitQuery()
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
        recyclerView = binding.recyclerView
        stateLayout = binding.stateLayout
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true
        mColumnSize = columnSizeFor(section)
        mLayoutManager = StaggeredGridLayoutManager(
            resources.getInteger(mColumnSize),
            StaggeredGridLayoutManager.VERTICAL,
        )
        recyclerView.layoutManager = mLayoutManager
        swipeRefreshLayout.setOnRefreshAndLoadListener(this)
        activity?.let { CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, it) }
        buildSectionSelector()
        attachSectionSurface()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loadStateRenderer = PagingLoadStateRenderer(
            itemCount = { mediaSearchAdapter.itemCount },
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
                    mediaViewModel.pagingDataFlow.collectLatest { pagingData ->
                        mediaSearchAdapter.submitData(pagingData)
                    }
                }
                launch {
                    mediaSearchAdapter.loadStateFlow.collect {
                        if (section.mediaType != null) loadStateRenderer.render(it)
                    }
                }
                launch { characterViewModel.state.collect { state -> if (section == Section.CHARACTERS) renderCharacter(state) } }
                launch { staffViewModel.state.collect { state -> if (section == Section.STAFF) renderStaff(state) } }
                launch { studioViewModel.state.collect { state -> if (section == Section.STUDIO) renderStudio(state) } }
                launch { userViewModel.state.collect { state -> if (section == Section.USERS) renderUser(state) } }
                launch { userViewModel.followStates.collect(::rebindFollowStates) }
            }
        }
        if (!searchQuery.isNullOrBlank()) makeRequest()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SECTION, section.name)
        outState.putString(STATE_QUERY, searchQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        queryInput = null
        listBinding = null
        super.onDestroyView()
    }

    override fun onRefresh() {
        if (section.mediaType != null) mediaSearchAdapter.refresh() else super.onRefresh()
    }

    override fun onLoadMore() {
        if (section.mediaType == null) super.onLoadMore()
    }

    override fun updateUI() {
        if (section.mediaType != null) return
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val query = searchQuery?.takeIf { it.isNotBlank() } ?: return
        val type = section.mediaType?.let { MediaType.valueOf(it) }
        if (section.mediaType != null) {
            val isAdult: Boolean? = if (settings.displayAdultContent) null else false
            mediaViewModel.load(query, type, isAdult)
            return
        }
        when (section) {
            Section.CHARACTERS -> characterViewModel.load(query, mScrollListener.currentPage)
            Section.STAFF -> staffViewModel.load(query, mScrollListener.currentPage)
            Section.STUDIO -> studioViewModel.load(query, mScrollListener.currentPage)
            Section.USERS -> userViewModel.load(query, mScrollListener.currentPage)
            Section.ANIME, Section.MANGA -> Unit
        }
    }

    override fun onChanged(value: Any?) = Unit

    override fun onItemClick(target: View, data: IndexedValue<Any>) {
        if (target.id != R.id.container) return
        when (val item = data.value) {
            is CharacterBase -> navigateToCharacter(CharacterScreenParam(item.id))
            is StaffBase -> navigateToStaff(StaffScreenParam(item.id))
            is StudioBase -> navigateToStudio(StudioScreenParam(item.id))
            is UserBase -> navigateToProfile(UserScreenParam(item.id))
        }
    }

    override fun onItemLongClick(target: View, data: IndexedValue<Any>) = Unit

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

    /**
     * Search is entered on this destination rather than through the host toolbar. Keeping the
     * field local lets the user replace the route query and submit again without leaving the
     * destination, while the editor action remains the keyboard's search action.
     */
    private fun submitQuery() {
        val query = normalizeSubmittedQuery(queryInput?.text?.toString())
        if (query == null) {
            Toast.makeText(requireContext(), R.string.text_search_empty, Toast.LENGTH_SHORT).show()
            return
        }
        searchQuery = query
        mScrollListener.onRefreshPage()
        if (section.mediaType != null) {
            makeRequest()
            mediaSearchAdapter.refresh()
        } else {
            sectionAdapter.clearSelectedSection()
            showLoading()
            makeRequest()
        }
    }

    private fun selectSection(value: Section, reload: Boolean = true) {
        if (section == value && reload) return
        removeScrollLoadTrigger()
        section = value
        isPager = value.mediaType == null
        mColumnSize = columnSizeFor(value)
        if (::mLayoutManager.isInitialized) mLayoutManager.spanCount = resources.getInteger(mColumnSize)
        sectionSelector.getChildAt(visibleSections().indexOf(value))?.let { sectionSelector.check(it.id) }
        attachSectionSurface()
        if (reload && view != null) {
            mScrollListener.onRefreshPage()
            sectionAdapter.clearSelectedSection()
            showLoading()
            makeRequest()
        }
    }

    private fun attachSectionSurface() {
        if (!::recyclerView.isInitialized) return
        if (section.mediaType != null) {
            swipeRefreshLayout.setPermitLoad(false)
            recyclerView.adapter = mediaAdapterWithFooter
        } else {
            swipeRefreshLayout.setPermitLoad(true)
            sectionAdapter.select(createAdapter(section))
            recyclerView.adapter = sectionAdapter
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) addScrollLoadTrigger()
        }
    }

    private fun visibleSections(): List<Section> = if (settings.isAuthenticated) Section.values().toList() else Section.values().filterNot { it == Section.USERS }

    private fun createAdapter(value: Section): RecyclerViewAdapter<*> = when (value) {
        Section.ANIME, Section.MANGA -> com.mxt.anitrend.adapter.recycler.index.MediaAdapter(requireContext(), true)
        Section.CHARACTERS -> GroupCharacterAdapter(requireContext())
        Section.STAFF -> StaffAdapter(requireContext())
        Section.STUDIO -> StudioAdapter(requireContext())
        Section.USERS -> UserAdapter(requireContext(), databaseHelper.currentUser) { userId, _ ->
            userViewModel.toggleFollow(userId)
        }
    }

    private fun sectionClickListener() = object : ItemClickListener<Any> {
        override fun onItemClick(target: View, data: IndexedValue<Any>) = this@SearchFragment.onItemClick(target, data)
        override fun onItemLongClick(target: View, data: IndexedValue<Any>) = this@SearchFragment.onItemLongClick(target, data)
    }

    private fun columnSizeFor(value: Section): Int = when (value) {
        Section.STUDIO -> R.integer.grid_list_x2
        Section.USERS -> R.integer.single_list_x1
        else -> R.integer.grid_giphy_x3
    }

    private fun renderCharacter(state: CharacterSearchViewModel.UiState) {
        when (state) {
            is CharacterSearchViewModel.UiState.Loading -> showLoading()
            is CharacterSearchViewModel.UiState.Success -> {
                state.content.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.pageData.let(GroupingUtil::wrapInGroup).map { it as Any })
            }
            is CharacterSearchViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderStaff(state: StaffSearchViewModel.UiState) {
        when (state) {
            is StaffSearchViewModel.UiState.Loading -> showLoading()
            is StaffSearchViewModel.UiState.Success -> {
                state.content.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.pageData.map { it as Any })
            }
            is StaffSearchViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderStudio(state: StudioSearchViewModel.UiState) {
        when (state) {
            is StudioSearchViewModel.UiState.Loading -> showLoading()
            is StudioSearchViewModel.UiState.Success -> {
                state.content.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.pageData.map { it as Any })
            }
            is StudioSearchViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderUser(state: UserSearchViewModel.UiState) {
        when (state) {
            is UserSearchViewModel.UiState.Loading -> showLoading()
            is UserSearchViewModel.UiState.Success -> {
                state.content.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.pageData.map { it as Any })
            }
            is UserSearchViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun rebindFollowStates(states: Map<Long, Boolean>) {
        if (section != Section.USERS) return
        states.forEach { (userId, isFollowing) ->
            val position = sectionAdapter.data.indexOfFirst { (it as? UserBase)?.id == userId }
            if (position < 0) return@forEach
            val user = sectionAdapter.data[position] as? UserBase ?: return@forEach
            if (user.isFollowing == isFollowing) return@forEach
            user.isFollowing = isFollowing
            sectionAdapter.onItemChanged(user, position)
        }
    }

    private fun openMedia(target: View, item: MediaSearchItemUiModel) {
        navigateToMedia(MediaScreenParam(item.id, item.mediaType))
    }

    private fun longPressMedia(target: View, item: MediaSearchItemUiModel): Boolean {
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
