package com.mxt.anitrend.view.fragment.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.custom.recycler.RecyclerSectionAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
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
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.CharacterFavouritesViewModel
import com.mxt.anitrend.viewmodel.MediaFavouritesViewModel
import com.mxt.anitrend.viewmodel.StaffFavouritesViewModel
import com.mxt.anitrend.viewmodel.StudioFavouritesViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Single Navigation 2 destination for a user's favourite sections. */
class FavouriteFragment : FragmentBaseList<Any, Any>() {
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
    private lateinit var sectionAdapter: RecyclerSectionAdapter
    private var section = Section.ANIME
    private var userId = 0L

    private val settings: Settings by inject()
    private val mediaViewModel: MediaFavouritesViewModel by viewModel()
    private val characterViewModel: CharacterFavouritesViewModel by viewModel()
    private val staffViewModel: StaffFavouritesViewModel by viewModel()
    private val studioViewModel: StudioFavouritesViewModel by viewModel()

    companion object {
        private const val STATE_SECTION = "favourite_section"

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
        sectionAdapter = RecyclerSectionAdapter(requireContext()) { sectionClickListener() }
        mAdapter = sectionAdapter
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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { mediaViewModel.state.collect { state -> if (state.mediaType == activeMediaType()) renderMedia(state) } }
                launch { characterViewModel.state.collect { state -> if (section == Section.CHARACTERS) renderCharacter(state) } }
                launch { staffViewModel.state.collect { state -> if (section == Section.STAFF) renderStaff(state) } }
                launch { studioViewModel.state.collect { state -> if (section == Section.STUDIOS) renderStudio(state) } }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SECTION, section.name)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        listBinding = null
        super.onDestroyView()
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(section == Section.ANIME || section == Section.MANGA)
        injectAdapter()
    }

    override fun makeRequest() {
        when (section) {
            Section.ANIME, Section.MANGA -> mediaViewModel.load(userId, mScrollListener.currentPage, activeMediaType())
            Section.CHARACTERS -> characterViewModel.load(userId, mScrollListener.currentPage)
            Section.STAFF -> staffViewModel.load(userId, mScrollListener.currentPage)
            Section.STUDIOS -> studioViewModel.load(userId, mScrollListener.currentPage)
        }
    }

    override fun onChanged(value: Any?) = Unit

    override fun onItemClick(target: View, data: IndexedValue<Any>) {
        if (target.id != R.id.container) return
        when (val item = data.value) {
            is MediaBase -> navigateToMedia(MediaScreenParam(item.id, item.type))
            is CharacterBase -> navigateToCharacter(CharacterScreenParam(item.id))
            is StaffBase -> navigateToStaff(StaffScreenParam(item.id))
            is StudioBase -> navigateToStudio(StudioScreenParam(item.id))
        }
    }

    override fun onItemLongClick(target: View, data: IndexedValue<Any>) {
        if (target.id != R.id.container || data.value !is MediaBase) return
        if (settings.isAuthenticated) {
            mediaActionUtil = MediaActionUtil.Builder().setId((data.value as MediaBase).id).build(requireActivity())
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
        if (::sectionAdapter.isInitialized) {
            sectionAdapter.select(createAdapter(value))
        }
        mColumnSize = columnSizeFor(value)
        if (::mLayoutManager.isInitialized) {
            mLayoutManager.spanCount = resources.getInteger(mColumnSize)
        }
        if (::sectionSelector.isInitialized) {
            sectionSelector.getChildAt(value.ordinal)?.let { sectionSelector.check(it.id) }
        }
        if (reload && view != null) {
            mScrollListener.onRefreshPage()
            sectionAdapter.clearSelectedSection()
            showLoading()
            makeRequest()
        }
    }

    private fun createAdapter(value: Section): RecyclerViewAdapter<*> = when (value) {
        Section.ANIME, Section.MANGA -> MediaAdapter(requireContext(), true)
        Section.CHARACTERS -> GroupCharacterAdapter(requireContext())
        Section.STAFF -> StaffAdapter(requireContext())
        Section.STUDIOS -> StudioAdapter(requireContext())
    }

    private fun sectionClickListener() = object : ItemClickListener<Any> {
        override fun onItemClick(target: View, data: IndexedValue<Any>) = this@FavouriteFragment.onItemClick(target, data)
        override fun onItemLongClick(target: View, data: IndexedValue<Any>) = this@FavouriteFragment.onItemLongClick(target, data)
    }

    private fun activeMediaType(): String = if (section == Section.MANGA) KeyUtil.MANGA else KeyUtil.ANIME

    private fun columnSizeFor(value: Section): Int = if (value == Section.STUDIOS) R.integer.grid_list_x2 else R.integer.grid_giphy_x3

    private fun renderMedia(state: MediaFavouritesViewModel.UiState) {
        when (state) {
            is MediaFavouritesViewModel.UiState.Loading -> showLoading()
            is MediaFavouritesViewModel.UiState.Success -> {
                val container = if (state.mediaType == KeyUtil.ANIME) state.content.connection.anime else state.content.connection.manga
                container?.pageInfo?.let(::setPageInfo)
                onPostProcessed(container?.pageData?.map { it as Any })
            }
            is MediaFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderCharacter(state: CharacterFavouritesViewModel.UiState) {
        when (state) {
            is CharacterFavouritesViewModel.UiState.Loading -> showLoading()
            is CharacterFavouritesViewModel.UiState.Success -> {
                state.content.connection.characters?.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.connection.characters?.pageData?.let(GroupingUtil::wrapInGroup)?.map { it as Any })
            }
            is CharacterFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderStaff(state: StaffFavouritesViewModel.UiState) {
        when (state) {
            is StaffFavouritesViewModel.UiState.Loading -> showLoading()
            is StaffFavouritesViewModel.UiState.Success -> {
                state.content.connection.staff?.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.connection.staff?.pageData?.map { it as Any })
            }
            is StaffFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }

    private fun renderStudio(state: StudioFavouritesViewModel.UiState) {
        when (state) {
            is StudioFavouritesViewModel.UiState.Loading -> showLoading()
            is StudioFavouritesViewModel.UiState.Success -> {
                state.content.connection.studios?.pageInfo?.let(::setPageInfo)
                onPostProcessed(state.content.connection.studios?.pageData?.map { it as Any })
            }
            is StudioFavouritesViewModel.UiState.Error -> showError(state.message)
        }
    }
}
