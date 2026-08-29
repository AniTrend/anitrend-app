package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.StaffCharacterRolesAdapter
import com.mxt.anitrend.adapter.recycler.detail.StaffMediaAdapter
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.databinding.FragmentStaffBinding
import com.mxt.anitrend.extension.serializable
import com.mxt.anitrend.model.entity.base.CharacterStaffBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.navigation.extension.navigateToCharacter
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel
import com.mxt.anitrend.viewmodel.MediaFormatViewModel
import com.mxt.anitrend.viewmodel.MediaStaffRoleViewModel
import com.mxt.anitrend.viewmodel.StaffOverviewViewModel
import com.mxt.anitrend.viewmodel.StaffViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Unified Staff destination. Former pager pages are local screen sections,
 * not child fragments or a replacement pager.
 */
class StaffFragment : Fragment() {

    private enum class Section {
        OVERVIEW,
        ANIME_ROLES,
        MEDIA_ROLES,
        STAFF_ROLES,
    }

    companion object {
        private const val KEY_SECTION = "staff.section"
        private const val KEY_ON_LIST = "staff.on_list"

        fun fromBundle(bundle: Bundle?): StaffScreenParam? = resolve(
            typed = bundle?.screenParam<StaffScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
        )

        @VisibleForTesting
        internal fun resolve(typed: StaffScreenParam?, legacyId: Long): StaffScreenParam? {
            typed?.takeIf { it.staffId > 0L }?.let { return it }
            return legacyId.takeIf { it > 0L }?.let(::StaffScreenParam)
        }
    }

    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!

    private var staffId = 0L
    private var onList: Boolean? = null
    private var model: com.mxt.anitrend.domain.model.StaffRecord? = null
    private var selectedSection = Section.OVERVIEW
    private var mediaActionUtil: MediaActionUtil? = null
    private var favouriteWidget: FavouriteToolbarWidget? = null

    private val settings: Settings by inject()
    private val staffViewModel: StaffViewModel by viewModel()
    private val overviewViewModel: StaffOverviewViewModel by viewModel()
    private val animeRolesViewModel: MediaAnimeRoleViewModel by viewModel()
    private val mediaRolesViewModel: MediaFormatViewModel by viewModel()
    private val staffRolesViewModel: MediaStaffRoleViewModel by viewModel()

    private lateinit var overviewSection: StaffOverviewSection
    private lateinit var animeRolesSection: DetailListSection
    private lateinit var mediaRolesSection: DetailListSection
    private lateinit var staffRolesSection: DetailListSection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        staffId = fromBundle(arguments)?.staffId ?: 0L
        onList = arguments?.serializable<Boolean>(KeyUtil.arg_onList)
        selectedSection = savedInstanceState?.getString(KEY_SECTION)
            ?.let { runCatching { Section.valueOf(it) }.getOrNull() }
            ?: Section.OVERVIEW
        onList = savedInstanceState?.getString(KEY_ON_LIST)?.let { value ->
            when (value) {
                "true" -> true
                "false" -> false
                else -> null
            }
        } ?: onList
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStaffBinding.inflate(inflater, container, false)
        overviewSection = StaffOverviewSection(::retryOverview)
        animeRolesSection = createAnimeRolesSection()
        mediaRolesSection = createMediaRolesSection()
        staffRolesSection = createStaffRolesSection()

        addSectionView(overviewSection.createView(inflater, binding.staffSectionContainer))
        addSectionView(animeRolesSection.createView(inflater, binding.staffSectionContainer))
        addSectionView(mediaRolesSection.createView(inflater, binding.staffSectionContainer))
        addSectionView(staffRolesSection.createView(inflater, binding.staffSectionContainer))

