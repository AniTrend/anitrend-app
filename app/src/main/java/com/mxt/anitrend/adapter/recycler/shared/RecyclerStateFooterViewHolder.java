package com.mxt.anitrend.adapter.recycler.shared;

import android.view.View;

import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.CustomRecyclerLoadingFooterBinding;

/**
 * Created by max on 2018/03/25.
 * LoadingFooterViewHolder
 */

public class LoadingFooterViewHolder<T> extends RecyclerViewHolder<T> {

    private CustomRecyclerLoadingFooterBinding binding;

    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     */
    public LoadingFooterViewHolder(CustomRecyclerLoadingFooterBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }


    @Override
    public void onBindViewHolder(T model) {
        binding.executePendingBindings();
    }

    @Override
    public void onViewRecycled() {
        binding.unbind();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
