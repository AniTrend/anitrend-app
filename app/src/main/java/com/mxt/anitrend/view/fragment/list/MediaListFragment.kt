package com.mxt.anitrend.view.fragment.list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.util.media.MediaListUtil
import com.mxt.anitrend.util.selectedIndex
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaListMutationViewModel
import com.mxt.anitrend.viewmodel.MediaListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/18.
 * media list fragment
 */
open class MediaListFragment : FragmentBaseList<MediaListItemUiModel, MediaListCollectionPageResult>() {

    protected var userId: Long = 0
    protected var userName: String? = null

    @KeyUtil.MediaType
    protected var mediaType: String? = null
    protected var statusIn: String? = null

    private val settings: Settings by inject()

    private val mediaListViewModel: MediaListViewModel by viewModel()
    private val mediaListMutationViewModel: MediaListMutationViewModel by viewModel()

    protected var stateListAdapter: MediaListAdapter? = null
    private var latestEntries: List<MediaListRecord> = emptyList()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaListFragment {
            val args = Bundle(params)
            return MediaListFragment().apply {
                arguments = args
            }
        }

        /**
         * Documented legacy channel: the media-list host activity (MediaListActivity)
         * writes only legacy wire extras (arg_id, arg_userName, arg_mediaType,
         * arg_statusIn), so the identity read stays on the transitional channel.
         * Reads mirror the pre-refactor getters exactly (absent id resolves to 0).
         */
        fun fromBundle(bundle: Bundle?): UserScreenParam? = resolve(
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyName = bundle?.getString(KeyUtil.arg_userName),
        )

        @VisibleForTesting
        internal fun resolve(legacyId: Long, legacyName: String?): UserScreenParam =
            UserScreenParam(userId = legacyId, initialName = legacyName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromBundle(arguments)?.let { args ->
            userId = args.userId
            userName = args.initialName
        }
        arguments?.let { args ->
            statusIn = args.getString(KeyUtil.arg_statusIn)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }

        isFilterableEnabled = true
        isPager = false
        val ctx = requireContext()
        stateListAdapter =
            MediaListAdapter(
                context = ctx,
                mediaListStyle = settings.mediaListStyle,
                onIncrement = ::incrementMediaProgress,
                onOpenMedia = ::openMedia,
                onOpenManage = ::openManage,
            )

        mColumnSize =
            if (settings.mediaListStyle == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                R.integer.single_list_x1
            } else {
                R.integer.grid_list_x2
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

    override fun applySearchQuery(searchQuery: String?) {
        this.query = searchQuery
        if (!isPager && (stateListAdapter?.itemCount ?: 0) > 0) {
            val filterQuery = if (searchQuery.isNullOrEmpty()) "" else searchQuery
            stateListAdapter?.filter?.filter(filterQuery)
        }
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
        menu.findItem(R.id.action_status).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ctx = context ?: return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_sort -> {
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_sort,
                    CompatUtil.getIndexOf(KeyUtil.MediaListSortType, settings.mediaListSort),
                    CompatUtil.capitalizeWords(KeyUtil.MediaListSortType),
                ) { dialog, _ ->
                    settings.mediaListSort = KeyUtil.MediaListSortType[dialog.selectedIndex]
                }
                return true
            }
            R.id.action_order -> {
                val sortOrders = arrayOf(KeyUtil.ASC, KeyUtil.DESC)
                DialogUtil.createSelection(
                    ctx,
                    R.string.app_filter_order,
                    CompatUtil.getIndexOf(sortOrders, settings.sortOrder),
                    CompatUtil.getStringList(ctx, R.array.order_by_types),
                ) { dialog, _ ->
                    settings.saveSortOrder(
                        sortOrders.getOrNull(dialog.selectedIndex) ?: settings.sortOrder,
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        val adapter = stateListAdapter ?: return
        // Content/empty is decided by the success handler from the submitted payload
        // (submitItems applies asynchronously through the filter + AsyncListDiffer), so
        // itemCount must not gate the teardown here. Both callers (onStart with
        // itemCount >= 1, and handleSuccess with a non-empty payload) guarantee content.
        if (recyclerView.adapter !== adapter) {
            recyclerView.adapter = adapter
        }
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
                stateListAdapter?.submitItems(emptyList())
                onRefresh()
            }
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: MediaListCollectionPageResult?) = Unit

    private fun handleSuccess(state: MediaListViewModel.UiState.Success) {
        latestEntries = state.entries
        submitStateList(state.renderedItems)
        state.pageInfo?.let { setPageInfo(it.toPageInfo()) }
        if (state.renderedItems.isNotEmpty()) {
            updateUI()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    protected fun submitStateList(rendered: List<MediaListItemUiModel>) {
        stateListAdapter?.submitItems(rendered)
    }

    override fun onStart() {
        super.onStart()
        showLoading()
        if ((stateListAdapter?.itemCount ?: 0) < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<MediaListItemUiModel>,
    ) = Unit

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaListItemUiModel>,
    ) = Unit

    private fun openMedia(
        target: View,
        item: MediaListItemUiModel,
    ) {
        val host = activity ?: return
        val intent = MediaActivity.newIntent(host, item.mediaId, item.mediaType)
        CompatUtil.startRevealAnim(host, target, intent)
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

    private fun incrementMediaProgress(item: MediaListItemUiModel) {
        val entry = latestEntries.firstOrNull { record ->
            record.id == item.id || record.mediaId == item.mediaId
        } ?: return
        mediaListMutationViewModel.increment(buildIncrementMediaProgressCommand(entry))
    }
}
