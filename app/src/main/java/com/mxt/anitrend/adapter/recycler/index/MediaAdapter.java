package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterAnimeBinding;
import com.mxt.anitrend.databinding.AdapterMangaBinding;
import com.mxt.anitrend.databinding.AdapterSeriesBinding;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/10/25.
 * Media adapter
 */

public class MediaAdapter extends RecyclerViewAdapter<MediaBase> {

    private boolean isCompatType;

    public MediaAdapter(List<MediaBase> data, Context context) {
        super(data, context);
    }

    public MediaAdapter(List<MediaBase> data, Context context, boolean isCompatType) {
        super(data, context);
        this.isCompatType = isCompatType;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<MediaBase> onCreateViewHolder(ViewGroup parent, @KeyUtil.RecyclerViewType int viewType) {
        if(isCompatType)
            return new MediaViewHolder(AdapterSeriesBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
        if(viewType == KeyUtil.RECYCLER_TYPE_ANIME)
            return new AnimeViewHolder(AdapterAnimeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return new MangaViewHolder(AdapterMangaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public @KeyUtil.RecyclerViewType int getItemViewType(int position) {
        if(CompatUtil.equals(data.get(position).getType(), KeyUtil.ANIME))
            return KeyUtil.RECYCLER_TYPE_ANIME;
        return KeyUtil.RECYCLER_TYPE_MANGA;
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
                    data = new ArrayList<>(Stream.of(clone).filter((model) -> model.getTitle().getEnglish().toLowerCase(Locale.getDefault()).contains(filter) ||
                            model.getTitle().getOriginal().toLowerCase(Locale.getDefault()).contains(filter) ||
                            model.getTitle().getRomaji().toLowerCase(Locale.getDefault()).contains(filter)).toList());
                }
                FilterResults results = new FilterResults();
                results.values = data;
                return results;
            }

            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = new ArrayList<>((List<Media>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    protected class AnimeViewHolder extends RecyclerViewHolder<MediaBase> {

        private AdapterAnimeBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         * @see ButterKnife
         */
        AnimeViewHolder(AdapterAnimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see Media
         */
        @Override
        public void onBindViewHolder(MediaBase model) {
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
            Glide.with(getContext()).clear(binding.seriesImage);
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
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }

    protected class MangaViewHolder extends RecyclerViewHolder<MediaBase> {

        private AdapterMangaBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param view
         */
        MangaViewHolder(AdapterMangaBinding view) {
            super(view.getRoot());
            this.binding = view;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see Media
         */
        @Override
        public void onBindViewHolder(MediaBase model) {
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
            Glide.with(getContext()).clear(binding.seriesImage);
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
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }

    protected class MediaViewHolder extends RecyclerViewHolder<MediaBase> {

        private AdapterSeriesBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public MediaViewHolder(AdapterSeriesBinding binding) {
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
        public void onBindViewHolder(MediaBase model) {
            binding.setModel(model);
            binding.seriesTitle.setTitle(model);
            binding.customRatingWidget.setFavourState(model.isFavourite());
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
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }
}
