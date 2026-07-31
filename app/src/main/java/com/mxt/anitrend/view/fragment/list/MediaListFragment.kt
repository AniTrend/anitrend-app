package com.mxt.anitrend.view.fragment.list

import android.content.Intent
import android.content.SharedPreferences
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
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.domain.model.toMediaListItemUiModel
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/18.
 * media list fragment
 */
open class MediaListFragment : FragmentBaseList<MediaList, PageContainer<MediaListCollection>>() {

    protected var userId: Long = 0
    protected var userName: String? = null

    @KeyUtil.MediaType
    protected var mediaType: String? = null
    protected var statusIn: String? = null

    private val settings: Settings by inject()

    private val mediaListViewModel: MediaListViewModel by viewModel()
    private val mediaListMutationViewModel: MediaListMutationViewModel by viewModel()

    protected var stateListAdapter: MediaListAdapter? = null
    private val renderedItems = MutableStateFlow<List<MediaList>>(emptyList())

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaListFragment {
            val args = Bundle(params)
            return MediaListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(KeyUtil.arg_id)
            userName = args.getString(KeyUtil.arg_userName)
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
        mAdapter = MediaListAdapterBridge(ctx) { stateListAdapter }

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
        if (adapter.itemCount > 0) {
            if (recyclerView.adapter !== adapter) {
                recyclerView.adapter = adapter
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            } else if (swipeRefreshLayout.isLoading()) {
                swipeRefreshLayout.setLoading(false)
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
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
                super.onSharedPreferenceChanged(sharedPreferences, key)
            }
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<MediaListCollection>?) = Unit

    private fun handleSuccess(state: MediaListViewModel.UiState.Success) {
        submitStateList(state.items, state.renderedItems)
        if (state.pageInfo != null) {
            setPageInfo(state.pageInfo)
        }
        if (state.renderedItems.isNotEmpty()) {
            updateUI()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    protected fun submitStateList(
        items: List<MediaList>,
        rendered: List<com.mxt.anitrend.domain.model.MediaListItemUiModel>? = null,
    ) {
        renderedItems.value = items
        val currentUser = mediaListViewModel.isCurrentUser(userId, userName)
        stateListAdapter?.submitItems(
            rendered ?: items.map { entry ->
                entry.toMediaListRecord().toMediaListItemUiModel(
                    isIncrementPending = false,
                    isDeletePending = false,
                    canIncrement = canIncrement(entry, currentUser),
                )
            },
        )
    }

    override fun onStart() {
        showLoading()
        if ((stateListAdapter?.itemCount ?: 0) < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<MediaList>,
    ) = Unit

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaList>,
    ) = Unit

    private fun openMedia(
        target: View,
        item: com.mxt.anitrend.domain.model.MediaListItemUiModel,
    ) {
        val host = activity ?: return
        val intent =
            Intent(host, MediaActivity::class.java).apply {
                putExtra(KeyUtil.arg_id, item.mediaId)
                putExtra(KeyUtil.arg_mediaType, item.mediaType)
            }
        CompatUtil.startRevealAnim(host, target, intent)
    }

    private fun openManage(item: com.mxt.anitrend.domain.model.MediaListItemUiModel) {
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

    private fun incrementMediaProgress(item: com.mxt.anitrend.domain.model.MediaListItemUiModel) {
        val entry = renderedItems.value.firstOrNull { mediaList ->
            mediaList.id == item.id || mediaList.mediaId == item.mediaId
        } ?: return
        mediaListMutationViewModel.increment(buildIncrementMediaProgressCommand(entry))
    }

    private fun canIncrement(
        entry: MediaList,
        isCurrentUser: Boolean,
    ): Boolean {
        if (!isCurrentUser) {
            return false
        }
        if (CompatUtil.equals(entry.media.status, KeyUtil.NOT_YET_RELEASED)) {
            return false
        }
        return if (CompatUtil.equals(entry.media.type, KeyUtil.ANIME)) {
            entry.media.episodes == 0 || entry.progress < entry.media.episodes
        } else {
            entry.media.chapters == 0 || entry.progress < entry.media.chapters
        }
    }

    private class MediaListAdapterBridge(
        context: android.content.Context,
        private val delegate: () -> MediaListAdapter?,
    ) : RecyclerViewAdapter<MediaList>(context) {
        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int,
        ): RecyclerViewHolder<MediaList> = error("Bridge adapter should never create view holders")

        override fun onBindViewHolder(holder: RecyclerViewHolder<MediaList>, position: Int) = Unit

        override fun getItemCount(): Int = delegate()?.itemCount ?: 0

        override fun getFilter() = delegate()?.filter
    }
}
