package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.Manga;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.view.detail.activity.MangaActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by max on 2017-04-10.
 */
public class MangaStaffRoles extends RecyclerViewAdapter<Manga> {

    private ApiPreferences apiPrefs;

    public MangaStaffRoles(List<Manga> mAdapter, Context mContext) {
        super(mAdapter, mContext);
        apiPrefs = new ApiPreferences(mContext);
    }

    @Override
    public RecyclerViewHolder<Manga> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_staff_roles, parent, false));
    }

    /**
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p>
     * <p>This method is usually implemented by {@link Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return null;
    }

    private class ViewHolder extends RecyclerViewHolder<Manga> {

        private TextView romanji, rating, relation_type;
        private ImageView model_image;

        public ViewHolder(View view) {
            super(view);
            romanji = (TextView) view.findViewById(R.id.relation_model_name);
            rating = (TextView) view.findViewById(R.id.relation_model_rating);
            relation_type = (TextView) view.findViewById(R.id.relation_model_relation_type);
            model_image = (ImageView) view.findViewById(R.id.relation_model_image);
            model_image.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(Manga model) {

            Glide.with(mContext).load(model.getImage_url_lge())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(model_image);

            switch (apiPrefs.getTitleLanguage()) {
                case "romaji":
                    romanji.setText(model.getTitle_romaji());
                    break;
                case "english":
                    romanji.setText(model.getTitle_english());
                    break;
                case "japanese":
                    romanji.setText(model.getTitle_japanese());
                    break;
            }
            rating.setText(String.format(Locale.getDefault(), "%.1f / 5", (model.getAverage_score()*5)/100));
            relation_type.setText(model.getRole());
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(model_image);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.relation_model_image:
                    Manga model = mAdapter.get(getAdapterPosition());
                    Intent starter = new Intent(mContext, MangaActivity.class);
                    starter.putExtra(MangaActivity.MODEL_ID_KEY, model.getId());
                    starter.putExtra(MangaActivity.MODEL_BANNER_KEY, model.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }
    }

}
