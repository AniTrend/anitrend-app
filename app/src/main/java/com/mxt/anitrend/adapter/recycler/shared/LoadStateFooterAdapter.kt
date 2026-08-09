package com.mxt.anitrend.adapter.recycler.shared

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.databinding.ItemLoadStateFooterBinding
import com.mxt.anitrend.extension.getLayoutInflater

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
    ): LoadStateViewHolder = LoadStateViewHolder(
        ItemLoadStateFooterBinding.inflate(
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

    /** View holder that renders the current append load state. */
    inner class LoadStateViewHolder(
        private val binding: ItemLoadStateFooterBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.retryButton.setOnClickListener { retry() }
        }

        /** Applies the Paging state to the footer's loading and retry surfaces. */
        fun bind(loadState: LoadState) {
            val uiState = LoadStateFooterUiState.from(loadState)
            binding.loadingProgress.isVisible = uiState.showLoading
            binding.loadingText.isVisible = uiState.showLoading
            binding.retryButton.isVisible = uiState.showRetry
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
