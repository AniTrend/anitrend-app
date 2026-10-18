package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.container.CardViewBase
import com.mxt.anitrend.extension.getDrawableFromAttr
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.StudioRow

/** RecyclerView adapter for studio rows in search and favourites. */
class StudioAdapter(
    context: Context,
) : RecyclerViewAdapter<StudioBase>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<StudioBase> {
        val context = parent.context
        val cardView =
            CardViewBase(context).apply {
                id = R.id.container
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        val margin = context.resources.getDimensionPixelSize(R.dimen.lg_margin)
                        setMargins(margin, margin, margin, margin)
                    }
                isClickable = true
                isFocusable = true
                foreground = context.getDrawableFromAttr(android.R.attr.selectableItemBackground)
            }
        return StudioViewHolder(cardView)
    }

    override fun getFilter(): Filter? = null

    private inner class StudioViewHolder(
        private val cardView: CardViewBase,
    ) : RecyclerViewHolder<StudioBase>(cardView) {
        private val studioName = mutableStateOf("")
        private val isFavourite = mutableStateOf(false)
        private val composeView =
            ComposeView(cardView.context).apply {
                id = View.generateViewId()
                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            }

        init {
            cardView.addView(composeView)
            bindClickListeners(R.id.container)
            composeView.setContent {
                AniTrendTheme {
                    StudioRow(
                        studioName = studioName.value,
                        isFavourite = isFavourite.value,
                    )
                }
            }
        }

        override fun onBindViewHolder(model: StudioBase) {
            studioName.value = model.name.orEmpty()
            isFavourite.value = model.isFavourite
        }

        override fun onViewRecycled() = Unit

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
