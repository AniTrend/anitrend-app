package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterCharacterBinding;
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import butterknife.OnClick;

/**
 * Created by max on 2017/12/20.
 */

public class GroupCharacterAdapter extends RecyclerViewAdapter<RecyclerItem> {

    public GroupCharacterAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<RecyclerItem> onCreateViewHolder(ViewGroup parent, @KeyUtil.RecyclerViewType int viewType) {
        if (viewType == KeyUtil.RECYCLER_TYPE_HEADER)
            return new GroupTitleViewHolder(AdapterEntityGroupBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(parent.getContext()), parent, false));
        return new CharacterViewHolder(AdapterCharacterBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public void onViewAttachedToWindow(RecyclerViewHolder<RecyclerItem> holder) {
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

    protected class CharacterViewHolder extends RecyclerViewHolder<RecyclerItem> {

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
         * @param recyclerItem Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(RecyclerItem recyclerItem) {
            CharacterBase model = (CharacterBase) recyclerItem;
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

        @Override @OnClick(R.id.container)
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
