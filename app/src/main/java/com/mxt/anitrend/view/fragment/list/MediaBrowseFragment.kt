package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.collection.GenreTagUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaBrowseUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.sheet.BottomSheetMediaFilter
import com.mxt.anitrend.view.sheet.MediaFilterSheetResult
import com.mxt.anitrend.viewmodel.MediaBrowseViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import java.util.UUID

/** Null-sentinel option values for the browse format filters, mirroring the legacy dialog arrays. */
private val ANIME_FORMATS: Array<String?> =
    arrayOf(null, KeyUtil.TV, KeyUtil.TV_SHORT, KeyUtil.MOVIE, KeyUtil.SPECIAL, KeyUtil.OVA, KeyUtil.ONA, KeyUtil.MUSIC)

private val MANGA_FORMATS: Array<String?> =
    arrayOf(null, KeyUtil.MANGA, KeyUtil.NOVEL, KeyUtil.ONE_SHOT)

/** Null-sentinel option values for the browse status filter, mirroring the legacy dialog array. */
private val MEDIA_STATUSES: Array<String?> =
    arrayOf(null, KeyUtil.FINISHED, KeyUtil.RELEASING, KeyUtil.NOT_YET_RELEASED, KeyUtil.CANCELLED)

/** Shared ASC/DESC option values for the order filters across list fragments. */
internal val mediaFilterSortOrders: Array<String> = arrayOf(KeyUtil.ASC, KeyUtil.DESC)

/**
 * Maps a single-choice filter sheet result to the setting value that should be persisted.
 * APPLY with a selected option returns the option value (the leading null sentinel of the
 * format/status arrays is a real selection). APPLY without a selection reports no change.
 * RESET reports the filter's existing default value instead of inventing a new sentinel,
 * so hosts restore the same state the Settings getter falls back to.
 */
internal fun resolveSingleFilterValue(
    action: String,
    selectedIndex: Int,
    values: Array<out String?>,
    defaultValue: String?,
): Pair<Boolean, String?> = when (action) {
    MediaFilterSheetResult.ACTION_RESET -> true to defaultValue
    else -> {
        if (selectedIndex in values.indices) {
            true to values[selectedIndex]
        } else {
            false to null
        }
    }
}

/** Year-range variant of [resolveSingleFilterValue]; RESET restores the Settings getter default. */
internal fun resolveSingleFilterYear(
    action: String,
    selectedIndex: Int,
    years: List<Int>,
): Pair<Boolean, Int> = when (action) {
    MediaFilterSheetResult.ACTION_RESET -> true to DateUtil.getCurrentYear(1)
    else -> {
        val year = years.getOrNull(selectedIndex)
        if (year != null) true to year else false to 0
    }
}

/**
 * Decides whether a sheet result belongs to the host's current pending request.
 * Acceptance requires an active pending filter identity AND an exact request ID
 * match, so delayed or duplicate results from an earlier invocation are rejected
 * without disturbing the pending operation.
 */
internal fun shouldAcceptFilterResult(
    pendingFilterKind: String?,
    pendingRequestId: String?,
    result: MediaFilterSheetResult,
): Boolean = pendingFilterKind != null && pendingRequestId != null && result.requestId == pendingRequestId

/**
 * Created by max on 2018/02/03.
 * Multi purpose media browse fragment
 */
open class MediaBrowseFragment : FragmentBaseList<MediaBase, PageContainer<MediaBase>>() {
    protected lateinit var requestArgs: Bundle
    private var mediaBrowseUtil: MediaBrowseUtil? = null

    private val settings: Settings by inject()

    private val mediaBrowseViewModel: MediaBrowseViewModel by viewModel()

    /** Distinguishes which filter an open sheet result belongs to. */
    protected enum class BrowseFilterKind { SORT, ORDER, GENRES, TAGS, TYPE, YEAR, STATUS }

    private var pendingFilter: BrowseFilterKind? = null

    /** Opaque invocation ID of the sheet currently awaiting a result, paired with [pendingFilter]. */
    private var pendingRequestId: String? = null

