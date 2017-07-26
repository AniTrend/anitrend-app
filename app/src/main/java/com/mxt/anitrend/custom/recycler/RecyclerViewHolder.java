package com.mxt.anitrend.custom.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by max on 2017-04-10.
 */
public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {

    public RecyclerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    /**
     * Load image, text, buttons, etc. in this method from the given parameter
     * <br/>
     *
     * @param model Is the model at the current adapter position
     * @see T
     */
    public abstract void onBindViewHolder(T model);

    /**
     * If any image views are used within the view holder, clear any pending async img requests
     * by using Glide.Clear(ImageView)
     * <br/>
     * @see com.bumptech.glide.Glide
     */
    public abstract void onViewRecycled();

    @Override
    public abstract void onClick(View v);
}