        binding.staffSectionTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectSection(Section.entries[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) {
                selectSection(Section.entries[tab.position])
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resources.getStringArray(R.array.staff_page_titles).forEach { title ->
            binding.staffSectionTabs.addTab(binding.staffSectionTabs.newTab().setText(title))
        }
        binding.staffSectionTabs.getTabAt(selectedSection.ordinal)?.select()
        observeStaff()
        observeOverview()
        observeAnimeRoles()
        observeMediaRoles()
        observeStaffRoles()
        animeRolesSection.restoreState(savedInstanceState, "staff.anime_roles")
        mediaRolesSection.restoreState(savedInstanceState, "staff.media_roles")
        staffRolesSection.restoreState(savedInstanceState, "staff.staff_roles")
        selectSection(selectedSection)
    }

    private fun observeStaff() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    staffViewModel.state.collect { state ->
                        if (state is StaffViewModel.UiState.Success) {
                            model = state.staff
                            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = state.staff.name
                        } else if (state is StaffViewModel.UiState.Error) {
                            NotifyUtil.makeText(requireContext(), state.message, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    combine(
                        staffViewModel.favouriteFlag,
                        staffViewModel.favouriteLoading,
                    ) { flag, loading ->
                        FavouriteWidgetRenderState.fromFlag(
                            flag = flag,
                            fallbackIsFavourite = model?.isFavourite ?: false,
                            isLoading = loading,
                        )
                    }.collect { favouriteWidget?.render(it) }
                }
            }
        }
    }

