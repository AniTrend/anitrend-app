package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.compose.collectAsLazyPagingItems
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.RecommendationGrid
import com.mxt.anitrend.ui.paging.PagingLoadStateRenderer
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** View-only Paging 3 recommendation section with direct Compose item rendering. */
@Suppress("LongParameterList", "TooManyFunctions")
class MediaRecommendationsSection(
    private val context: Context,
    private val viewModel: MediaRecommendationsViewModel,
    private val mediaId: Long,
    private val mediaType: String?,
    private val isAdultContent: Boolean,
    private val onOpenMedia: (View, RecommendationItemUiModel) -> Unit,
    private val onLongPressMedia: (View, RecommendationItemUiModel) -> Boolean,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private data class PagingSnapshot(val loadStates: CombinedLoadStates, val itemCount: Int)

    private var binding: FragmentListBinding? = null
    private val selected = mutableStateOf(false)
    private val refreshSignal = mutableStateOf(0)
    private val paging = MutableStateFlow<PagingSnapshot?>(null)
    private val listState = LazyGridState()

    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val content = FragmentListBinding.inflate(inflater, container, false)
        binding = content
        content.refreshLayout.setPermitLoad(false)
        content.refreshLayout.setOnRefreshAndLoadListener(this)
        content.refreshLayout.scrollBoundary = { direction ->
            if (direction < 0) listState.canScrollBackward else listState.canScrollForward
        }
        val host = ComposeView(context).apply {
            id = R.id.container
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                if (selected.value) {
                    val items = viewModel.pagingDataFlow.collectAsLazyPagingItems()
                    LaunchedEffect(refreshSignal.value) {
                        if (refreshSignal.value > 0) {
                            refreshSignal.value = 0
                            items.refresh()
                        }
                    }
                    LaunchedEffect(items) {
                        snapshotFlow { PagingSnapshot(items.loadState, items.itemCount) }
                            .collect { paging.value = it }
                    }
                    AniTrendTheme {
                        RecommendationGrid(
                            items = items,
                            listState = listState,
                            onOpenMedia = { onOpenMedia(this@apply, it) },
                            onLongPressMedia = { onLongPressMedia(this@apply, it) },
                        )
                    }
                }
            }
        }
        content.refreshLayout.addView(host, 0)
        content.stateLayout.showLoading()
        return content.root
    }

    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                paging.collect { snapshot ->
                    if (selected.value && snapshot != null) render(snapshot)
                }
            }
        }
    }

    fun select() {
        if (!selected.value) {
            val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
            viewModel.load(mediaId, type, if (isAdultContent) null else false)
            selected.value = true
        }
        // A tab round-trip may not emit another paging load state.
        paging.value?.let(::render) ?: binding?.stateLayout?.showLoading()
    }

    private fun render(snapshot: PagingSnapshot) {
        if (binding == null) return
        PagingLoadStateRenderer(
            itemCount = { snapshot.itemCount },
            callbacks = PagingLoadStateRenderer.Callbacks(
                showLoading = { binding?.stateLayout?.showLoading() },
                showContent = { binding?.stateLayout?.showContent() },
                showError = ::showError,
                showEmpty = ::showEmpty,
                stopRefreshIndicators = ::stopRefreshIndicators,
                messages = PagingLoadStateRenderer.Callbacks.Messages(
                    errorMessage = { context.getString(R.string.text_error_request) },
                    emptyMessage = { context.getString(R.string.layout_empty_response) },
                ),
            ),
        ).render(snapshot.loadStates)
    }

    override fun onRefresh() {
        refreshSignal.value++
    }

    override fun onLoad() = Unit

    private fun showError(message: String) {
        stopRefreshIndicators()
        binding?.stateLayout?.showError(
            context.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            context.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun showEmpty(message: String) {
        stopRefreshIndicators()
        binding?.stateLayout?.showError(
            context.getCompatDrawable(R.drawable.ic_emoji_sweat),
            message,
            context.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun stopRefreshIndicators() {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
    }

    fun destroyView() {
        binding?.refreshLayout?.scrollBoundary = null
        binding = null
        selected.value = false
        paging.value = null
    }
}
