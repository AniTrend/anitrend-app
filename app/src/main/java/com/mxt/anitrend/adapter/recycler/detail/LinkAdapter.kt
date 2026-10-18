package com.mxt.anitrend.adapter.recycler.detail

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
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.MediaMetadataRow

/** RecyclerView adapter for external link rows. */
class LinkAdapter(
    context: Context,
) : RecyclerViewAdapter<ExternalLink>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<ExternalLink> {
        val context = parent.context
        val cardView =
            CardViewBase(context).apply {
                id = R.id.container
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                isClickable = true
                isFocusable = true
                foreground = context.getDrawableFromAttr(android.R.attr.selectableItemBackground)
            }
        return LinkViewHolder(cardView)
    }

    override fun getFilter(): Filter? = null

    private inner class LinkViewHolder(
        private val cardView: CardViewBase,
    ) : RecyclerViewHolder<ExternalLink>(cardView) {
        private val linkSite = mutableStateOf("")
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
            bindLongClickListeners(R.id.container)
            composeView.setContent {
                AniTrendTheme {
                    MediaMetadataRow(
                        iconResource = R.drawable.ic_link_white_24dp,
                        label = linkSite.value,
                    )
                }
            }
        }

        override fun onBindViewHolder(model: ExternalLink) {
            linkSite.value = model.site.orEmpty()
        }

        override fun onViewRecycled() = Unit

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
