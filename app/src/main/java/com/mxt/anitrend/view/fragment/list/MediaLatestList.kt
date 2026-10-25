package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentMediaLatestBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.LatestMediaList
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.ui.media.MediaItems
import com.mxt.anitrend.ui.paging.ManualPageState
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.MediaLatestViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ViewModel-first host for the latest media list. The fragment owns paging,
 * refresh, error surfaces, and navigation; [LatestMediaList] renders the rows.
 */
open class MediaLatestList :
    Fragment(),
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {

    private val settings: Settings by inject()
    private val mediaLatestViewModel: MediaLatestViewModel by viewModel()

    private var _binding: FragmentMediaLatestBinding? = null
    protected val binding: FragmentMediaLatestBinding
        get() = requireNotNull(_binding)

    protected lateinit var requestArgs: Bundle

    // Referential policy: same-id in-place model mutations arrive on new list
    // instances that structural equality would reject as unchanged.
    private var renderedItems by mutableStateOf(emptyList<MediaBase>(), referentialEqualityPolicy())
    private val pageState = ManualPageState()
    private val gridState = LazyGridState()
    private var hasMorePages = true
    private var requestInFlight = false
    private var appendGate = ManualGridAppendGate()
    private var snackbar: Snackbar? = null

    private val stateLayoutOnClick =
        View.OnClickListener {
            if (binding.refreshLayout.isRefreshing()) {
                binding.refreshLayout.setRefreshing(false)
            }
            binding.stateLayout.showLoading()
            onRefresh()
        }

    private val snackBarOnClick =
        View.OnClickListener {
            snackbar?.dismiss()
            makeRequest()
        }

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaLatestList {
            val args = Bundle(params)
            return MediaLatestList().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestArgs =
            Bundle(arguments ?: Bundle()).apply {
                if (!containsKey(KeyUtil.arg_page_limit)) {
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val listBinding = FragmentMediaLatestBinding.inflate(inflater, container, false)
        attachList(listBinding)
        return listBinding.root
    }

    /**
     * Wires an already-inflated list host. Hosts such as [TrendingFragment] embed
     * the list in their own layout instead of inflating [onCreateView]'s binding.
     */
    protected fun attachList(listBinding: FragmentMediaLatestBinding) {
        _binding = listBinding
        listBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        listBinding.refreshLayout.scrollBoundary = { direction ->
            if (direction < 0) gridState.canScrollBackward else gridState.canScrollForward
        }
        activity?.let { CompatUtil.configureSwipeRefreshLayout(listBinding.refreshLayout, it) }
        listBinding.mediaListView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        listBinding.mediaListView.setContent {
            AniTrendTheme {
                LatestMediaList(
                    items = MediaItems(renderedItems),
                    listState = gridState,
                    onMediaClick = { media -> navigateToMedia(MediaScreenParam(media.id, media.type)) },
                    onMediaLongClick = ::onMediaLongClick,
                    onLoadMore = ::loadMore,
                )
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        if (renderedItems.isEmpty()) {
            binding.stateLayout.showLoading()
            onRefresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaLatestViewModel.state.collect { state ->
                    when (state) {
                        is MediaLatestViewModel.UiState.Loading -> Unit
                        is MediaLatestViewModel.UiState.Success -> {
                            if (state.page != pageState.currentPage) return@collect
                            requestInFlight = false
                            hasMorePages = state.hasMorePages
                            appendGate.onRequestSucceeded(state.itemsInPage)
                            renderedItems = state.items
                            stopRefreshIndicators()
                            if (state.items.isEmpty()) {
                                showEmptyState()
                            } else {
                                binding.stateLayout.showContent()
                                if (state.page > 1 && state.itemsInPage == 0) showLoadMoreError()
                            }
                        }
                        is MediaLatestViewModel.UiState.Error -> {
                            if (state.page != pageState.currentPage) return@collect
                            requestInFlight = false
                            if (state.page > 1) appendGate.onRequestFailed()
                            showErrorState(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun onRefresh() {
        pageState.onRefreshPage()
        hasMorePages = true
        appendGate = ManualGridAppendGate()
        makeRequest()
    }

    override fun onLoad() {
        loadMore()
    }

    private fun loadMore() {
        if (
            requestInFlight ||
            !hasMorePages ||
            !appendGate.canRequestNextPage ||
            renderedItems.isEmpty()
        ) {
            return
        }
        binding.refreshLayout.setLoading(true)
        pageState.currentPage++
        makeRequest()
    }

    /** Clears rendered pages and reloads from the first page. */
    protected fun resetAndLoad() {
        pageState.onRefreshPage()
        hasMorePages = true
        appendGate = ManualGridAppendGate()
        renderedItems = emptyList()
        binding.stateLayout.showLoading()
        makeRequest()
    }

    protected fun makeRequest() {
        requestInFlight = true
        if (pageState.currentPage > 1) appendGate.onRequestStarted()
        val type = NavigationArgs.resolveMediaType(requestArgs.getString(KeyUtil.arg_mediaType))
        val sort = NavigationArgs.optionalString(requestArgs.containsKey(KeyUtil.arg_sort), requestArgs.getString(KeyUtil.arg_sort))
        val isAdult: Boolean? =
            if (!settings.displayAdultContent) {
                false
            } else {
                NavigationArgs.optionalBoolean(requestArgs.containsKey(KeyUtil.arg_isAdult), requestArgs.getBoolean(KeyUtil.arg_isAdult))
            }
        mediaLatestViewModel.load(
            type = type,
            page = pageState.currentPage,
            pageLimit = NavigationArgs.intWithDefault(requestArgs.containsKey(KeyUtil.arg_page_limit), requestArgs.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
            sort = sort,
            isAdult = isAdult,
        )
    }

    private fun onMediaLongClick(media: MediaBase) {
        if (settings.isAuthenticated) {
            val host = activity ?: return
            MediaActionUtil
                .Builder()
                .setId(media.id)
                .build(host)
                .startSeriesAction()
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

    private fun stopRefreshIndicators() {
        if (binding.refreshLayout.isRefreshing()) {
            binding.refreshLayout.setRefreshing(false)
        }
        if (binding.refreshLayout.isLoading()) {
            binding.refreshLayout.setLoading(false)
        }
    }

    private fun showEmptyState() {
        stopRefreshIndicators()
        if (pageState.currentPage > 1) {
            showLoadMoreError()
        } else {
            binding.stateLayout.showError(
                context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
                getString(R.string.layout_empty_response),
                getString(R.string.try_again),
                stateLayoutOnClick,
            )
        }
    }

    private fun showErrorState(error: String) {
        stopRefreshIndicators()
        if (pageState.currentPage > 1) {
            showLoadMoreError()
        } else {
            binding.stateLayout.showError(
                context?.getCompatDrawable(R.drawable.ic_emoji_cry),
                error,
                getString(R.string.try_again),
                stateLayoutOnClick,
            )
        }
    }

    private fun showLoadMoreError() {
        if (binding.stateLayout.isLoading) {
            binding.stateLayout.showContent()
        }
        snackbar =
            NotifyUtil
                .make(binding.stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.try_again, snackBarOnClick)
        snackbar?.show()
    }

    override fun onDestroyView() {
        snackbar?.dismiss()
        snackbar = null
        _binding = null
        super.onDestroyView()
    }
}
