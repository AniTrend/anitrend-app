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
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
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
import com.mxt.anitrend.viewmodel.MediaListViewModel
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

    private val mutationCoordinator by inject<WidgetMutationCoordinator>()

    private val mediaListViewModel: MediaListViewModel by viewModel()

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
        mAdapter =
            MediaListAdapter(ctx, mutationCoordinator).apply {
                setCurrentUser(userName)
            }

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
        injectAdapter()
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
        if (state.pageInfo != null) {
            setPageInfo(state.pageInfo)
        }
        if (!state.isEmpty) {
            onPostProcessed(state.items)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<MediaList>,
    ) {
        when (target.id) {
            R.id.container,
            R.id.series_image,
            -> {
                val host = activity ?: return
                val mediaBase = data.value.media
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, data.value.mediaId)
                        putExtra(KeyUtil.arg_mediaType, mediaBase.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaList>,
    ) {
        when (target.id) {
            R.id.container,
            R.id.series_image,
            -> {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(data.value.mediaId)
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
