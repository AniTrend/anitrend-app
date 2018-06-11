package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaListUtil;
import com.mxt.anitrend.util.MediaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2018/02/25.
 */

public class GroupSeriesListAdapter extends RecyclerViewAdapter<RecyclerItem> {

    private String currentUser;

    public GroupSeriesListAdapter(Context context) {
        super(context);
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<RecyclerItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == KeyUtil.RECYCLER_TYPE_HEADER)
            return new GroupTitleViewHolder(AdapterEntityGroupBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
        return new SeriesListViewHolder(AdapterSeriesListBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public @KeyUtil.RecyclerViewType
    int getItemViewType(int position) {
        return data.get(position).getContentType();
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
                    List<RecyclerItem> filteredList = new ArrayList<>();
                    for (RecyclerItem model : clone) {
                        if(model instanceof MediaList) {
                            if (MediaListUtil.isFilterMatch((MediaList) model, filter))
                                filteredList.add(model);
                        }
                    }
                    results.values = filteredList;
                }
                return results;
            }

            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results.values != null) {
                    data = (List<RecyclerItem>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    protected class SeriesListViewHolder extends RecyclerViewHolder<RecyclerItem> {

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
         * @param recyclerItem Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(RecyclerItem recyclerItem) {
            MediaList model = (MediaList) recyclerItem;
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

        @Override @OnClick(R.id.series_image)
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick(R.id.series_image)
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
