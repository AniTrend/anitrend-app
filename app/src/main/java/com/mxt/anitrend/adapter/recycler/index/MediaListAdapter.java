package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterSeriesAiringBinding;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.MediaListUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/11/03.
 * adapter for series lists
 */

public class MediaListAdapter extends RecyclerViewAdapter<MediaList> {

    private String currentUser;

    public MediaListAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<MediaList> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeriesListViewHolder(AdapterSeriesAiringBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(CompatUtil.isEmpty(clone))
                    clone = data;
                String filter = constraint.toString();
                if(TextUtils.isEmpty(filter)) {
                    results.values = new ArrayList<>(clone);
                    clone = null;
                } else {
                    results.values = Stream.of(clone)
                            .filter(c -> MediaListUtil.isFilterMatch(c, filter))
                            .toList();
                }
                return results;
            }

            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results.values != null) {
                    data = (List<MediaList>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    protected class SeriesListViewHolder extends RecyclerViewHolder<MediaList> {

        private AdapterSeriesAiringBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public SeriesListViewHolder(AdapterSeriesAiringBinding binding) {
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
        public void onBindViewHolder(MediaList model) {
            binding.setModel(model);
            binding.seriesTitle.setTitle(model);
            binding.seriesEpisodes.setModel(model, currentUser);
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
            binding.seriesEpisodes.onViewRecycled();
            binding.customRatingWidget.onViewRecycled();
            binding.unbind();
        }

        @Override @OnClick({R.id.series_image, R.id.container})
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick({R.id.series_image, R.id.container})
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
