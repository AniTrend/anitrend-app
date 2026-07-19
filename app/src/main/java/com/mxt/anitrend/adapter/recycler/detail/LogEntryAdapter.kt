package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.core.content.ContextCompat
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.databinding.AdapterLogEntryBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.log.LogEntry
import com.mxt.anitrend.util.KeyUtil

class LogEntryAdapter(context: Context) : RecyclerViewAdapter<LogEntry>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<LogEntry> =
        LogEntryHolder(
            AdapterLogEntryBinding.inflate(
                parent.context.getLayoutInflater(),
                parent,
                false,
            ),
        )

    override fun getItemViewType(position: Int): Int = KeyUtil.RECYCLER_TYPE_CONTENT

    override fun getFilter(): Filter? = null

    inner class LogEntryHolder(
        private val binding: AdapterLogEntryBinding,
    ) : RecyclerViewHolder<LogEntry>(binding.root) {

        override fun onBindViewHolder(model: LogEntry) {
            binding.logLevel.text = model.level.name
            binding.logMessage.text = model.message

            val colorRes = when (model.level) {
                LogEntry.Level.ERROR -> R.color.colorStateRed
                LogEntry.Level.WARNING -> R.color.colorStateOrange
                LogEntry.Level.INFO -> R.color.colorStateBlue
                LogEntry.Level.DEBUG -> R.color.colorAccent
                LogEntry.Level.VERBOSE -> R.color.colorTextGrey
            }
            binding.logLevel.setTextColor(ContextCompat.getColor(context, colorRes))
        }

        override fun onViewRecycled() {
            // No async resources to clear
        }

        override fun onClick(v: View) {
            // No click handling in the UI lane
        }

        override fun onLongClick(v: View): Boolean = false
    }
}
