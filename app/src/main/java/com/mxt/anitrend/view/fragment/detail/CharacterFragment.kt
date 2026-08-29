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
import com.mxt.anitrend.adapter.recycler.detail.CharacterActorsAdapter
import com.mxt.anitrend.adapter.recycler.detail.CharacterMediaAdapter
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetRenderState
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.FragmentCharacterBinding
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.navigation.extension.navigateToStaff
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GroupingUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.CharacterActorsViewModel
import com.mxt.anitrend.viewmodel.CharacterOverviewViewModel
import com.mxt.anitrend.viewmodel.CharacterViewModel
import com.mxt.anitrend.viewmodel.MediaFormatViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/**
 * Unified Character destination.
 *
 * The four former pager pages are local screen sections. Each section is a
 * view-only renderer backed by the parent Fragment's ViewModels. No child
 * FragmentManager or replacement pager is used.
 */
@Suppress("TooManyFunctions") // Lifecycle, navigation, and section rendering stay centralized.
class CharacterFragment : Fragment() {

    private enum class Section {
        OVERVIEW,
        ANIME_ROLES,
        MANGA_ROLES,
        ACTOR_ROLES,
    }

    /** Argument helpers for the character destination. */
    companion object {
        private const val KEY_SECTION = "character.section"

        /** Reads the character identity from typed or legacy arguments. */
        fun fromBundle(bundle: Bundle?): CharacterScreenParam? = resolve(
            typed = bundle?.screenParam<CharacterScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
        )

        @VisibleForTesting
        internal fun resolve(typed: CharacterScreenParam?, legacyId: Long): CharacterScreenParam? {
            typed?.let { return it.takeIf { param -> param.characterId > 0L } }
            return legacyId.takeIf { it > 0L }?.let(::CharacterScreenParam)
        }
    }

    private var _binding: FragmentCharacterBinding? = null
    private val binding get() = _binding!!

    private var characterId = 0L
    private var model: com.mxt.anitrend.domain.model.CharacterRecord? = null
    private var selectedSection = Section.OVERVIEW
    private var activeMediaSection = Section.ANIME_ROLES
    private var favouriteWidget: FavouriteToolbarWidget? = null
    private var mediaActionUtil: MediaActionUtil? = null

    private val settings: Settings by inject()
    private val characterViewModel: CharacterViewModel by viewModel()
    private val overviewViewModel: CharacterOverviewViewModel by viewModel()
    private val actorsViewModel: CharacterActorsViewModel by viewModel()
    private val mediaFormatViewModel: MediaFormatViewModel by viewModel()

    private lateinit var overviewSection: CharacterOverviewSection
    private lateinit var animeSection: DetailListSection
    private lateinit var mangaSection: DetailListSection
    private lateinit var actorsSection: DetailListSection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterId = fromBundle(arguments)?.characterId ?: 0L
        selectedSection = savedInstanceState?.getString(KEY_SECTION)
            ?.let { runCatching { Section.valueOf(it) }.getOrNull() }
            ?: Section.OVERVIEW
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCharacterBinding.inflate(inflater, container, false)
        overviewSection = CharacterOverviewSection(::retryOverview)
        animeSection = createMediaSection(Section.ANIME_ROLES, KeyUtil.ANIME)
        mangaSection = createMediaSection(Section.MANGA_ROLES, KeyUtil.MANGA)
        actorsSection = createActorsSection()

        addSectionView(overviewSection.createView(inflater, binding.characterSectionContainer))
        addSectionView(animeSection.createView(inflater, binding.characterSectionContainer))
        addSectionView(mangaSection.createView(inflater, binding.characterSectionContainer))
        addSectionView(actorsSection.createView(inflater, binding.characterSectionContainer))

