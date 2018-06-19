package com.mxt.anitrend.adapter.recycler.shared;

import android.view.View;

import com.annimon.stream.IntPair;
import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.AdapterMediaHeaderBinding;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2018/03/26.
 * Group header for media items
 */

public class GroupMediaViewHolder extends RecyclerViewHolder<RecyclerItem> {

    private AdapterMediaHeaderBinding binding;
    private ItemClickListener<RecyclerItem> clickListener;

    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     */
    public GroupMediaViewHolder(AdapterMediaHeaderBinding binding, ItemClickListener<RecyclerItem> clickListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.clickListener = clickListener;
    }

    /**
     * Load image, text, buttons, etc. in this method from the given parameter
     * <br/>
     *
     * @param recyclerItem Is the model at the current adapter position
     */
    @Override
    public void onBindViewHolder(RecyclerItem recyclerItem) {
        MediaBase model = (MediaBase) recyclerItem;
        binding.setModel(model);
        binding.seriesTitle.setTitle(model);
        binding.executePendingBindings();
    }

    /**
     * If any image views are used within the view holder, clear any pending async img requests
     * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
     * <br/>
     *
     * @see Glide
     */
    @Override
    public void onViewRecycled() {
        Glide.with(getContext()).clear(binding.seriesImage);
        binding.unbind();
    }

    @Override @OnClick(R.id.container)
    public void onClick(View v) {
        IntPair<Boolean> pair = isValidIndexPair();
        if(binding != null && binding.getModel() != null && isClickable(binding.getModel()) && pair.getSecond())
            clickListener.onItemClick(v, new IntPair<>(pair.getFirst(), binding.getModel()));
    }

    @Override @OnLongClick(R.id.container)
    public boolean onLongClick(View v) {
        IntPair<Boolean> pair = isValidIndexPair();
        if(binding != null && binding.getModel() != null && isLongClickable(binding.getModel()) && pair.getSecond()) {
            clickListener.onItemLongClick(v, new IntPair<>(pair.getFirst(), binding.getModel()));
            return true;
        }
        return false;
    }
}