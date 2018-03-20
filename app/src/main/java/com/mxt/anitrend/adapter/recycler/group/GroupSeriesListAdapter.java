package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding;
import com.mxt.anitrend.databinding.AdapterSeriesListBinding;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.SeriesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2018/02/25.
 */

public class GroupSeriesListAdapter extends RecyclerViewAdapter<EntityGroup> {

    private String currentUser;
    private List<MediaBase> favouriteSeries;

    public GroupSeriesListAdapter(List<EntityGroup> data, Context context) {
        super(data, context);
        Favourite favourite = presenter.getFavourites();
        if(favourite != null) {
            favouriteSeries = new ArrayList<>();
            if(favourite.getAnime() != null)
                favouriteSeries.addAll(favourite.getAnime());
            if(favourite.getManga() != null)
                favouriteSeries.addAll(favourite.getManga());
        }
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public RecyclerViewHolder<EntityGroup> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == KeyUtils.RECYCLER_TYPE_HEADER)
            return new GroupTitleViewHolder(AdapterEntityGroupBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
        return new SeriesListViewHolder(AdapterSeriesListBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public @KeyUtils.RecyclerViewType
    int getItemViewType(int position) {
        return data.get(position).getContentType();
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
                    for (EntityGroup model : clone) {
                        if(model instanceof MediaList) {
                            MediaBase mediaBase = SeriesUtil.getSeriesModel((MediaList) model);

                            if (mediaBase.getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    mediaBase.getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                    mediaBase.getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter))
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
                data = new ArrayList<>((List<EntityGroup>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    protected class SeriesListViewHolder extends RecyclerViewHolder<EntityGroup> {

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
         * @param entityGroup Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(EntityGroup entityGroup) {
            MediaList model = (MediaList) entityGroup;
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
