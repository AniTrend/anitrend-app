package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.shared.GroupMediaViewHolder;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.AdapterMediaHeaderBinding;
import com.mxt.anitrend.databinding.AdapterStaffBinding;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

import butterknife.OnClick;

/**
 * Created by max on 2018/03/23.
 * Character Actor Adapter
 */

public class GroupActorAdapter extends RecyclerViewAdapter<EntityGroup> {

    private ItemClickListener<EntityGroup> mediaClickListener;

    public GroupActorAdapter(List<EntityGroup> data, Context context) {
        super(data, context);
    }

    public void setMediaClickListener(ItemClickListener<EntityGroup> mediaClickListener) {
        this.mediaClickListener = mediaClickListener;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<EntityGroup> onCreateViewHolder(ViewGroup parent, @KeyUtil.RecyclerViewType int viewType) {
        if (viewType == KeyUtil.RECYCLER_TYPE_HEADER)
            return new GroupMediaViewHolder(AdapterMediaHeaderBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false), mediaClickListener);
        return new StaffViewHolder(AdapterStaffBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public @KeyUtil.RecyclerViewType int getItemViewType(int position) {
        return data.get(position).getContentType();
    }

    @Override
    public Filter getFilter() {
        return null;
    }


    protected class StaffViewHolder extends RecyclerViewHolder<EntityGroup> {

        private AdapterStaffBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public StaffViewHolder(AdapterStaffBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(EntityGroup model) {
            binding.setModel((StaffBase) model);
            if(((StaffBase)model).isFavourite())
                binding.favouriteIndicator.setVisibility(View.VISIBLE);
            else
                binding.favouriteIndicator.setVisibility(View.GONE);
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
            Glide.with(getContext()).clear(binding.staffImg);
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
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
