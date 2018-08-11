package com.mxt.anitrend.util;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.widget.CheckBox;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.ActionModeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/07/17.
 * Custom action mode holder class
 */

public class ActionModeUtil<T> {

    private boolean isEnabled;
    private ActionMode mActionMode;
    private RecyclerView.Adapter recyclerAdapter;

    private final List<T> mSelectedItems;
    private final ActionModeListener modeListener;

    public ActionModeUtil(ActionModeListener modeListener, boolean isEnabled) {
        this.modeListener = modeListener;
        this.mSelectedItems = new ArrayList<>();
        this.isEnabled = isEnabled;
        this.mActionMode = null;
    }

    public void setRecyclerAdapter(RecyclerView.Adapter recyclerAdapter) {
        this.recyclerAdapter = recyclerAdapter;
    }

    public boolean isSelected(T model) {
        return mSelectedItems.contains(model);
    }

    private void stopActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    private void startActionMode(RecyclerViewHolder<T> viewHolder) {
        if (mSelectedItems.size() == 0 && modeListener != null)
            mActionMode = viewHolder.itemView.startActionMode(modeListener);
    }

    public void clearSelection() {
        stopActionMode();
        mSelectedItems.clear();
        recyclerAdapter.notifyDataSetChanged();
    }

    public List<T> getSelectedItems() {
        return mSelectedItems;
    }

    private void selectItem(RecyclerViewHolder<T> viewHolder, T objectItem) {
        startActionMode(viewHolder);

        mSelectedItems.add(objectItem);

        setBackgroundColor(viewHolder, true);

        if (modeListener != null && mActionMode != null)
            modeListener.onSelectionChanged(mActionMode, mSelectedItems.size());
    }

    private void deselectItem(RecyclerViewHolder<T> viewHolder, T objectItem) {
        mSelectedItems.remove(objectItem);

        setBackgroundColor(viewHolder, false);

        if (modeListener != null && mActionMode != null)
            if (mSelectedItems.size() == 0) {
                mActionMode.finish();
                mActionMode = null;
            } else
                modeListener.onSelectionChanged(mActionMode, mSelectedItems.size());
    }

    public boolean onItemClick(RecyclerViewHolder<T> viewHolder, T objectItem) {
        if (!isEnabled || mSelectedItems.size() == 0) {
            return false;
        } else {
            if (isSelected(objectItem))
                deselectItem(viewHolder, objectItem);
            else
                selectItem(viewHolder, objectItem);
            return true;
        }
    }

    public boolean onItemLongClick(RecyclerViewHolder<T> viewHolder, T objectItem) {
        if (!isEnabled)
            return false;
        if (isSelected(objectItem))
            deselectItem(viewHolder, objectItem);
        else
            selectItem(viewHolder, objectItem);
        return true;
    }

    public void setBackgroundColor(RecyclerViewHolder<T> viewHolder, boolean isSelected) {
        if (isSelected) {
            if (viewHolder.itemView instanceof CardView)
                ((CardView)viewHolder.itemView).setCardBackgroundColor(ContextCompat.getColor(viewHolder.getContext(), R.color.colorTextGrey2nd));
            else if (viewHolder.itemView instanceof CheckBox)
                ((CheckBox) viewHolder.itemView).setChecked(true);
            else
                viewHolder.itemView.setBackgroundResource(R.drawable.selection_frame);
        } else {
            if (viewHolder.itemView instanceof CardView)
                ((CardView)viewHolder.itemView).setCardBackgroundColor(CompatUtil.getColorFromAttr(viewHolder.getContext(), R.attr.cardColor));
            else if (viewHolder.itemView instanceof CheckBox)
                ((CheckBox) viewHolder.itemView).setChecked(false);
            else
                viewHolder.itemView.setBackgroundResource(0);
        }
    }

    public void selectAllItems(List<T> selectableItems) {
        mSelectedItems.clear();
        mSelectedItems.addAll(selectableItems);
        recyclerAdapter.notifyDataSetChanged();
        if (modeListener != null && mActionMode != null)
            modeListener.onSelectionChanged(mActionMode, mSelectedItems.size());
    }

    public int getSelectionCount() {
        return mSelectedItems.size();
    }
}