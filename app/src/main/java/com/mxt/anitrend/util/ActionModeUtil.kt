package com.mxt.anitrend.util

import android.view.ActionMode
import android.widget.CheckBox
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.interfaces.event.ActionModeListener
import com.mxt.anitrend.extension.getCompatColorAttr

/**
 * Created by max on 2017/07/17.
 * Custom action mode holder class
 */
class ActionModeUtil<T>(
    private val modeListener: ActionModeListener?,
    private val isEnabled: Boolean,
) {
    private var actionMode: ActionMode? = null
    private lateinit var recyclerAdapter: RecyclerView.Adapter<*>
    val selectedItems: MutableList<T> = mutableListOf()

    fun setRecyclerAdapter(recyclerAdapter: RecyclerView.Adapter<*>) {
        this.recyclerAdapter = recyclerAdapter
    }

    fun isSelected(model: T): Boolean = selectedItems.contains(model)

    private fun stopActionMode() {
        actionMode?.finish()
        actionMode = null
    }

    private fun startActionMode(viewHolder: RecyclerViewHolder<T>) {
        if (selectedItems.isEmpty() && modeListener != null) {
            actionMode = viewHolder.itemView.startActionMode(modeListener)
        }
    }

    fun clearSelection() {
        stopActionMode()
        selectedItems.clear()
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun selectItem(
        viewHolder: RecyclerViewHolder<T>,
        objectItem: T,
    ) {
        startActionMode(viewHolder)

        selectedItems.add(objectItem)

        setBackgroundColor(viewHolder, true)

        val mode = actionMode
        if (modeListener != null && mode != null) {
            modeListener.onSelectionChanged(mode, selectedItems.size)
        }
    }

    private fun deselectItem(
        viewHolder: RecyclerViewHolder<T>,
        objectItem: T,
    ) {
        selectedItems.remove(objectItem)

        setBackgroundColor(viewHolder, false)

        val mode = actionMode
        if (modeListener != null && mode != null) {
            if (selectedItems.isEmpty()) {
                mode.finish()
                actionMode = null
            } else {
                modeListener.onSelectionChanged(mode, selectedItems.size)
            }
        }
    }

    fun onItemClick(
        viewHolder: RecyclerViewHolder<T>,
        objectItem: T,
    ): Boolean {
        if (!isEnabled || selectedItems.isEmpty()) {
            return false
        }
        if (isSelected(objectItem)) {
            deselectItem(viewHolder, objectItem)
        } else {
            selectItem(viewHolder, objectItem)
        }
        return true
    }

    fun onItemLongClick(
        viewHolder: RecyclerViewHolder<T>,
        objectItem: T,
    ): Boolean {
        if (!isEnabled) {
            return false
        }
        if (isSelected(objectItem)) {
            deselectItem(viewHolder, objectItem)
        } else {
            selectItem(viewHolder, objectItem)
        }
        return true
    }

    fun setBackgroundColor(
        viewHolder: RecyclerViewHolder<T>,
        isSelected: Boolean,
    ) {
        when {
            isSelected ->
                when (val itemView = viewHolder.itemView) {
                    is CardView ->
                        itemView.setCardBackgroundColor(
                            ContextCompat.getColor(viewHolder.getContext(), R.color.colorTextGrey2nd),
                        )
                    is CheckBox -> itemView.isChecked = true
                    else -> itemView.setBackgroundResource(R.drawable.selection_frame)
                }
            else ->
                when (val itemView = viewHolder.itemView) {
                    is CardView ->
                        itemView.setCardBackgroundColor(
                            viewHolder.getContext().getCompatColorAttr(R.attr.colorOnSurface),
                        )
                    is CheckBox -> itemView.isChecked = false
                    else -> itemView.setBackgroundResource(0)
                }
        }
    }

    fun selectAllItems(selectableItems: List<T>) {
        selectedItems.clear()
        selectedItems.addAll(selectableItems)
        recyclerAdapter.notifyDataSetChanged()
        val mode = actionMode
        if (modeListener != null && mode != null) {
            modeListener.onSelectionChanged(mode, selectedItems.size)
        }
    }

    fun getSelectionCount(): Int = selectedItems.size
}
