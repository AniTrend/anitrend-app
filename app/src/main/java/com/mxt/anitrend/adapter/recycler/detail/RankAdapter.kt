package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.RankRow

/** Binds media ranking rows while preserving their existing click targets. */
class RankAdapter(
    context: Context,
) : RecyclerViewAdapter<MediaRank>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<MediaRank> {
        val composeView =
            ComposeView(parent.context).apply {
                id = R.id.container
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            }
        return RankViewHolder(composeView)
    }

    override fun getFilter(): Filter? = null

    private inner class RankViewHolder(
        private val composeView: ComposeView,
    ) : RecyclerViewHolder<MediaRank>(composeView) {
        private val rank = mutableStateOf<MediaRank?>(null)

        init {
            composeView.setContent {
                AniTrendTheme {
                    rank.value?.let { currentRank ->
                        RankRow(
                            rank = currentRank,
                            onClick = { this@RankViewHolder.onClick(composeView) },
                            onTextClick = {
                                composeView
                                    .findViewById<View>(R.id.sub_container)
                                    ?.let(this@RankViewHolder::onClick)
                            },
                            onLongClick = { this@RankViewHolder.onLongClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: MediaRank) {
            rank.value = model
        }

        override fun onViewRecycled() {
            rank.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
