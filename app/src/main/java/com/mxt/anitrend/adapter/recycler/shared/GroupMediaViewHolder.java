package com.mxt.anitrend.adapter.recycler.shared;

import android.view.View;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.AdapterMediaHeaderBinding;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.group.EntityGroup;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2018/03/26.
 * Group header for media items
 */

public class GroupMediaViewHolder extends RecyclerViewHolder<EntityGroup> {

    private AdapterMediaHeaderBinding binding;
    private ItemClickListener<EntityGroup> clickListener;

    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     */
    public GroupMediaViewHolder(AdapterMediaHeaderBinding binding, ItemClickListener<EntityGroup> clickListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.clickListener = clickListener;
    }

    /**
     * Load image, text, buttons, etc. in this method from the given parameter
     * <br/>
     *
     * @param entityGroup Is the model at the current adapter position
     */
    @Override
    public void onBindViewHolder(EntityGroup entityGroup) {
        Media model = (Media) entityGroup;
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

    /**
     * Handle any onclick events from our views
     * <br/>
     *
     * @param v the view that has been clicked
     * @see View.OnClickListener
     */
    @Override @OnClick(R.id.container)
    public void onClick(View v) {
        if(binding != null && binding.getModel() != null && getAdapterPosition() > -1)
            clickListener.onItemClick(v, binding.getModel());
    }

    @Override @OnLongClick(R.id.container)
    public boolean onLongClick(View view) {
        if(binding != null && binding.getModel() != null && getAdapterPosition() > -1)
            clickListener.onItemLongClick(view, binding.getModel());
        return true;
    }
}