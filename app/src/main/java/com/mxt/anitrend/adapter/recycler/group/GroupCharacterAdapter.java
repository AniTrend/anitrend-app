package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterCharacterBinding;
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

import butterknife.OnClick;

/**
 * Created by max on 2017/12/20.
 */

public class GroupCharacterAdapter extends RecyclerViewAdapter<EntityGroup> {

    public GroupCharacterAdapter(List<EntityGroup> data, Context context) {
        super(data, context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<EntityGroup> onCreateViewHolder(ViewGroup parent, @KeyUtil.RecyclerViewType int viewType) {
        if (viewType == KeyUtil.RECYCLER_TYPE_HEADER)
            return new GroupTitleViewHolder(AdapterEntityGroupBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
        return new CharacterViewHolder(AdapterCharacterBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public void onViewAttachedToWindow(RecyclerViewHolder<EntityGroup> holder) {
        super.onViewAttachedToWindow(holder);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        if(getItemViewType(holder.getLayoutPosition()) == KeyUtil.RECYCLER_TYPE_HEADER)
            layoutParams.setFullSpan(true);
    }

    @Override
    public @KeyUtil.RecyclerViewType
    int getItemViewType(int position) {
        return data.get(position).getContentType();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class CharacterViewHolder extends RecyclerViewHolder<EntityGroup> {

        private AdapterCharacterBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public CharacterViewHolder(AdapterCharacterBinding binding) {
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
            CharacterBase model = (CharacterBase) entityGroup;
            binding.setModel(model);
            if(model.isFavourite())
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

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
