package com.mxt.anitrend.view.fragment.list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.medialist.MediaListGrid
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.view.sheet.BottomSheetMediaFilter
import com.mxt.anitrend.view.sheet.BottomSheetSeriesManage
import com.mxt.anitrend.view.sheet.MediaFilterSheetResult
import com.mxt.anitrend.viewmodel.MediaListDisplayState
import com.mxt.anitrend.viewmodel.MediaListMutationViewModel
import com.mxt.anitrend.viewmodel.MediaListViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

/**
 * Created by max on 2017/12/18.
 * media list fragment
 *
 * Route origin of the media list destination. [MediaListOrigin.ROOT] is the
 * drawer My Anime/My Manga contract; every other producer (profile stats,
 * user-list deep links, media-list shortcuts, the ROUTE_MEDIA_LIST ingress)
 * pushes the destination with [MediaListOrigin.PUSHED] so caller-back
 * semantics survive (NFR-002).
 */
enum class MediaListOrigin { ROOT, PUSHED }

open class MediaListFragment : FragmentBaseList<MediaListItemUiModel, MediaListCollectionPageResult>() {

    protected var userId: Long = 0
    protected var userName: String? = null

    @KeyUtil.MediaType
    protected var mediaType: String? = null
    protected var statusIn: String? = null

    private var isUnifiedDestination = false

    private val settings: Settings by inject()

    private val mediaListViewModel: MediaListViewModel by viewModel()
    private val mediaListMutationViewModel: MediaListMutationViewModel by viewModel()

    protected open val display: StateFlow<MediaListDisplayState?>
        get() = mediaListViewModel.display

    protected open fun onSearchQueryChanged(searchQuery: String?) {
        mediaListViewModel.onSearchQueryChanged(searchQuery)
    }

    /** Distinguishes which filter an open sheet result belongs to. */
    private enum class MediaListFilterKind { SORT, ORDER }

    private var pendingFilter: MediaListFilterKind? = null
    private var pendingFilterRequestId: String? = null

    protected var mediaListStyle: Int = KeyUtil.LIST_VIEW_STYLE_DEFAULT
    private val renderedItems = mutableStateOf(emptyList<MediaListItemUiModel>())
    private val mediaListState = LazyGridState()
    private var composeView: ComposeView? = null
    private var latestEntries: List<MediaListRecord> = emptyList()
    private var pendingViewportAnchor: PendingViewportAnchor? = null
    private val pendingViewportRestore = mutableStateOf<PendingViewportAnchor?>(null)

    private data class PendingViewportAnchor(
        val anchorMediaId: Long,
        val offset: Int,
        val targetMediaId: Long,
        val targetBaselineRevision: Long,
        val predecessorMediaIds: List<Long>,
    )

