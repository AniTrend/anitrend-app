package com.mxt.anitrend.view.fragment.group

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
import com.mxt.anitrend.adapter.recycler.group.RecommendationAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.databinding.ItemRecommendationLoadStateFooterBinding
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Media recommendations screen driven by the Paging 3 network-only pilot.
 *
 * Paging owns page orchestration through the ViewModel's cached [PagingData] stream;
 * this fragment submits that stream with the view lifecycle, renders refresh/append
 * load states from the adapter's load state flow, and shows append errors in a
 * load-state footer. The base class's manual scroll listener and page counter stay
 * disabled for this screen ([isPager] stays false); [FragmentBaseList] and
 * [com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener] are unchanged.
 */
class MediaRecommendationsFragment : FragmentBaseList<RecommendationItemUiModel, RecommendationPageResult>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    private val settings: Settings by inject()

    private val mediaRecommendationsViewModel: MediaRecommendationsViewModel by viewModel()

    private var recommendationAdapter: RecommendationAdapter? = null

    companion object {
        /**
         * Resolves the media identity from the fragment arguments.
         *
         * The typed [MediaScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] / [KeyUtil.arg_mediaType] extras are bridged with
         * their exact raw values (0 or negative ids pass through, mirroring the
         * pre-refactor getter). A typed param present but invalid falls back to the
         * legacy raw values.
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(typed: MediaScreenParam?, legacyId: Long, legacyType: String?): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy values.
            }
            return MediaScreenParam(mediaId = legacyId, mediaType = legacyType)
        }

        @JvmStatic
        fun newInstance(args: Bundle): MediaRecommendationsFragment = MediaRecommendationsFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        fromBundle(arguments)?.let { args ->
            mediaId = args.mediaId
            mediaType = args.mediaType
        }
        // Paging owns pagination for this screen: the base class's manual scroll
        // listener and page counter remain disabled (isPager stays false).
        mColumnSize = R.integer.grid_giphy_x3
        recommendationAdapter =
            RecommendationAdapter(
                context = ctx,
                onOpenMedia = ::openMedia,
                onLongPressMedia = ::onLongPressMedia,
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = recommendationAdapter ?: return
        // Append errors render in a load-state footer; the pull-up "load more"
        // gesture of the base swipe layout is disabled for this screen.
        swipeRefreshLayout.setPermitLoad(false)
        val footer = RecommendationsLoadStateAdapter(retry = adapter::retry)
        recyclerView.adapter = adapter.withLoadStateFooter(footer)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mediaRecommendationsViewModel.pagingDataFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
                launch {
                    adapter.loadStateFlow.collect(::onLoadStateChanged)
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
        if ((recommendationAdapter?.itemCount ?: 0) > 0) {
            showContent()
        }
    }

    /**
     * Maps Paging load states to the screen surfaces: the base progress/error/empty
     * states for refresh, and the load-state footer for append.
     */
    private fun onLoadStateChanged(loadStates: CombinedLoadStates) {
        val adapter = recommendationAdapter ?: return
        val refresh = loadStates.refresh
        when {
            adapter.itemCount > 0 -> {
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

    override fun onRefresh() {
        recommendationAdapter?.refresh()
    }

    /** No-op: append pagination is driven by Paging through the load-state footer. */
    override fun onLoadMore() = Unit

    /** No-op: this screen has no in-memory filtering. */
    override fun updateUI() = Unit

    override fun makeRequest() {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        mediaRecommendationsViewModel.load(
            mediaId = mediaId,
            type = type,
            isAdult = isAdult,
        )
    }

    /** No-op: the PagingData collection in onViewCreated handles the stream. */
    override fun onChanged(value: RecommendationPageResult?) = Unit

    private fun openMedia(
        target: View,
        item: RecommendationItemUiModel,
    ) {
        val host = activity ?: return
        val intent = MediaActivity.newIntent(host, item.mediaId, item.mediaType)
        CompatUtil.startRevealAnim(host, target, intent)
    }

    private fun onLongPressMedia(
        target: View,
        item: RecommendationItemUiModel,
    ): Boolean {
        if (settings.isAuthenticated) {
            val host = activity ?: return false
            mediaActionUtil =
                MediaActionUtil
                    .Builder()
                    .setId(item.mediaId)
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

    override fun onItemClick(
        target: View,
        data: IndexedValue<RecommendationItemUiModel>,
    ) = Unit

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<RecommendationItemUiModel>,
    ) = Unit
}

/**
 * Append load-state footer for the recommendations screen: shows a spinner while the
 * next page loads and a retry action when the append failed.
 */
private class RecommendationsLoadStateAdapter(
    private val retry: () -> Unit,
) : LoadStateAdapter<RecommendationsLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadStateViewHolder = LoadStateViewHolder(
        ItemRecommendationLoadStateFooterBinding.inflate(
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
        private val binding: ItemRecommendationLoadStateFooterBinding,
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
