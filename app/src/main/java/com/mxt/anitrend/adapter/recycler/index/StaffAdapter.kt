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
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.staff.StaffRow
import com.mxt.anitrend.ui.staff.StaffRowUiState

/**
 * Created by max on 2017/12/20.
 * StaffAdapter
 */
class StaffAdapter(
    context: Context,
) : RecyclerViewAdapter<StaffBase>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<StaffBase> = StaffViewHolder(parent)

    override fun getFilter(): Filter? = null

    inner class StaffViewHolder(
        parent: ViewGroup,
    ) : RecyclerViewHolder<StaffBase>(
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
        private val staff = mutableStateOf<StaffRowUiState?>(null, neverEqualPolicy())

        init {
            composeView.setContent {
                AniTrendTheme {
                    staff.value?.let { current ->
                        StaffRow(
                            state = current,
                            onClick = { this@StaffViewHolder.onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: StaffBase) {
            staff.value = StaffRowUiState.from(model)
        }

        override fun onViewRecycled() {
            staff.value = null
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
