package com.mxt.anitrend.adapter.recycler.shared

import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/**
 * Shared Paging 3 append load-state footer that shows progress while the next
 * page loads and a retry action when the append fails.
 */
class LoadStateFooterAdapter(
    private val retry: () -> Unit,
) : LoadStateAdapter<LoadStateFooterAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadStateViewHolder {
        val composeView =
            ComposeView(parent.context).apply {
                id = View.generateViewId()
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            }
        return LoadStateViewHolder(composeView, loadState)
    }

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState,
    ) {
        holder.bind(loadState)
    }

    /** View holder that renders the current append load state. */
    inner class LoadStateViewHolder(
        composeView: ComposeView,
        initialLoadState: LoadState,
    ) : RecyclerView.ViewHolder(composeView) {
        private val state = mutableStateOf(LoadStateFooterUiState.from(initialLoadState))

        init {
            composeView.setContent {
                AniTrendTheme {
                    LoadStateFooter(
                        state = state.value,
                        onRetry = retry,
                    )
                }
            }
        }

        /** Applies the Paging state to the footer's loading and retry surfaces. */
        fun bind(loadState: LoadState) {
            state.value = LoadStateFooterUiState.from(loadState)
        }
    }
}

/**
 * Pure projection of a paging [LoadState] onto the footer's visible surfaces.
 * Kept framework-free so the Loading/Error/NotLoading mapping stays unit-testable.
 */
internal data class LoadStateFooterUiState(
    val showLoading: Boolean,
    val showRetry: Boolean,
) {
    companion object {
        /** Maps a Paging state to the footer surfaces that should be visible. */
        fun from(loadState: LoadState): LoadStateFooterUiState = LoadStateFooterUiState(
            showLoading = loadState is LoadState.Loading,
            showRetry = loadState is LoadState.Error,
        )
    }
}
