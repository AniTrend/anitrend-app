package com.mxt.anitrend.adapter.recycler.details;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
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
import com.mxt.anitrend.api.structure.Relations;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.RecyclerViewHolder;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by Maxwell on 10/23/2016.
 * Relational Items Adapter
 */
public class AnimeDetailAdapter extends RecyclerViewAdapter<Relations> {

    private ApiPreferences apiPrefs;

    public AnimeDetailAdapter(List<Relations> adapter, FragmentActivity context) {
        super(adapter, context);
        apiPrefs = new ApiPreferences(context);
    }

    @Override
    public RecyclerViewHolder<Relations> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_relation_list, parent, false));
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

    private class ViewHolder extends RecyclerViewHolder<Relations> {

        private TextView romanji, type, rating, relation_type;
        private ImageView model_image;

        ViewHolder(View view) {
            super(view);
            romanji = (TextView) view.findViewById(R.id.relation_model_name);
            type = (TextView) view.findViewById(R.id.relation_model_type);
            rating = (TextView) view.findViewById(R.id.relation_model_rating);
            relation_type = (TextView) view.findViewById(R.id.relation_model_relation_type);
            model_image = (ImageView) view.findViewById(R.id.relation_model_image);
            model_image.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(Relations model) {
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
            type.setText(model.getType());
            rating.setText(String.format(Locale.getDefault(), "%.1f", (model.getAverage_score()*5)/100));
            relation_type.setText(model.getRelation_type());
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(model_image);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.relation_model_image:
                    Relations model = mAdapter.get(getAdapterPosition());
                    Intent starter = new Intent(mContext, AnimeActivity.class);
                    starter.putExtra(AnimeActivity.MODEL_ID_KEY, model.getId());
                    starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, model.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }
    }
}
