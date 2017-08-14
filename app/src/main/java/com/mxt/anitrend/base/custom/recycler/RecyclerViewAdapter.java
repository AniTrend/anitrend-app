package com.mxt.anitrend.base.custom.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Filterable;

import java.util.List;

/**
 * Created by max on 2017/03/09.
 */
public abstract class RecyclerViewAdapter <T> extends RecyclerView.Adapter<RecyclerViewHolder<T>> implements Filterable {

    protected Context mContext;
    protected List<T> mAdapter, mStore;

    protected RecyclerViewAdapter(List<T> mAdapter, Context mContext) {
        this.mContext = mContext;
        this.mAdapter = mAdapter;
        this.mStore = mAdapter;
    }

    protected RecyclerViewAdapter(List<T> mAdapter) {
        this.mAdapter = mAdapter;
    }

    public void onDataSetModified(List<T> swap) {
        mAdapter = swap;
        mStore = swap;
        notifyDataSetChanged();
    }

    public void refreshItem(int position) {
        notifyItemChanged(position);
    }

    public void onItemChanged(List<T> swap, int position) {
        mAdapter = swap;
        mStore = swap;
        notifyItemChanged(position);
    }

    public void onItemRemoved(List<T> swap, int position) {
        mAdapter = swap;
        mStore = swap;
        notifyItemRemoved(position);
    }

    @Override
    public abstract RecyclerViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * Calls the the recycler view holder to perform view binding
     * @see RecyclerViewHolder
     * <br/><br/>
     * default implemation is already done for you
     */
    @Override
    public void onBindViewHolder(RecyclerViewHolder<T> holder, int position) {
        holder.onBindViewHolder(mAdapter.get(position));
    }

    /**
     * Calls the the recycler view holder impl to perform view recycling
     * @see RecyclerViewHolder
     * <br/><br/>
     * default implemation is already done for you
     */
    @Override
    public void onViewRecycled(RecyclerViewHolder<T> holder) {
        holder.onViewRecycled();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * <br/>
     * The default method has already been implemented for you.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mAdapter != null?mAdapter.size():0;
    }
}
