package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterReviewBinding;
import com.mxt.anitrend.databinding.AdapterSeriesReviewBinding;
import com.mxt.anitrend.model.entity.anilist.Review;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/10/30.
 * Media review adapter
 */

public class ReviewAdapter extends RecyclerViewAdapter<Review> {

    private boolean isMediaType;

    public ReviewAdapter(Context context) {
        super(context);
    }

    public ReviewAdapter(Context context, boolean isMediaType) {
        super(context);
        this.isMediaType = isMediaType;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<Review> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(!isMediaType)
            return new ReviewBanner(AdapterReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return new ReviewDefault(AdapterSeriesReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class ReviewBanner extends RecyclerViewHolder<Review> {

        private AdapterReviewBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param view
         */
        public ReviewBanner(AdapterReviewBinding view) {
            super(view.getRoot());
            binding = view;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see Review
         */
        @Override
        public void onBindViewHolder(Review model) {
            binding.setModel(model);
            binding.seriesTitle.setTitle(model);
            binding.reviewVote.setModel(model, R.color.white);
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
            binding.reviewVote.onViewRecycled();
            binding.unbind();
        }

        @Override @OnClick({R.id.series_image, R.id.review_read_more})
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick(R.id.series_image)
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }

    protected class ReviewDefault extends RecyclerViewHolder<Review> {

        private AdapterSeriesReviewBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param view
         */
        public ReviewDefault(AdapterSeriesReviewBinding view) {
            super(view.getRoot());
            binding = view;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see Review
         */
        @Override
        public void onBindViewHolder(Review model) {
            binding.setModel(model);
            binding.seriesTitle.setTitle(model);
            binding.reviewVote.setModel(model, 0);
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
            binding.reviewVote.onViewRecycled();
            binding.unbind();
        }

        @Override @OnClick({R.id.review_read_more, R.id.user_avatar})
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick(R.id.series_image)
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