    companion object {
        private const val STATE_PENDING_FILTER = "state_pending_filter"
        private const val STATE_PENDING_REQUEST_ID = "state_pending_request_id"
        private const val STATE_STATUS = "state_media_list_status"
        private const val FILTER_SHEET_TAG = "media_filter_sheet"

        /**
         * Legacy unified-destination flag written for root and pushed routes
         * alike. It gates the status filter menu visibility and is NOT the
         * route-origin contract; see [MediaListOrigin] and [ARG_MEDIA_LIST_ORIGIN].
         */
        const val ARG_UNIFIED_DESTINATION = "navigation_media_list_unified"

        /**
         * Wire key for the route-origin contract ([MediaListOrigin]). Written by
         * the destination helpers and read by the host's top-level/back policy.
         * Absent or unknown values resolve to [MediaListOrigin.PUSHED].
         */
        const val ARG_MEDIA_LIST_ORIGIN = "navigation_media_list_origin"

        @JvmStatic
        fun newInstance(params: Bundle): MediaListFragment {
            val args = Bundle(params)
            return MediaListFragment().apply {
                arguments = args
            }
        }

        /**
         * The typed user parameter is preferred. Legacy wire extras remain supported
         * for the top-level pager compatibility path and restored older intents.
         */
        fun fromBundle(bundle: Bundle?): UserScreenParam? = resolve(
            typed = bundle?.screenParam<UserScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyName = bundle?.getString(KeyUtil.arg_userName),
        )

        @VisibleForTesting
        internal fun resolve(
            typed: UserScreenParam?,
            legacyId: Long,
            legacyName: String?,
        ): UserScreenParam = typed ?: UserScreenParam(userId = legacyId, initialName = legacyName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getString(STATE_PENDING_FILTER)?.let { name ->
            pendingFilter = runCatching { MediaListFilterKind.valueOf(name) }.getOrNull()
        }
        pendingFilterRequestId = savedInstanceState?.getString(STATE_PENDING_REQUEST_ID)
        childFragmentManager.setFragmentResultListener(
            BottomSheetMediaFilter.RESULT_KEY,
            this,
        ) { _, bundle ->
            val result =
                bundle.parcelable<MediaFilterSheetResult>(BottomSheetMediaFilter.RESULT_BUNDLE_KEY)
            if (result != null) {
                applyFilterResult(result)
            }
        }
        requireActivity().supportFragmentManager.setFragmentResultListener(
            BottomSheetSeriesManage.MEDIA_LIST_MUTATION_STARTED_RESULT_KEY,
            this,
        ) { _, bundle ->
            bundle.getLong(BottomSheetSeriesManage.MEDIA_LIST_MUTATION_STARTED_MEDIA_ID)
                .takeIf { it > 0L }
                ?.let(::captureViewportAnchor)
        }
        requireActivity().supportFragmentManager.setFragmentResultListener(
            BottomSheetSeriesManage.MEDIA_LIST_MUTATION_FAILED_RESULT_KEY,
            this,
        ) { _, bundle ->
            val mediaId = bundle.getLong(BottomSheetSeriesManage.MEDIA_LIST_MUTATION_FAILED_MEDIA_ID)
            if (pendingViewportAnchor?.targetMediaId == mediaId) {
                pendingViewportAnchor = null
                pendingViewportRestore.value = null
            }
        }
        fromBundle(arguments)?.let { args ->
            userId = args.userId
            userName = args.initialName
        }
        isUnifiedDestination = arguments?.getBoolean(ARG_UNIFIED_DESTINATION) == true
        arguments?.let { args ->
            statusIn = savedInstanceState?.getString(STATE_STATUS) ?: args.getString(KeyUtil.arg_statusIn)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        updateScreenTitle()

        isFilterableEnabled = true
        isPager = false
        mediaListStyle = settings.mediaListStyle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachMediaListComposeHost()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    display.collect { snapshot ->
                        if (snapshot != null) {
                            submitStateList(snapshot.rows, snapshot.entries)
                        }
                    }
                }
                launch {
                    mediaListViewModel.state.collect { state ->
                        when (state) {
                            is MediaListViewModel.UiState.Loading -> {
                                // Loading is handled by swipeRefreshLayout in the base class
                            }
                            is MediaListViewModel.UiState.Success -> {
                                handleSuccess(state)
                            }
                            is MediaListViewModel.UiState.Error -> {
                                showError(state.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun attachMediaListComposeHost() {
        val host = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme {
                    val items = renderedItems.value
                    val restoreAnchor = pendingViewportRestore.value
                    MediaListGrid(
                        items = items,
                        style = mediaListStyle,
                        listState = mediaListState,
                        onIncrement = ::incrementMediaProgress,
                        onIncrementBusy = ::showIncrementBusy,
                        onIncrementLimit = ::showIncrementLimit,
                        onOpenMedia = ::openMedia,
                        onOpenManage = ::openManage,
                    )
                    LaunchedEffect(items, restoreAnchor) {
                        if (restoreAnchor != null) {
                            restoreViewportAnchor(restoreAnchor)
                        }
                    }
                }
            }
        }
        composeView = host
        swipeRefreshLayout.addView(
            host,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        swipeRefreshLayout.scrollBoundary = { direction ->
            if (direction < 0) mediaListState.canScrollBackward else mediaListState.canScrollForward
        }
    }

    private fun showIncrementBusy() {
        val context = context ?: return
        NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
    }

    private fun showIncrementLimit() {
        val context = context ?: return
        NotifyUtil
            .makeText(
                context,
                if (mediaType == KeyUtil.ANIME) {
                    R.string.text_unable_to_increment_episodes
                } else {
                    R.string.text_unable_to_increment_chapters
                },
                R.drawable.ic_warning_white_18dp,
                Toast.LENGTH_SHORT,
            ).show()
    }

    override fun onDestroyView() {
        swipeRefreshLayout.scrollBoundary = null
        composeView = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PENDING_FILTER, pendingFilter?.name)
        outState.putString(STATE_PENDING_REQUEST_ID, pendingFilterRequestId)
        outState.putString(STATE_STATUS, statusIn)
    }

    private fun applyFilterResult(result: MediaFilterSheetResult) {
        val active = pendingFilter ?: return
        if (!shouldAcceptFilterResult(active.name, pendingFilterRequestId, result)) return
        pendingFilter = null
        pendingFilterRequestId = null
        if (result.action == MediaFilterSheetResult.ACTION_CANCEL) return
        val selectedIndex = result.selectedIndices.firstOrNull() ?: -1
        when (active) {
            MediaListFilterKind.SORT -> {
                val (changed, value) = resolveSingleFilterValue(
                    result.action,
                    selectedIndex,
                    KeyUtil.MediaListSortType,
                    KeyUtil.PROGRESS,
                )
                if (changed && value != null) settings.mediaListSort = value
            }
            MediaListFilterKind.ORDER -> {
                val (changed, value) = resolveSingleFilterValue(
                    result.action,
                    selectedIndex,
                    mediaFilterSortOrders,
                    KeyUtil.DESC,
                )
                if (changed && value != null) settings.saveSortOrder(value)
            }
        }
    }

    private fun showFilterSheet(
        kind: MediaListFilterKind,
        title: Int,
        options: List<String>,
        selectedIndices: Collection<Int>,
        multiSelect: Boolean,
    ) {
        if (pendingFilter != null) return
        val requestId = UUID.randomUUID().toString()
        pendingFilter = kind
        pendingFilterRequestId = requestId
        BottomSheetMediaFilter
            .newInstance(title, options, selectedIndices, multiSelect, requestId)
            .show(childFragmentManager, FILTER_SHEET_TAG)
    }

    fun applySearchQuery(searchQuery: String?) {
        onSearchQueryChanged(searchQuery)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_genre).isVisible = false
        menu.findItem(R.id.action_tag).isVisible = false
        menu.findItem(R.id.action_type).isVisible = false
        menu.findItem(R.id.action_year).isVisible = false
        menu.findItem(R.id.action_status).isVisible = isUnifiedDestination
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                showFilterSheet(
                    MediaListFilterKind.SORT,
                    R.string.app_filter_sort,
                    CompatUtil.capitalizeWords(KeyUtil.MediaListSortType),
                    listOf(CompatUtil.getIndexOf(KeyUtil.MediaListSortType, settings.mediaListSort)),
                    multiSelect = false,
                )
                return true
            }
            R.id.action_order -> {
                showFilterSheet(
                    MediaListFilterKind.ORDER,
                    R.string.app_filter_order,
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                    listOf(CompatUtil.getIndexOf(mediaFilterSortOrders, settings.sortOrder)),
                    multiSelect = false,
                )
                return true
            }
            R.id.action_status -> {
                showStatusSelector()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showStatusSelector() {
        val context = context ?: return
        val options = CompatUtil.capitalizeWords(KeyUtil.MediaListStatusValues)
        val selectedIndex = statusIn?.let { CompatUtil.getIndexOf(KeyUtil.MediaListStatusValues, it) } ?: -1
        AlertDialog.Builder(context)
            .setTitle(R.string.menu_title_status)
            .setSingleChoiceItems(options.toTypedArray(), selectedIndex) { dialog, which ->
                statusIn = KeyUtil.MediaListStatusValues[which]
                updateScreenTitle()
                dialog.dismiss()
                showLoading()
                onRefresh()
            }
            .setNegativeButton(R.string.Close, null)
            .show()
    }

    private fun updateScreenTitle() {
        val title = when {
            CompatUtil.equals(mediaType, KeyUtil.ANIME) -> R.string.title_anime_list
            CompatUtil.equals(mediaType, KeyUtil.MANGA) -> R.string.title_manga_list
            else -> R.string.title_activity_media_list
        }
        activity?.setTitle(title)
    }

    override fun updateUI() {
        // Content/empty is decided by the success handler from the unfiltered payload;
        // this method only ends refresh/loading and reveals the successful list surface.
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false)
        } else if (swipeRefreshLayout.isLoading()) {
            swipeRefreshLayout.setLoading(false)
        }
        showContent()
    }

    override fun makeRequest() {
        mediaListViewModel.load(userId, userName, mediaType, statusIn)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        if (key != null && isFilterableEnabled && GraphUtil.isKeyFilter(key)) {
            val mediaListSort = settings.mediaListSort ?: KeyUtil.PROGRESS
            if (CompatUtil.equals(key, Settings._mediaListSort) && MediaListUtil.isTitleSort(mediaListSort)) {
                swipeRefreshLayout.setRefreshing(true)
                mediaListViewModel.onSortPreferenceChanged()
            } else {
                showLoading()
                submitStateList(emptyList())
                onRefresh()
            }
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: MediaListCollectionPageResult?) = Unit

    private fun handleSuccess(state: MediaListViewModel.UiState.Success) {
        // Rows and entries arrive through the display collector as one snapshot;
        // this handler only owns the page info and content/empty surface decision.
        state.pageInfo?.let { pageState.setPageInfo(it.toPageInfo()) }
        if (state.renderedItems.isNotEmpty()) {
            updateUI()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    protected fun submitStateList(
        rendered: List<MediaListItemUiModel>,
        entries: List<MediaListRecord>? = null,
    ) {
        if (entries != null) {
            latestEntries = entries
        }
        renderedItems.value = rendered
        pendingViewportAnchor?.takeIf(::isMutationApplied)?.let { anchor ->
            pendingViewportRestore.value = anchor
        }
    }

    private fun captureViewportAnchor(targetMediaId: Long) {
        val position = mediaListState.firstVisibleItemIndex
        val item = renderedItems.value.getOrNull(position) ?: return
        pendingViewportAnchor = PendingViewportAnchor(
            anchorMediaId = item.mediaId,
            offset = mediaListState.firstVisibleItemScrollOffset,
            targetMediaId = targetMediaId,
            targetBaselineRevision = latestEntries.firstOrNull { it.mediaId == targetMediaId }?.revision ?: Long.MIN_VALUE,
            predecessorMediaIds = renderedItems.value.take(position).asReversed().map(MediaListItemUiModel::mediaId),
        )
    }

    private fun isMutationApplied(anchor: PendingViewportAnchor): Boolean {
        val targetEntry = latestEntries.firstOrNull { it.mediaId == anchor.targetMediaId }
        return targetEntry == null || targetEntry.revision > anchor.targetBaselineRevision
    }

    private suspend fun restoreViewportAnchor(anchor: PendingViewportAnchor) {
        if (pendingViewportAnchor != anchor || !isMutationApplied(anchor)) return
        val itemPosition = sequenceOf(anchor.anchorMediaId)
            .plus(anchor.predecessorMediaIds.asSequence())
            .map { mediaId -> renderedItems.value.indexOfFirst { it.mediaId == mediaId } }
            .firstOrNull { it >= 0 }
        mediaListState.scrollToItem(itemPosition ?: 0, if (itemPosition == null) 0 else anchor.offset)
        pendingViewportAnchor = null
        pendingViewportRestore.value = null
    }

    override fun onStart() {
        super.onStart()
        if (latestEntries.isEmpty()) {
            showLoading()
            onRefresh()
        } else {
            updateUI()
        }
    }

    private fun openMedia(item: MediaListItemUiModel) {
        navigateToMedia(MediaScreenParam(item.mediaId, item.mediaType))
    }

    private fun openManage(item: MediaListItemUiModel) {
        if (!settings.isAuthenticated) {
            context?.let {
                NotifyUtil
                    .makeText(
                        it,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
            return
        }

        val host = activity ?: return
        mediaActionUtil =
            MediaActionUtil
                .Builder()
                .setId(item.mediaId)
                .build(host)
        mediaActionUtil.startSeriesAction()
    }

    protected open fun incrementMediaProgress(item: MediaListItemUiModel) {
        val entry = latestEntries.firstOrNull { record ->
            record.id == item.id || record.mediaId == item.mediaId
        } ?: return
        dispatchIncrement(entry)
    }

    /**
     * Dispatches an increment mutation through the shared mutation path.
     * Subclasses that resolve entries from their own ViewModel state (for
     * example the Airing list) reuse this dispatch instead of duplicating it.
     */
    protected fun dispatchIncrement(entry: MediaListRecord) {
        captureViewportAnchor(entry.mediaId)
        mediaListMutationViewModel.increment(buildIncrementMediaProgressCommand(entry))
    }
}
