package com.mxt.anitrend.view.fragment.search

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.search.MediaSearchAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.databinding.ItemMediaSearchLoadStateFooterBinding
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaSearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/20.
 * series searching fragment
 *
 * Media search screen driven by the Paging 3 network-only pilot. Paging owns page
 * orchestration through the ViewModel's cached [PagingData] stream; this fragment
 * submits that stream with the view lifecycle, renders refresh/append load states
 * from the adapter's load state flow, and shows append errors in a load-state
 * footer. The base class's manual scroll listener and page counter stay disabled
 * for this screen ([isPager] stays false); [FragmentBaseList] and
 * [com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener] are unchanged.
 */
class MediaSearchFragment : FragmentBaseList<MediaSearchItemUiModel, PageContainer<MediaBase>>() {
    private var searchQuery: String? = null

    @KeyUtil.MediaType
    private var mediaType: String? = null

    private val settings: Settings by inject()

    private val mediaSearchViewModel: MediaSearchViewModel by viewModel()

    private var mediaSearchAdapter: MediaSearchAdapter? = null

    companion object {
        /**
         * Documented legacy channel: the search query is caller state, not identity.
         * It stays on arg_search (with the media type for the media destination) until
         * a search-state model is designed. Reads mirror the pre-refactor getters
         * exactly (absent values resolve to null).
         */
        fun fromBundle(bundle: Bundle?): SearchQueryLegacyArgs? = resolve(
            legacyQuery = bundle?.getString(KeyUtil.arg_search),
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        /** Exact legacy read result for the media search destination. */
        data class SearchQueryLegacyArgs(
            val searchQuery: String?,
            val mediaType: String?,
        )

        @VisibleForTesting
        internal fun resolve(legacyQuery: String?, legacyType: String?): SearchQueryLegacyArgs = SearchQueryLegacyArgs(searchQuery = legacyQuery, mediaType = legacyType)

        @JvmStatic
        fun newInstance(
            bundle: Bundle,
            @KeyUtil.MediaType mediaType: String,
        ): MediaSearchFragment {
            val args =
                Bundle(bundle).apply {
                    putString(KeyUtil.arg_mediaType, mediaType)
                }
            return MediaSearchFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromBundle(arguments)?.let { args ->
            searchQuery = args.searchQuery
            mediaType = args.mediaType
        }
        mColumnSize = R.integer.grid_giphy_x3
        // Paging owns pagination for this screen: the base class's manual scroll
        // listener and page counter remain disabled (isPager stays false).
        val ctx = requireContext()
        val mediaItemActions = MediaItemActionHandler()
        mediaSearchAdapter =
            MediaSearchAdapter(
                context = ctx,
                onOpenMedia = mediaItemActions::open,
                onLongPressMedia = mediaItemActions::longPress,
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = mediaSearchAdapter ?: return
        // Append errors render in a load-state footer; the pull-up "load more"
        // gesture of the base swipe layout is disabled for this screen.
        swipeRefreshLayout.setPermitLoad(false)
        val footer = MediaSearchLoadStateAdapter(retry = adapter::retry)
        recyclerView.adapter = adapter.withLoadStateFooter(footer)
        val loadStateRenderer = PagingLoadStateRenderer(itemCount = { adapter.itemCount })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mediaSearchViewModel.pagingDataFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
                launch {
                    adapter.loadStateFlow.collect(loadStateRenderer::render)
                }
            }
        }
        makeRequest()
    }

    override fun onStart() {
        super.onStart()
        // The base class shows the loading state on every start; restore the
        // content state when a generation is already presented. Loads themselves
        // are driven entirely by the Paging collection in onViewCreated.
        if ((mediaSearchAdapter?.itemCount ?: 0) > 0) {
            showContent()
        }
    }

    override fun onRefresh() {
        mediaSearchAdapter?.refresh()
    }

    /** No-op: append pagination is driven by Paging through the load-state footer. */
    override fun onLoadMore() = Unit

    /** No-op: this screen has no in-memory filtering. */
    override fun updateUI() = Unit

    override fun makeRequest() {
        val query = searchQuery ?: return
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        mediaSearchViewModel.load(
            search = query,
            type = type,
            isAdult = isAdult,
        )
    }

    /** No-op: the PagingData collection in onViewCreated handles the stream. */
    override fun onChanged(value: PageContainer<MediaBase>?) = Unit

    /**
     * Renders Paging load states onto the base-class screen surfaces: the base
     * progress/error/empty states for refresh, and the load-state footer for
     * append. Keeps the swipe-refresh indicators in sync with the refresh state.
     */
    private inner class PagingLoadStateRenderer(
        private val itemCount: () -> Int,
    ) {
        fun render(loadStates: CombinedLoadStates) {
            val refresh = loadStates.refresh
            when {
                itemCount() > 0 -> {
                    // Keep the swipe spinner while a refresh is in flight; otherwise stop it.
                    if (refresh !is LoadState.Loading) {
                        stopRefreshIndicators()
                    }
                    showContent()
                }
                refresh is LoadState.Loading -> showLoading()
                refresh is LoadState.Error -> {
                    stopRefreshIndicators()
                    showError(refresh.error.message ?: getString(R.string.text_error_request))
                }
                loadStates.append.endOfPaginationReached -> {
                    stopRefreshIndicators()
                    showEmpty(getString(R.string.layout_empty_response))
                }
                else -> showLoading()
            }
        }

        private fun stopRefreshIndicators() {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            }
            if (swipeRefreshLayout.isLoading()) {
                swipeRefreshLayout.setLoading(false)
            }
        }
    }

    /**
     * Media item click actions for this screen: opens the media detail screen
     * and starts the long-press series action sheet.
     */
    private inner class MediaItemActionHandler {
        fun open(
            target: View,
            item: MediaSearchItemUiModel,
        ) {
            val host = activity ?: return
            val intent = MediaActivity.newIntent(host, item.id, item.mediaType)
            CompatUtil.startRevealAnim(host, target, intent)
        }

        fun longPress(
            target: View,
            item: MediaSearchItemUiModel,
        ): Boolean {
            if (settings.isAuthenticated) {
                val host = activity ?: return false
                mediaActionUtil =
                    MediaActionUtil
                        .Builder()
                        .setId(item.id)
                        .build(host)
                mediaActionUtil.startSeriesAction()
                return true
            }
            context?.let {
                NotifyUtil
                    .makeText(
                        it,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
            return true
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<MediaSearchItemUiModel>,
    ) = Unit

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<MediaSearchItemUiModel>,
    ) = Unit
}

/**
 * Append load-state footer for the media search screen: shows a spinner while the
 * next page loads and a retry action when the append failed.
 */
private class MediaSearchLoadStateAdapter(
    private val retry: () -> Unit,
) : LoadStateAdapter<MediaSearchLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadStateViewHolder = LoadStateViewHolder(
        ItemMediaSearchLoadStateFooterBinding.inflate(
            parent.context.getLayoutInflater(),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState,
    ) {
        holder.bind(loadState)
    }

    inner class LoadStateViewHolder(
        private val binding: ItemMediaSearchLoadStateFooterBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.retryButton.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            val loading = loadState is LoadState.Loading
            binding.loadingProgress.isVisible = loading
            binding.loadingText.isVisible = loading
            binding.retryButton.isVisible = loadState is LoadState.Error
        }
    }
}
