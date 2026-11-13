package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.staff.StaffRow
import com.mxt.anitrend.ui.staff.StaffRowUiState
import com.mxt.anitrend.util.KeyUtil

private val mediaStaffItemDiff = object : DiffUtil.ItemCallback<RecyclerItem>() {
    override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = when {
        oldItem is StaffBase && newItem is StaffBase -> oldItem.id == newItem.id
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
}

/** Displays staff associated with a media item, grouped by role. */
@Suppress("UNCHECKED_CAST")
class MediaStaffRoleAdapter(
    private val onStaffClick: (View, StaffBase) -> Unit,
) : ListAdapter<RecyclerItem, RecyclerView.ViewHolder>(mediaStaffItemDiff) {

    override fun getItemViewType(position: Int): Int = getItem(position).contentType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = if (viewType == KeyUtil.RECYCLER_TYPE_HEADER) {
        GroupTitleViewHolder(parent)
    } else {
        StaffViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onBindViewHolder(getItem(position))
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? RecyclerViewHolder<RecyclerItem>)?.onViewRecycled()
        super.onViewRecycled(holder)
    }

    private inner class StaffViewHolder(
        parent: ViewGroup,
    ) : RecyclerViewHolder<RecyclerItem>(
        ComposeView(parent.context).apply {
            id = R.id.container
            val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(margin, margin, margin, margin) }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        },
    ) {
        private val composeView = itemView as ComposeView
        private val staffState = mutableStateOf<StaffRowUiState?>(null, neverEqualPolicy())
        private var staff: StaffBase? = null

        init {
            composeView.setContent {
                AniTrendTheme {
                    staffState.value?.let { current ->
                        StaffRow(
                            state = current,
                            onClick = { this@StaffViewHolder.onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: RecyclerItem) {
            staff = model as? StaffBase
            staffState.value = staff?.let { StaffRowUiState.from(it) }
        }

        override fun onViewRecycled() {
            staffState.value = null
            staff = null
        }

        override fun onClick(v: View) {
            staff?.let { onStaffClick(v, it) }
        }

        override fun onLongClick(v: View): Boolean = false
    }
}
