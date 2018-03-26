package com.mxt.anitrend.adapter.recycler.shared;

import android.view.View;

import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.model.entity.group.EntityHeader;

/**
 * Created by max on 2018/02/18.
 */

public class GroupTitleViewHolder extends RecyclerViewHolder<EntityGroup> {

    private AdapterEntityGroupBinding binding;

    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     */
    public GroupTitleViewHolder(AdapterEntityGroupBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void onBindViewHolder(EntityGroup model) {
        binding.setModel((EntityHeader) model);
        if (((EntityHeader) model).getSize() < 1)
            binding.catalogHeaderCount.setVisibility(View.GONE);
        else
            binding.catalogHeaderCount.setVisibility(View.VISIBLE);
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
