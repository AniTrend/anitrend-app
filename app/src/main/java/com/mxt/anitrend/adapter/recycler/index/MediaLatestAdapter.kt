package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView

import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.LatestMediaRowUiState
import com.mxt.anitrend.ui.media.MediaLatestRow
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.KeyUtil.RecyclerViewType

/**
 * Adapter for rendering the latest anime and manga media cards.
 */
class MediaLatestAdapter(context: Context) : RecyclerViewAdapter<MediaBase>(context) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        @RecyclerViewType viewType: Int,
    ): RecyclerViewHolder<MediaBase> = if (viewType == KeyUtil.RECYCLER_TYPE_ANIME) {
        LatestAnimeViewHolder(parent)
    } else {
        LatestMangaViewHolder(parent)
    }

    private fun createComposeView(parent: ViewGroup): ComposeView {
        val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
        return ComposeView(parent.context).apply {
            id = R.id.container
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    marginStart = margin
                    marginEnd = margin
                    topMargin = margin
                }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        }
    }

    @RecyclerViewType
    override fun getItemViewType(position: Int): Int = if (data[position].type == KeyUtil.ANIME) {
        KeyUtil.RECYCLER_TYPE_ANIME
    } else {
        KeyUtil.RECYCLER_TYPE_MANGA
    }

    override fun getFilter(): Filter? = null

    /**
     * View holder for the latest anime card layout.
     */
    inner class LatestAnimeViewHolder(
        parent: ViewGroup,
    ) : RecyclerViewHolder<MediaBase>(createComposeView(parent)) {
        private val composeView = itemView as ComposeView
        private val rowState = mutableStateOf<LatestMediaRowUiState?>(null, neverEqualPolicy())

        init {
            composeView.setContent {
                AniTrendTheme {
                    rowState.value?.let { state ->
                        MediaLatestRow(
                            state = state,
                            onClick = { this@LatestAnimeViewHolder.onClick(composeView) },
                            onLongClick = { this@LatestAnimeViewHolder.onLongClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: MediaBase) {
            rowState.value = LatestMediaRowUiState.from(composeView.context, model)
        }

        override fun onViewRecycled() {
            rowState.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, composeView)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, composeView)
    }

    /**
     * View holder for the latest manga card layout.
     */
    inner class LatestMangaViewHolder(
        parent: ViewGroup,
    ) : RecyclerViewHolder<MediaBase>(createComposeView(parent)) {
        private val composeView = itemView as ComposeView
        private val rowState = mutableStateOf<LatestMediaRowUiState?>(null, neverEqualPolicy())

        init {
            composeView.setContent {
                AniTrendTheme {
                    rowState.value?.let { state ->
                        MediaLatestRow(
                            state = state,
                            onClick = { this@LatestMangaViewHolder.onClick(composeView) },
                            onLongClick = { this@LatestMangaViewHolder.onLongClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: MediaBase) {
            rowState.value = LatestMediaRowUiState.from(composeView.context, model)
        }

        override fun onViewRecycled() {
            rowState.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, composeView)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, composeView)
    }
}
