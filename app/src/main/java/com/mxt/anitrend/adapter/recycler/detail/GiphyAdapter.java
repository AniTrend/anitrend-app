package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterGiphyBinding;
import com.mxt.anitrend.model.entity.giphy.Gif;
import com.mxt.anitrend.model.entity.giphy.Giphy;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.HashMap;
import java.util.List;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/12/09.
 */

public class GiphyAdapter extends RecyclerViewAdapter<Giphy> {

    public GiphyAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<Giphy> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GiphyViewHolder(AdapterGiphyBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class GiphyViewHolder extends RecyclerViewHolder<Giphy> {

        private AdapterGiphyBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public GiphyViewHolder(AdapterGiphyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.setOnClickListener(this);
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(Giphy model) {
            HashMap<String, Gif> giphy = model.getImages();
            Gif giphyImage;
            if(giphy.containsKey(KeyUtil.GIPHY_PREVIEW))
                giphyImage = giphy.get(KeyUtil.GIPHY_PREVIEW);
            else
                giphyImage = giphy.get(KeyUtil.GIPHY_ORIGINAL_ANIMATED);
            Glide.with(getContext()).load(giphyImage.getUrl())
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.giphyImage);

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
            Glide.with(getContext()).clear(binding.giphyImage);
            binding.giphyImage.onViewRecycled();
            binding.unbind();
        }

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick(R.id.giphy_image)
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.giphy_image)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }
}
