package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.giphy.GiphyResultRow

/**
 * Created by max on 2017/12/09.
 */
class GiphyAdapter(
    context: Context,
) : RecyclerViewAdapter<Giphy>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<Giphy> {
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
        return GiphyViewHolder(composeView)
    }

    override fun getFilter(): Filter? = null

    private inner class GiphyViewHolder(
        private val composeView: ComposeView,
    ) : RecyclerViewHolder<Giphy>(composeView) {
        private val giphy = mutableStateOf<Giphy?>(null)

        init {
            composeView.setContent {
                AniTrendTheme {
                    giphy.value?.let { item ->
                        GiphyResultRow(
                            giphy = item,
                            onClick = { this@GiphyViewHolder.onClick(itemView) },
                            onLongClick = { this@GiphyViewHolder.onLongClick(itemView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: Giphy) {
            giphy.value = model
        }

        override fun onViewRecycled() {
            giphy.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(view: View): Boolean = performLongClick(clickListener, data, view)
    }
}