        binding.characterSectionTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
        val titles = resources.getStringArray(R.array.character_page_titles)
        titles.forEach { binding.characterSectionTabs.addTab(binding.characterSectionTabs.newTab().setText(it)) }
        binding.characterSectionTabs.getTabAt(selectedSection.ordinal)?.select()
        observeCharacter()
        observeOverview()
        observeMedia()
        observeActors()
        animeSection.restoreState(savedInstanceState, "character.anime")
        mangaSection.restoreState(savedInstanceState, "character.manga")
        actorsSection.restoreState(savedInstanceState, "character.actors")
        selectSection(selectedSection)
    }

    private fun observeCharacter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    characterViewModel.state.collect { state ->
                        when (state) {
                            is CharacterViewModel.UiState.Loading -> Unit
                            is CharacterViewModel.UiState.Success -> {
                                model = state.character
                                (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = state.character.name
                            }
                            is CharacterViewModel.UiState.Error -> {
                                NotifyUtil.makeText(requireContext(), state.message, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    combine(
                        characterViewModel.favouriteFlag,
                        characterViewModel.favouriteLoading,
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
                        is CharacterOverviewViewModel.UiState.Loading -> overviewSection.renderLoading()
                        is CharacterOverviewViewModel.UiState.Success -> overviewSection.render(state.character)
                        is CharacterOverviewViewModel.UiState.Error -> overviewSection.renderError(state.message)
                    }
                }
            }
        }
    }

    private fun observeMedia() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaFormatViewModel.state.collect { state ->
                    when (state) {
                        is MediaFormatViewModel.UiState.Loading -> {
                            if (selectedSection == activeMediaSection) {
                                if (activeMediaSection == Section.ANIME_ROLES) animeSection.renderLoading()
                                if (activeMediaSection == Section.MANGA_ROLES) mangaSection.renderLoading()
                            }
                        }
                        is MediaFormatViewModel.UiState.Success -> {
                            if (selectedSection == activeMediaSection) {
                                val section = if (activeMediaSection == Section.MANGA_ROLES) mangaSection else animeSection
                                section.render(state.items, state.pageInfo, state.isEmpty)
                            }
                        }
                        is MediaFormatViewModel.UiState.Error -> {
                            if (selectedSection == activeMediaSection) {
                                if (activeMediaSection == Section.ANIME_ROLES) animeSection.renderError(state.message)
                                if (activeMediaSection == Section.MANGA_ROLES) mangaSection.renderError(state.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeActors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                actorsViewModel.state.collect { state ->
                    when (state) {
                        is CharacterActorsViewModel.UiState.Loading -> actorsSection.renderLoading()
                        is CharacterActorsViewModel.UiState.Success -> {
                            if (state.content.isEmpty) {
                                actorsSection.render(emptyList(), null, true)
                            } else {
                                val page = state.content.connection
                                actorsSection.render(
                                    GroupingUtil.groupActorMediaEdge(page.edges),
                                    page.pageInfo,
                                    page.isEmpty,
                                )
                            }
                        }
                        is CharacterActorsViewModel.UiState.Error -> actorsSection.renderError(state.message)
                    }
                }
            }
        }
    }

    private fun selectSection(section: Section) {
        if (!::overviewSection.isInitialized) return
        selectedSection = section
        for (index in 0 until binding.characterSectionContainer.childCount) {
            binding.characterSectionContainer.getChildAt(index).visibility =
                if (index == section.ordinal) View.VISIBLE else View.GONE
        }
        when (section) {
            Section.OVERVIEW -> {
                overviewViewModel.load(characterId)
            }
            Section.ANIME_ROLES -> animeSection.select()
            Section.MANGA_ROLES -> mangaSection.select()
            Section.ACTOR_ROLES -> actorsSection.select()
        }
    }

    private fun addSectionView(view: View) {
        binding.characterSectionContainer.addView(view)
    }

    private fun createMediaSection(section: Section, mediaType: String): DetailListSection {
        val adapter = CharacterMediaAdapter(
            onMediaClick = ::openMedia,
            onMediaLongClick = ::longPressMedia,
        )
        return DetailListSection(
            context = requireContext(),
            adapter = adapter,
            onLoadPage = { page ->
                activeMediaSection = section
                mediaFormatViewModel.load(
                    id = characterId,
                    onList = null,
                    mediaType = mediaType,
                    page = page,
                    requestType = KeyUtil.CHARACTER_MEDIA_REQ,
                )
            },
        )
    }

    private fun createActorsSection(): DetailListSection {
        val adapter = CharacterActorsAdapter(
            onMediaClick = object : ItemClickListener<RecyclerItem> {
                override fun onItemClick(target: View, data: IndexedValue<RecyclerItem>) {
                    val media = data.value as? MediaBase ?: return
                    if (target.id == R.id.container) openMedia(target, media)
                }

                override fun onItemLongClick(target: View, data: IndexedValue<RecyclerItem>) {
                    val media = data.value as? MediaBase ?: return
                    if (target.id == R.id.container) longPressMedia(media)
                }
            },
            onStaffClick = ::openStaff,
        )
        return DetailListSection(
            context = requireContext(),
            adapter = adapter,
            onLoadPage = { page -> actorsViewModel.load(characterId, page) },
        )
    }

    private fun openMedia(target: View, media: MediaBase) {
        navigateToMedia(MediaScreenParam(media.id, media.type))
    }

    private fun openStaff(target: View, staff: StaffBase) {
        navigateToStaff(StaffScreenParam(staff.id))
    }

    private fun longPressMedia(media: MediaBase) {
        if (!settings.isAuthenticated) {
            NotifyUtil.makeText(requireContext(), R.string.info_login_req, android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        mediaActionUtil = MediaActionUtil.Builder().setId(media.id).build(requireActivity())
        mediaActionUtil?.startSeriesAction()
    }

    private fun retryOverview() = overviewViewModel.load(characterId)

    override fun onStart() {
        super.onStart()
        characterViewModel.load(characterId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.custom_menu, menu)
        val favourite = menu.findItem(R.id.action_favourite)
        favourite.isVisible = settings.isAuthenticated
        if (favourite.isVisible) {
            favouriteWidget = favourite.actionView as? FavouriteToolbarWidget
            favouriteWidget?.setOnToggleAction { characterViewModel.toggleFavouriteCharacter(characterId) }
            favouriteWidget?.render(
                FavouriteWidgetRenderState.fromFlag(
                    characterViewModel.favouriteFlag.value,
                    model?.isFavourite ?: false,
                    characterViewModel.favouriteLoading.value,
                ),
            )
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_share) {
            val current = model ?: return super.onOptionsItemSelected(item)
            val send = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), "%s - %s", current.name.orEmpty(), current.siteUrl.orEmpty()))
                type = "text/plain"
            }
            startActivity(Intent.createChooser(send, getString(R.string.abc_shareactionprovider_share_with)))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_SECTION, selectedSection.name)
        animeSection.saveState(outState, "character.anime")
        mangaSection.saveState(outState, "character.manga")
        actorsSection.saveState(outState, "character.actors")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mediaActionUtil?.onDestroy()
        mediaActionUtil = null
        favouriteWidget?.setOnToggleAction(null)
        favouriteWidget = null
        overviewSection.destroyView()
        animeSection.destroyView()
        mangaSection.destroyView()
        actorsSection.destroyView()
        _binding = null
        super.onDestroyView()
    }
}
