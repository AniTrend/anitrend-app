package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterFeedSlideBinding;
import com.mxt.anitrend.util.RegexUtil;

import java.util.List;

/**
 * Created by max on 2017/11/25.
 * image preview adapter
 */

public class ImagePreviewAdapter extends RecyclerViewAdapter<String> {

    private final List<String> contentTypes;

    public ImagePreviewAdapter(List<String> contentTypes, Context context) {
        super(context);
        this.contentTypes = contentTypes;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreviewHolder(AdapterFeedSlideBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class PreviewHolder extends RecyclerViewHolder<String> {

        protected AdapterFeedSlideBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public PreviewHolder(AdapterFeedSlideBinding binding) {
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
        public void onBindViewHolder(String model) {
            String targetModel;
            boolean isCenterCrop = false;
            switch (contentTypes.get(getAdapterPosition()).toLowerCase()) {
                case RegexUtil.KEY_IMG:
                    targetModel = model;
                    ViewCompat.setTransitionName(binding.feedStatusImage, model);
                    binding.feedPlayBack.setVisibility(View.GONE);
                    break;
                case RegexUtil.KEY_YOU:
                    targetModel = RegexUtil.INSTANCE.getYoutubeThumb(model);
                    binding.feedPlayBack.setVisibility(View.VISIBLE);
                    isCenterCrop = true;
                    break;
                default:
                    targetModel = RegexUtil.NO_THUMBNAIL;
                    binding.feedPlayBack.setVisibility(View.VISIBLE);
                    isCenterCrop = true;
                    break;
            }

            if(!isCenterCrop)
                Glide.with(getContext()).load(targetModel)
                        .transition(DrawableTransitionOptions.withCrossFade(250))
                        .transform(
                                new CenterInside(),
                                new RoundedCorners(
                                        context.getResources().
                                                getDimensionPixelSize(R.dimen.md_margin)
                                )
                        )
                        .into(binding.feedStatusImage);
            else
                Glide.with(getContext()).load(targetModel)
                        .transition(DrawableTransitionOptions.withCrossFade(250))
                        .transform(
                                new CenterCrop(),
                                new RoundedCorners(
                                        context.getResources().
                                                getDimensionPixelSize(R.dimen.md_margin)
                                )
                        )
                        .into(binding.feedStatusImage);
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
            Glide.with(getContext()).clear(binding.feedStatusImage);
            binding.unbind();
        }

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
