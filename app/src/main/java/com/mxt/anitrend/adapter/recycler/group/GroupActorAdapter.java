package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.AdapterMediaHeaderBinding;
import com.mxt.anitrend.databinding.AdapterSeriesStaffRoleBinding;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;

import butterknife.OnClick;
import butterknife.OnLongClick;

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

    @Override
    public RecyclerViewHolder<EntityGroup> onCreateViewHolder(ViewGroup parent, @KeyUtils.RecyclerViewType int viewType) {
        if (viewType == KeyUtils.RECYCLER_TYPE_HEADER)
            return new SeriesViewHolder(AdapterMediaHeaderBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
        return new StaffViewHolder(AdapterSeriesStaffRoleBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public @KeyUtils.RecyclerViewType int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class StaffViewHolder extends RecyclerViewHolder<EntityGroup> {

        private AdapterSeriesStaffRoleBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        StaffViewHolder(AdapterSeriesStaffRoleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param entityGroup Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(EntityGroup entityGroup) {
            MediaBase model = (MediaBase) entityGroup;
            binding.setModel(model);
            binding.seriesTitle.setTitle(model);
            binding.customRatingWidget.setFavourState(model.isFavourite());
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
            Glide.with(getContext()).clear(binding.characterImg);
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

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }

    protected class SeriesViewHolder extends RecyclerViewHolder<EntityGroup> {

        private AdapterMediaHeaderBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public SeriesViewHolder(AdapterMediaHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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
            int index;
            if((index = getAdapterPosition()) > -1)
                mediaClickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                mediaClickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }
}
