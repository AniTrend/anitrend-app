package com.mxt.anitrend.base.custom.recycler

import android.content.Context
import android.view.ViewGroup
import android.widget.Filter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener

/**
 * Presents one of several existing typed RecyclerViewAdapter implementations
 * through a single RecyclerView surface owned by a Fragment.
 */
class RecyclerSectionAdapter(
    context: Context,
    private val onItemClick: () -> ItemClickListener<Any>,
) : RecyclerViewAdapter<Any>(context) {
    private var delegate: RecyclerViewAdapter<Any>? = null

    /** Selects the typed adapter that supplies this section's views and data. */
    @Suppress("UNCHECKED_CAST")
    fun <T> select(adapter: RecyclerViewAdapter<T>) {
        delegate?.clearDataSet()
        delegate = adapter as RecyclerViewAdapter<Any>
        delegate?.setClickListener(onItemClick())
        delegate?.clearDataSet()
        super.clearDataSet()
    }

    /** Clears data from the currently selected section and its host adapter. */
    fun clearSelectedSection() {
        delegate?.clearDataSet()
        super.clearDataSet()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<Any> = requireNotNull(delegate).onCreateViewHolder(parent, viewType) as RecyclerViewHolder<Any>

    override fun getItemViewType(position: Int): Int = requireNotNull(delegate).getItemViewType(position)

    override fun onBindViewHolder(
        holder: RecyclerViewHolder<Any>,
        position: Int,
    ) {
        if (position < data.size) {
            holder.onBindViewHolder(data[position])
            holder.onBindSelectionState(data[position])
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolder<Any>) {
        holder.onViewRecycled()
    }

    override fun onItemsInserted(swap: List<Any>) {
        delegate?.onItemsInserted(swap)
        super.onItemsInserted(swap)
    }

    override fun onItemRangeInserted(swap: List<Any>) {
        delegate?.onItemRangeInserted(swap)
        super.onItemRangeInserted(swap)
    }

    override fun onItemChanged(
        swap: Any,
        position: Int,
    ) {
        delegate?.onItemChanged(swap, position)
        super.onItemChanged(swap, position)
    }

    override fun getFilter(): Filter? = null
}
