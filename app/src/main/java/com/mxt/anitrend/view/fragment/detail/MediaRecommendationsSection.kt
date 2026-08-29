package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.RecommendationAdapter
import com.mxt.anitrend.adapter.recycler.shared.LoadStateFooterAdapter
import com.mxt.anitrend.adapter.recycler.shared.PagingLoadStateRenderer
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * View-only Paging 3 recommendations section used by the media destination.
 *
 * The constructor callbacks and grouped state helpers intentionally mirror the
 * destination's existing recommendation actions and lifecycle.
 */
@Suppress("LongParameterList", "TooManyFunctions")
class MediaRecommendationsSection(
    context: Context,
    private val viewModel: MediaRecommendationsViewModel,
    private val mediaId: Long,
    private val mediaType: String?,
    private val isAdultContent: Boolean,
    private val onOpenMedia: (View, RecommendationItemUiModel) -> Unit,
    private val onLongPressMedia: (View, RecommendationItemUiModel) -> Boolean,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private val appContext = context
    private val adapter = RecommendationAdapter(
        context = context,
        onOpenMedia = onOpenMedia,
        onLongPressMedia = onLongPressMedia,
    )
    private var binding: FragmentListBinding? = null
    private var selected = false

    private val currentBinding: FragmentListBinding
        get() = checkNotNull(binding)

    /** Inflates and initializes the recommendations view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.recyclerView.layoutManager = StaggeredGridLayoutManager(
            appContext.resources.getInteger(R.integer.grid_giphy_x3),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.recyclerView.setHasFixedSize(true)
        sectionBinding.refreshLayout.setPermitLoad(false)
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        sectionBinding.recyclerView.adapter = adapter.withLoadStateFooter(
            LoadStateFooterAdapter(retry = adapter::retry),
        )
        sectionBinding.stateLayout.showLoading()
        return sectionBinding.root
    }

    /** Starts collecting recommendation paging state for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pagingDataFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
                launch {
                    adapter.loadStateFlow.collect { loadStates ->
                        PagingLoadStateRenderer(
                            itemCount = { adapter.itemCount },
                            callbacks = PagingLoadStateRenderer.Callbacks(
                                showLoading = ::showLoading,
                                showContent = ::showContent,
                                showError = ::showError,
                                showEmpty = ::showEmpty,
                                stopRefreshIndicators = ::stopRefreshIndicators,
                                messages = PagingLoadStateRenderer.Callbacks.Messages(
                                    errorMessage = { appContext.getString(R.string.text_error_request) },
                                    emptyMessage = { appContext.getString(R.string.layout_empty_response) },
                                ),
                            ),
                        ).render(loadStates)
                    }
                }
            }
        }
    }

    /** Activates the section and starts loading recommendations when needed. */
    fun select() {
        if (!selected) {
            selected = true
            load()
        } else if (adapter.itemCount > 0) {
            showContent()
        } else {
            showLoading()
        }
    }

    /** Releases the recommendation adapter and view resources. */
    fun destroyView() {
        binding?.recyclerView?.adapter = null
        binding = null
        selected = false
    }

    /** Refreshes the recommendation paging stream. */
    override fun onRefresh() = adapter.refresh()

    /** Ignores the legacy append callback because Paging owns append loading. */
    override fun onLoad() = Unit

    private fun load() {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        viewModel.load(
            mediaId = mediaId,
            type = type,
            isAdult = if (isAdultContent) null else false,
        )
    }

    private fun showLoading() {
        currentBinding.stateLayout.showLoading()
    }

    private fun showContent() {
        currentBinding.stateLayout.showContent()
    }

    private fun showError(message: String) {
        stopRefreshIndicators()
        currentBinding.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun showEmpty(message: String) {
        stopRefreshIndicators()
        currentBinding.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_sweat),
            message,
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun stopRefreshIndicators() {
        currentBinding.refreshLayout.setRefreshing(false)
        currentBinding.refreshLayout.setLoading(false)
    }
}
