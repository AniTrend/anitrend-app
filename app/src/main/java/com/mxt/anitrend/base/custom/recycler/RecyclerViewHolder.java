package com.mxt.anitrend.base.custom.recycler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.util.ActionModeHelper;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by max on 2017/06/09.
 * Recycler view holder implementation
 */

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private ActionModeHelper<T> callback;

    /**
     * Default constructor which includes binding with butter knife
     */
    public RecyclerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    /**
     * Load image, text, buttons, etc. in this method from the given parameter
     * <br/>
     *
     * @param model Is the model at the current adapter position
     */
    public abstract void onBindViewHolder(T model);

    /**
     * If any image views are used within the view holder, clear any pending async img requests
     * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
     * <br/>
     * @see com.bumptech.glide.Glide
     */
    public abstract void onViewRecycled();

    /**
     * Handle any onclick events from our views
     * <br/>
     *
     * @param v the view that has been clicked
     * @see View.OnClickListener
     */
    @Override
    public abstract void onClick(View v);

    public @NonNull Context getContext() {
        return itemView.getContext().getApplicationContext();
    }

    /**
     * Applying selection styling on the desired item
     * @param model the current model item
     */
    void onBindSelectionState(T model) {
        if(callback != null)
            callback.setBackgroundColor(this, callback.isSelected(model));
    }

    /**
     * Constructs an int pair container with a boolean representing a valid adapter position
     * @return IntPair
     */
    protected @NonNull IntPair<Boolean> isValidIndexPair() {
        final int index = getAdapterPosition();
        return new IntPair<>(index, index != RecyclerView.NO_POSITION);
    }

    /**
     * Handle any onclick events from our views
     * <br/>
     *
     * @param v the view that has been clicked
     * @see View.OnClickListener
     */
    protected void performClick(ItemClickListener<T> clickListener, List<T> data, View v) {
        IntPair<Boolean> pair = isValidIndexPair();
        T model;
        if(pair.getSecond() && isClickable((model = data.get(pair.getFirst()))))
            clickListener.onItemClick(v, new IntPair<>(pair.getFirst(), model));
    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    protected boolean performLongClick(ItemClickListener<T> clickListener, List<T> data, View v) {
        IntPair<Boolean> pair = isValidIndexPair();
        T model;
        if(pair.getSecond() && isLongClickable((model = data.get(pair.getFirst())))) {
            clickListener.onItemLongClick(v, new IntPair<>(pair.getFirst(), model));
            return true;
        }
        return false;
    }

    protected boolean isClickable (T clicked) {
        return (callback == null || !callback.onItemClick(this, clicked));
    }

    protected boolean isLongClickable (T clicked) {
        return (callback == null || !callback.onItemLongClick(this, clicked));
    }

    void setActionMode(ActionModeHelper<T> actionModeHelper) {
        callback = actionModeHelper;
    }
}
