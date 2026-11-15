package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.foundation.text.selection.SelectionState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.logging.LogEntryRow
import com.mxt.anitrend.util.KeyUtil

class LogEntryAdapter(context: Context) : RecyclerViewAdapter<LogEntry>(context) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<LogEntry> {
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
        return LogEntryHolder(composeView)
    }

    override fun getItemViewType(position: Int): Int = KeyUtil.RECYCLER_TYPE_CONTENT

    override fun getFilter(): Filter? = null

    inner class LogEntryHolder(
        private val composeView: ComposeView,
    ) : RecyclerViewHolder<LogEntry>(composeView) {
        internal val selectionState = SelectionState()
        private val level = mutableStateOf(LogEntry.Level.INFO)
        private val message = mutableStateOf("")

        init {
            composeView.setContent {
                AniTrendTheme {
                    LogEntryRow(
                        level = level.value,
                        message = message.value,
                        selectionState = selectionState,
                    )
                }
            }
        }

        override fun onBindViewHolder(model: LogEntry) {
            selectionState.clear()
            level.value = model.level
            message.value = model.message
        }

        override fun onViewRecycled() {
            selectionState.clear()
        }

        override fun onClick(v: View) = Unit

        override fun onLongClick(v: View): Boolean = false
    }
}
