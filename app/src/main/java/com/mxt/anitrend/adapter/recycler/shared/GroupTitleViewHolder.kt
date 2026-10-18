package com.mxt.anitrend.adapter.recycler.shared

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.GroupTitleRow

/** RecyclerView holder for grouped section title rows. */
class GroupTitleViewHolder(
    parent: ViewGroup,
) : RecyclerViewHolder<RecyclerItem>(
    ComposeView(parent.context).apply {
        layoutParams =
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
                setMargins(margin, margin, margin, margin)
            }
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
        )
    },
) {
    private val composeView = itemView as ComposeView
    private val title = mutableStateOf("")
    private val count = mutableStateOf(0)

    init {
        composeView.setContent {
            AniTrendTheme {
                GroupTitleRow(
                    title = title.value,
                    count = count.value,
                )
            }
        }
    }

    override fun onBindViewHolder(model: RecyclerItem) {
        val header = model as? RecyclerHeaderItem ?: return
        title.value = header.getTitle()
        count.value = header.size
    }

    override fun onViewRecycled() = Unit

    override fun onClick(v: View) = Unit

    override fun onLongClick(v: View): Boolean = false
}