    companion object {
        private const val STATE_PENDING_FILTER = "state_pending_filter"
        private const val STATE_PENDING_REQUEST_ID = "state_pending_request_id"
        private const val FILTER_SHEET_TAG = "media_filter_sheet"

        @JvmStatic
        fun newInstance(params: Bundle): MediaBrowseFragment {
            val args = Bundle(params)
            return MediaBrowseFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getString(STATE_PENDING_FILTER)?.let { name ->
            pendingFilter = runCatching { BrowseFilterKind.valueOf(name) }.getOrNull()
        }
        pendingRequestId = savedInstanceState?.getString(STATE_PENDING_REQUEST_ID)
        childFragmentManager.setFragmentResultListener(
            BottomSheetMediaFilter.RESULT_KEY,
            this,
        ) { _, bundle ->
            val result =
                bundle.parcelable<MediaFilterSheetResult>(BottomSheetMediaFilter.RESULT_BUNDLE_KEY)
                    ?: return@setFragmentResultListener
            applyFilterResult(result)
        }
        requestArgs =
            Bundle(arguments ?: Bundle()).apply {
                if (!containsKey(KeyUtil.arg_page_limit)) {
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                }
            }
        mediaBrowseUtil = arguments?.parcelable(KeyUtil.arg_media_util)

        val browseUtil = mediaBrowseUtil ?: MediaBrowseUtil(true)
        mediaBrowseUtil = browseUtil

        isPager = true
        isFilterableEnabled = browseUtil.isFilterEnabled

        val ctx = requireContext()
        mAdapter = MediaAdapter(ctx, browseUtil.isCompactType)

        mColumnSize =
            if (browseUtil.isCompactType) {
                R.integer.grid_giphy_x3
            } else {
                if (settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                    R.integer.single_list_x1
                } else {
                    R.integer.grid_list_x2
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaBrowseViewModel.state.collect { state ->
                    when (state) {
                        is MediaBrowseViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaBrowseViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaBrowseViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PENDING_FILTER, pendingFilter?.name)
        outState.putString(STATE_PENDING_REQUEST_ID, pendingRequestId)
    }

    /**
     * Applies a committed sheet result to the filter that opened the sheet.
     * A result is applied only when an active pending filter matches the exact
     * request ID; mismatched delayed/duplicate results are ignored without
     * clearing the pending operation. CANCEL never mutates settings.
     */
    private fun applyFilterResult(result: MediaFilterSheetResult) {
        val active = pendingFilter ?: return
        if (!shouldAcceptFilterResult(active.name, pendingRequestId, result)) return
        pendingFilter = null
        pendingRequestId = null
        if (result.action == MediaFilterSheetResult.ACTION_CANCEL) return
        val selectedIndex = result.selectedIndices.firstOrNull() ?: -1
        when (active) {
            BrowseFilterKind.SORT -> {
                val (changed, value) = resolveSingleFilterValue(
                    result.action,
                    selectedIndex,
                    KeyUtil.MediaSortType,
                    KeyUtil.POPULARITY,
                )
                if (changed) settings.mediaSort = value
            }
            BrowseFilterKind.ORDER -> {
                val (changed, value) = resolveSingleFilterValue(
                    result.action,
                    selectedIndex,
                    mediaFilterSortOrders,
                    KeyUtil.DESC,
                )
                if (changed && value != null) settings.saveSortOrder(value)
            }
            BrowseFilterKind.GENRES -> {
                val genres = mediaBrowseViewModel.genreCollection
                if (genres.isNotEmpty()) {
                    settings.selectedGenres =
                        GenreTagUtil.createGenreSelectionMap(genres, result.selectedIndices.toTypedArray())
                }
            }
            BrowseFilterKind.TAGS -> {
                val tagList = mediaBrowseViewModel.mediaTags
                if (tagList.isNotEmpty()) {
                    settings.selectedTags =
                        GenreTagUtil.createTagSelectionMap(tagList, result.selectedIndices.toTypedArray())
                }
            }
            BrowseFilterKind.TYPE -> {
                val isAnime = CompatUtil.equals(requestArgs.getString(KeyUtil.arg_mediaType), KeyUtil.ANIME)
                val formats = if (isAnime) ANIME_FORMATS else MANGA_FORMATS
                val (changed, value) = resolveSingleFilterValue(result.action, selectedIndex, formats, null)
                if (changed) {
                    if (isAnime) settings.animeFormat = value else settings.mangaFormat = value
                }
            }
            BrowseFilterKind.YEAR -> {
                val (changed, year) = resolveSingleFilterYear(
                    result.action,
                    selectedIndex,
                    DateUtil.getYearRanges(1950, 1),
                )
                if (changed) settings.saveSeasonYear(year)
            }
            BrowseFilterKind.STATUS -> {
                val (changed, value) = resolveSingleFilterValue(result.action, selectedIndex, MEDIA_STATUSES, null)
                if (changed) settings.mediaStatus = value
            }
        }
    }

    /**
     * Shows the M3 selection sheet for the given filter and tracks it as the pending
     * result owner. A new sheet is refused while a pending request is still active so
     * results cannot be cross-correlated between overlapping invocations.
     */
    protected fun showFilterSheet(
        kind: BrowseFilterKind,
        title: Int,
        options: List<String>,
        selectedIndices: Collection<Int>,
        multiSelect: Boolean,
    ) {
        if (pendingFilter != null) return
        val requestId = UUID.randomUUID().toString()
        pendingFilter = kind
        pendingRequestId = requestId
        BottomSheetMediaFilter
            .newInstance(title, options, selectedIndices, multiSelect, requestId)
            .show(childFragmentManager, FILTER_SHEET_TAG)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        if (mediaBrowseUtil?.isBasicFilter == true) {
            menu.findItem(R.id.action_type).isVisible = false
            menu.findItem(R.id.action_year).isVisible = false
            menu.findItem(R.id.action_status).isVisible = false
            menu.findItem(R.id.action_genre).isVisible = false
            menu.findItem(R.id.action_tag).isVisible = false
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                showFilterSheet(
                    BrowseFilterKind.SORT,
                    R.string.app_filter_sort,
                    CompatUtil.capitalizeWords(KeyUtil.MediaSortType),
                    listOf(CompatUtil.getIndexOf(KeyUtil.MediaSortType, settings.mediaSort)),
                    multiSelect = false,
                )
                return true
            }
            R.id.action_order -> {
                showFilterSheet(
                    BrowseFilterKind.ORDER,
                    R.string.app_filter_order,
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                    listOf(CompatUtil.getIndexOf(mediaFilterSortOrders, settings.sortOrder)),
                    multiSelect = false,
                )
                return true
            }
            R.id.action_genre -> {
                val genres: List<Genre> = mediaBrowseViewModel.genreCollection
                if (CompatUtil.isEmpty(genres)) {
                    NotifyUtil
                        .makeText(
                            ctx,
                            R.string.app_splash_loading,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    showFilterSheet(
                        BrowseFilterKind.GENRES,
                        R.string.app_filter_genres,
                        genres.map { it.genre.orEmpty() },
                        settings.selectedGenres.orEmpty().keys,
                        multiSelect = true,
                    )
                }
                return true
            }
            R.id.action_tag -> {
                val tagList: List<MediaTag> = mediaBrowseViewModel.mediaTags
                if (CompatUtil.isEmpty(tagList)) {
                    NotifyUtil
                        .makeText(
                            ctx,
                            R.string.app_splash_loading,
                            R.drawable.ic_warning_white_18dp,
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    showFilterSheet(
                        BrowseFilterKind.TAGS,
                        R.string.app_filter_tags,
                        tagList.map { it.name.orEmpty() },
                        settings.selectedTags.orEmpty().keys,
                        multiSelect = true,
                    )
                }
                return true
            }
            R.id.action_type -> {
                val isAnime = CompatUtil.equals(requestArgs.getString(KeyUtil.arg_mediaType), KeyUtil.ANIME)
                showFilterSheet(
                    BrowseFilterKind.TYPE,
                    R.string.app_filter_show_type,
                    CompatUtil.getStringList(ctx, if (isAnime) R.array.anime_formats else R.array.manga_formats),
                    listOf(
                        CompatUtil.getIndexOf(
                            if (isAnime) ANIME_FORMATS else MANGA_FORMATS,
                            if (isAnime) settings.animeFormat else settings.mangaFormat,
                        ),
                    ),
                    multiSelect = false,
                )
                return true
            }
            R.id.action_year -> {
                val yearRanges = DateUtil.getYearRanges(1950, 1)
                showFilterSheet(
                    BrowseFilterKind.YEAR,
                    R.string.app_filter_year,
                    yearRanges.map { it.toString() },
                    listOf(CompatUtil.getIndexOf(yearRanges, settings.seasonYear)),
                    multiSelect = false,
                )
                return true
            }
            R.id.action_status -> {
                showFilterSheet(
                    BrowseFilterKind.STATUS,
                    R.string.anime,
                    CompatUtil.getStringList(ctx, R.array.media_status),
                    listOf(CompatUtil.getIndexOf(MEDIA_STATUSES, settings.mediaStatus)),
                    multiSelect = false,
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        injectAdapter()
    }

    override fun makeRequest() {
        // All filter reads go through NavigationArgs so the exact containsKey /
        // null-versus-empty semantics are captured in production helpers and are
        // JVM-testable. The browse destination has no stable identity of its own;
        // every value stays on the legacy bundle channel until a Phase 2+ browse
        // configuration model is introduced.
        val type = NavigationArgs.resolveMediaType(requestArgs.getString(KeyUtil.arg_mediaType))
        val isAdult: Boolean? =
            if (!settings.displayAdultContent) {
                false
            } else {
                NavigationArgs.optionalBoolean(requestArgs.containsKey(KeyUtil.arg_isAdult), requestArgs.getBoolean(KeyUtil.arg_isAdult))
            }

        // Bundle values are explicit caller intent. Settings only provide fallback defaults
        // when filtering is enabled and the caller did not provide that query value.
        val season =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_season), requestArgs.getString(KeyUtil.arg_season))
        var format =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_format), requestArgs.getString(KeyUtil.arg_format))
        var seasonYear =
            NavigationArgs.optionalInt(requestArgs.containsKey(KeyUtil.arg_seasonYear), requestArgs.getInt(KeyUtil.arg_seasonYear))
        var startDateLike =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_startDateLike), requestArgs.getString(KeyUtil.arg_startDateLike))
        var status =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_status), requestArgs.getString(KeyUtil.arg_status))
        var genres: List<String>? =
            NavigationArgs.optionalStringList(requestArgs.containsKey(KeyUtil.arg_genres), requestArgs.getStringArrayList(KeyUtil.arg_genres))
        var tags: List<String>? =
            NavigationArgs.optionalStringList(requestArgs.containsKey(KeyUtil.arg_tags), requestArgs.getStringArrayList(KeyUtil.arg_tags))
        var sort =
            NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_sort), requestArgs.getString(KeyUtil.arg_sort))

        if (isFilterableEnabled) {
            if (mediaBrowseUtil?.isBasicFilter != true) {
                if (CompatUtil.equals(requestArgs.getString(KeyUtil.arg_mediaType), KeyUtil.MANGA)) {
                    if (!requestArgs.containsKey(KeyUtil.arg_startDateLike)) {
                        startDateLike = String.format(Locale.getDefault(), "%d%%", settings.seasonYear)
                    }
                    if (!requestArgs.containsKey(KeyUtil.arg_format)) {
                        format = settings.mangaFormat
                    }
                } else {
                    if (!requestArgs.containsKey(KeyUtil.arg_seasonYear)) {
                        seasonYear = settings.seasonYear
                    }
                    if (!requestArgs.containsKey(KeyUtil.arg_format)) {
                        format = settings.animeFormat
                    }
                }
                if (!requestArgs.containsKey(KeyUtil.arg_status)) {
                    status = settings.mediaStatus
                }
                if (!requestArgs.containsKey(KeyUtil.arg_genres)) {
                    genres = ArrayList(GenreTagUtil.getMappedValues(settings.selectedGenres).orEmpty())
                }
                if (!requestArgs.containsKey(KeyUtil.arg_tags)) {
                    tags = ArrayList(GenreTagUtil.getMappedValues(settings.selectedTags).orEmpty())
                }
            }
            if (!requestArgs.containsKey(KeyUtil.arg_sort)) {
                sort = settings.mediaSort + settings.sortOrder
            }
        }

        mediaBrowseViewModel.load(
            type = type,
            page = mScrollListener.currentPage,
            pageLimit = NavigationArgs.intWithDefault(requestArgs.containsKey(KeyUtil.arg_page_limit), requestArgs.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
            season = season,
            sort = sort,
            isAdult = isAdult,
            format = format,
            seasonYear = seasonYear,
            startDateLike = startDateLike,
            status = status,
            genres = genres,
            tags = tags,
        )
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<MediaBase>?) = Unit

    private fun handleSuccess(value: PageContainer<MediaBase>) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        // The ViewModel always emits the complete accumulated deduplicated snapshot
        // (page one, page two, and store recombination alike), so every success
        // replaces the adapter contents. Appending would duplicate every item that
        // was already rendered on a previous emission.
        mAdapter.onItemsInserted(value.pageData)
        updateUI()
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<MediaBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                val host = activity ?: return
                val intent = MediaActivity.newIntent(host, data.value.id, data.value.type)
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaBase>,
    ) {
        when (target.id) {
            R.id.container -> {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.value.id)
                            .build(host)
                    mediaActionUtil.startSeriesAction()
                } else {
                    context?.let {
                        NotifyUtil
                            .makeText(
                                it,
                                R.string.info_login_req,
                                R.drawable.ic_group_add_grey_600_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            }
        }
    }
}
