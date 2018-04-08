package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterSeriesListBinding;
import com.mxt.anitrend.model.entity.anilist.MediaList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/11/03.
 * adapter for series lists
 */

public class MediaListAdapter extends RecyclerViewAdapter<MediaList> {

    private String currentUser;

    public MediaListAdapter(List<MediaList> data, Context context) {
        super(data, context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<MediaList> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SeriesListViewHolder(AdapterSeriesListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
                    data = new ArrayList<>();
                    for (MediaList model : clone) {
                        if (model.getMedia().getTitle().getEnglish().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getMedia().getTitle().getRomaji().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getMedia().getTitle().getOriginal().toLowerCase(Locale.getDefault()).contains(filter)) {
                            data.add(model);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = data;
                return results;
            }

            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = new ArrayList<>((List<MediaList>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    protected class SeriesListViewHolder extends RecyclerViewHolder<MediaList> {

        private AdapterSeriesListBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public SeriesListViewHolder(AdapterSeriesListBinding binding) {
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
            binding.unbind();
        }

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick(R.id.series_image)
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
