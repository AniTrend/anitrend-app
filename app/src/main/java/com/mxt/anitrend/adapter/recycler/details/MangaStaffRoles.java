package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.SeriesSmall;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.view.detail.activity.MangaActivity;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

/**
 * Created by max on 2017-04-10.
 */
public class MangaStaffRoles extends RecyclerViewAdapter<SeriesSmall> {

    private ApiPreferences apiPrefs;

    public MangaStaffRoles(List<SeriesSmall> mAdapter, Context mContext) {
        super(mAdapter, mContext);
        apiPrefs = new ApiPreferences(mContext);
    }

    @Override
    public RecyclerViewHolder<SeriesSmall> onCreateViewHolder(ViewGroup parent, int viewType) {
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

    class ViewHolder extends RecyclerViewHolder<SeriesSmall> {

        @BindView(R.id.relation_model_name)
        TextView romanji;
        @BindView(R.id.relation_model_rating)
        RatingBar rating;
        @BindView(R.id.relation_model_relation_type)
        TextView relation_type;
        @BindView(R.id.relation_model_image)
        ImageView model_image;

        ViewHolder(View view) {
            super(view);
            model_image.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(SeriesSmall model) {

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
            rating.setRating(((float) (model.getAverage_score()*5)/100));
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
                    SeriesSmall model = mAdapter.get(getAdapterPosition());
                    Intent starter = new Intent(mContext, MangaActivity.class);
                    starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    starter.putExtra(MangaActivity.MODEL_ID_KEY, model.getId());
                    starter.putExtra(MangaActivity.MODEL_BANNER_KEY, model.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }
    }

}