    private fun observeOverview() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                overviewViewModel.state.collect { state ->
                    when (state) {
                        is StaffOverviewViewModel.UiState.Loading -> overviewSection.renderLoading()
                        is StaffOverviewViewModel.UiState.Success -> overviewSection.render(state.staff)
                        is StaffOverviewViewModel.UiState.Error -> overviewSection.renderError(state.message)
                    }
                }
            }
        }
    }

    private fun observeAnimeRoles() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                animeRolesViewModel.state.collect { state ->
                    when (state) {
                        is MediaAnimeRoleViewModel.UiState.Loading -> animeRolesSection.renderLoading()
                        is MediaAnimeRoleViewModel.UiState.Success -> {
                            val page = state.content.connection
                            animeRolesSection.render(
                                if (page.isEmpty) emptyList() else GroupingUtil.groupCharactersByYear(page.edges, null),
                                page.pageInfo,
                                page.isEmpty,
                            )
                        }
                        is MediaAnimeRoleViewModel.UiState.Error -> animeRolesSection.renderError(state.message)
                    }
                }
            }
        }
    }

    private fun observeMediaRoles() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaRolesViewModel.state.collect { state ->
                    if (selectedSection != Section.MEDIA_ROLES) return@collect
                    when (state) {
                        is MediaFormatViewModel.UiState.Loading -> mediaRolesSection.renderLoading()
                        is MediaFormatViewModel.UiState.Success -> mediaRolesSection.render(state.items, state.pageInfo, state.isEmpty)
                        is MediaFormatViewModel.UiState.Error -> mediaRolesSection.renderError(state.message)
                    }
                }
            }
        }
    }

    private fun observeStaffRoles() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                staffRolesViewModel.state.collect { state ->
                    when (state) {
                        is MediaStaffRoleViewModel.UiState.Loading -> staffRolesSection.renderLoading()
                        is MediaStaffRoleViewModel.UiState.Success -> {
                            val page = state.content.connection
                            staffRolesSection.render(
                                if (page.isEmpty) emptyList() else GroupingUtil.groupMediaByStaffRole(page.edges, null),
                                page.pageInfo,
                                page.isEmpty,
                            )
                        }
                        is MediaStaffRoleViewModel.UiState.Error -> staffRolesSection.renderError(state.message)
                    }
                }
            }
        }
    }

    private fun selectSection(section: Section) {
        if (!::overviewSection.isInitialized) return
        selectedSection = section
        for (index in 0 until binding.staffSectionContainer.childCount) {
            binding.staffSectionContainer.getChildAt(index).visibility =
                if (index == section.ordinal) View.VISIBLE else View.GONE
        }
        when (section) {
            Section.OVERVIEW -> overviewViewModel.load(staffId)
            Section.ANIME_ROLES -> animeRolesSection.select()
            Section.MEDIA_ROLES -> mediaRolesSection.select()
            Section.STAFF_ROLES -> staffRolesSection.select()
        }
    }

    private fun addSectionView(view: View) {
        binding.staffSectionContainer.addView(view)
    }

    private fun createAnimeRolesSection(): DetailListSection = DetailListSection(
        context = requireContext(),
        adapter = StaffCharacterRolesAdapter(::openCharacter),
        onLoadPage = { page -> animeRolesViewModel.load(staffId, onList, page) },
    )

    private fun createMediaRolesSection(): DetailListSection = DetailListSection(
        context = requireContext(),
        adapter = StaffMediaAdapter(::openMedia, ::longPressMedia),
        onLoadPage = { page ->
            mediaRolesViewModel.load(
                id = staffId,
                onList = onList,
                mediaType = KeyUtil.MANGA,
                page = page,
                requestType = KeyUtil.STAFF_MEDIA_REQ,
            )
        },
    )

    private fun createStaffRolesSection(): DetailListSection = DetailListSection(
        context = requireContext(),
        adapter = StaffMediaAdapter(::openMedia, ::longPressMedia),
        onLoadPage = { page -> staffRolesViewModel.load(staffId, onList, page) },
    )

    private fun openCharacter(target: View, item: CharacterStaffBase) {
        navigateToCharacter(CharacterScreenParam(item.character.id))
    }

    private fun openMedia(target: View, media: MediaBase) {
        navigateToMedia(MediaScreenParam(media.id, media.type))
    }

    private fun longPressMedia(media: MediaBase) {
        if (!settings.isAuthenticated) {
            NotifyUtil.makeText(requireContext(), R.string.info_login_req, android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        mediaActionUtil = MediaActionUtil.Builder().setId(media.id).build(requireActivity())
        mediaActionUtil?.startSeriesAction()
    }

    private fun retryOverview() = overviewViewModel.load(staffId)

    override fun onStart() {
        super.onStart()
        staffViewModel.load(staffId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.staff_menu, menu)
        val favourite = menu.findItem(R.id.action_favourite)
        val filter = menu.findItem(R.id.action_filter)
        val onMyList = menu.findItem(R.id.action_on_my_list)
        favourite.isVisible = settings.isAuthenticated
        filter.isVisible = settings.isAuthenticated
        onMyList.isVisible = settings.isAuthenticated
        if (favourite.isVisible) {
            favouriteWidget = favourite.actionView as? FavouriteToolbarWidget
            favouriteWidget?.setOnToggleAction { staffViewModel.toggleFavouriteStaff(staffId) }
            favouriteWidget?.render(
                FavouriteWidgetRenderState.fromFlag(
                    staffViewModel.favouriteFlag.value,
                    model?.isFavourite ?: false,
                    staffViewModel.favouriteLoading.value,
                ),
            )
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                val current = model ?: return super.onOptionsItemSelected(item)
                val send = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), "%s - %s", current.name, current.siteUrl.orEmpty()))
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(send, getString(R.string.abc_shareactionprovider_share_with)))
                return true
            }
            R.id.action_on_my_list -> {
                val selectedIndex = when (onList) {
                    null -> 0
                    false -> 1
                    true -> 2
                }
                DialogUtil.createSelection(
                    requireContext(),
                    R.string.app_filter_on_list,
                    selectedIndex,
                    CompatUtil.getStringList(requireContext(), R.array.on_list_values),
                ) { dialog, _ ->
                    onList = when (dialog.selectedIndex) {
                        0 -> null
                        1 -> false
                        else -> true
                    }
                    refreshSelectedSection()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshSelectedSection() {
        when (selectedSection) {
            Section.OVERVIEW -> overviewViewModel.load(staffId)
            Section.ANIME_ROLES -> animeRolesSection.refresh()
            Section.MEDIA_ROLES -> mediaRolesSection.refresh()
            Section.STAFF_ROLES -> staffRolesSection.refresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SECTION, selectedSection.name)
        outState.putString(KEY_ON_LIST, onList?.toString())
        animeRolesSection.saveState(outState, "staff.anime_roles")
        mediaRolesSection.saveState(outState, "staff.media_roles")
        staffRolesSection.saveState(outState, "staff.staff_roles")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mediaActionUtil?.onDestroy()
        mediaActionUtil = null
        favouriteWidget?.setOnToggleAction(null)
        favouriteWidget = null
        overviewSection.destroyView()
        animeRolesSection.destroyView()
        mediaRolesSection.destroyView()
        staffRolesSection.destroyView()
        _binding = null
        super.onDestroyView()
    }
}
