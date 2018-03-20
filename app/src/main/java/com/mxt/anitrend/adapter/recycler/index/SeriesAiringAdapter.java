package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterSeriesAiringBinding;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.general.MediaList;
import com.mxt.anitrend.util.SeriesUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/12/19.
 */

public class SeriesAiringAdapter extends RecyclerViewAdapter<MediaList> {

    private String currentUser;
    private List<MediaBase> favouriteSeries;

    public SeriesAiringAdapter(List<MediaList> data, Context context) {
        super(data, context);
        Favourite favourite = presenter.getFavourites();
        if(presenter.getApplicationPref().isAuthenticated())
            currentUser = presenter.getDatabase().getCurrentUser().getDisplay_name();
        if(favourite != null) {
            favouriteSeries = new ArrayList<>();
            if(favourite.getAnime() != null)
                favouriteSeries.addAll(favourite.getAnime());
            if(favourite.getManga() != null)
                favouriteSeries.addAll(favourite.getManga());
        }
    }

    @Override
    public RecyclerViewHolder<MediaList> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AiringViewHolder(AdapterSeriesAiringBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        /*return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filter = constraint.toString();
                if(filter.isEmpty()) {
                    data = clone;
                } else {
                    data = new ArrayList<>();
                    for (MediaList model : clone) {
                        if(model.getAnime() != null)
                            if(model.getAnime().getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    model.getAnime().getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    model.getAnime().getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)) {
                                data.add(model);
                            }
                            else if(model.getManga() != null)
                                if(model.getManga().getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                        model.getManga().getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                        model.getManga().getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)) {
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
        };*/
        return null;
    }

    protected class AiringViewHolder extends RecyclerViewHolder<MediaList> {

        private AdapterSeriesAiringBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public AiringViewHolder(AdapterSeriesAiringBinding binding) {
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
            binding.customRatingWidget.setFavourState(favouriteSeries != null && favouriteSeries.contains(SeriesUtil.getSeriesModel(model)));
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