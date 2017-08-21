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
import com.mxt.anitrend.api.model.CharacterSmall;
import com.mxt.anitrend.api.model.SeriesSmall;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;
import com.mxt.anitrend.view.detail.activity.CharacterActivity;

import java.util.List;

import butterknife.BindView;

/**
 * Created by max on 2017/08/21.
 */

public class AnimeActorRoles extends RecyclerViewAdapter<SeriesSmall> {

    private ApiPreferences apiPrefs;

    public AnimeActorRoles(List<SeriesSmall> mAdapter, Context mContext) {
        super(mAdapter, mContext);
        apiPrefs = new ApiPreferences(mContext);
    }

    @Override
    public RecyclerViewHolder<SeriesSmall> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AnimeActorRoles.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_actor_roles, parent, false));
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
        @BindView(R.id.relation_model_character)
        TextView relation_character;
        @BindView(R.id.relation_model_image)
        ImageView model_image;
        @BindView(R.id.relation_avatar)
        ImageView character_image;

        ViewHolder(View view) {
            super(view);
            model_image.setOnClickListener(this);
            character_image.setOnClickListener(this);
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
            if(model.getCharacters() != null) {
                CharacterSmall character = model.getCharacters().get(0);
                Glide.with(mContext).load(character.getImage_url_med())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(character_image);
                relation_type.setText(character.getRole());
                relation_character.setText(String.format("%s %s", character.getName_first(), character.getName_last()));
            } else {
                relation_type.setVisibility(View.GONE);
                relation_character.setVisibility(View.GONE);
                character_image.setVisibility(View.GONE);
            }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(model_image);
            Glide.clear(character_image);
        }

        @Override
        public void onClick(View v) {
            Intent starter;
            switch (v.getId()) {
                case R.id.relation_model_image:
                    SeriesSmall model = mAdapter.get(getAdapterPosition());
                    starter = new Intent(mContext, AnimeActivity.class);
                    starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    starter.putExtra(AnimeActivity.MODEL_ID_KEY, model.getId());
                    starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, model.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
                case R.id.relation_avatar:
                    CharacterSmall character = mAdapter.get(getAdapterPosition()).getCharacters().get(0);
                    starter = new Intent(mContext, CharacterActivity.class);
                    starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    starter.putExtra(CharacterActivity.CHARACTER_OBJECT_PARAM, character);
                    mContext.startActivity(starter);

                    break;
            }
        }
    }
}