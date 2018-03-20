package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterReviewBinding;
import com.mxt.anitrend.databinding.AdapterSeriesReviewBinding;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.general.FeedReview;
import com.mxt.anitrend.util.ApplicationPref;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/10/30.
 * Media review adapter
 */

public class SeriesReviewAdapter extends RecyclerViewAdapter<Review> {

    private Boolean reviewType;

    public SeriesReviewAdapter(List<Review> data, Context context) {
        super(data, context);
    }

    public SeriesReviewAdapter(FeedReview data, Context context) {
        super(new ApplicationPref(context).getReviewType()?
                data.getAnime():data.getManga(), context);
        reviewType = presenter.getApplicationPref().getReviewType();
    }

    @Override
    public RecyclerViewHolder<Review> onCreateViewHolder(ViewGroup parent, int viewType) {
        if(reviewType != null)
            return new ReviewBanner(AdapterReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return new ReviewDefault(AdapterSeriesReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filter = constraint.toString();
                if(filter.isEmpty()) {
                    data = clone;
                } else {
                    data = new ArrayList<>(Stream.of(clone).filter((model) -> model.getUser().getDisplay_name().toLowerCase(Locale.getDefault()).contains(filter) || model.getAnime() != null? (
                            model.getAnime().getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    model.getAnime().getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    model.getAnime().getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)) :
                            model.getManga().getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    model.getManga().getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    model.getManga().getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)).toList());
                }
                FilterResults results = new FilterResults();
                results.values = data;
                return results;
            }

            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = new ArrayList<>((List<Review>) results.values);
                notifyDataSetChanged();
            }
        };
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

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick({R.id.series_image, R.id.review_read_more})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.series_image)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
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

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick({R.id.review_read_more, R.id.user_avatar})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.series_image)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }
}
