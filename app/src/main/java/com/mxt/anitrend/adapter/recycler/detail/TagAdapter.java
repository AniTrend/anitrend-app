package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterTagBinding;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.util.CompatUtil;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2018/01/01.
 */

public class TagAdapter extends RecyclerViewAdapter<MediaTag> {

    public TagAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<MediaTag> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagViewHolder(AdapterTagBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class TagViewHolder extends RecyclerViewHolder<MediaTag> {

        private AdapterTagBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public TagViewHolder(AdapterTagBinding binding) {
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
        public void onBindViewHolder(MediaTag model) {
            binding.setModel(model);
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
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
