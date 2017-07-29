package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.Anime;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.custom.event.SeriesInteractionListener;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by Maxwell on 11/20/2016.
 */
public class SeriesMyAnimeAdapter extends RecyclerViewAdapter<ListItem> {

    private ApplicationPrefs mPrefs;
    private ApiPreferences mApiPrefs;
    private SeriesInteractionListener interactionListener;

    public SeriesMyAnimeAdapter(List<ListItem> adapter, Context context, ApplicationPrefs prefs, ApiPreferences apiPrefs, SeriesInteractionListener event) {
        super(adapter, context);
        interactionListener = event;
        mApiPrefs = apiPrefs;
        mPrefs = prefs;
    }

    public SeriesMyAnimeAdapter(List<ListItem> adapter, Context context, ApplicationPrefs prefs, ApiPreferences apiPrefs) {
        super(adapter, context);
        mApiPrefs = apiPrefs;
        mPrefs = prefs;
    }

    @Override
    public RecyclerViewHolder<ListItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(!mPrefs.isNewStyle()?R.layout.adapter_series_my_anime:R.layout.adapter_series_new_style, parent, false);
        if(interactionListener != null)
            return !mPrefs.isNewStyle()?new CardViewHolder(view):new GridViewHolder(view);
        return !mPrefs.isNewStyle()?new ExCardViewHolder(view):new ExGridViewHolder(view);
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
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filter = constraint.toString();
                if(filter.isEmpty()) {
                    mAdapter = mStore;
                } else {
                    mAdapter = new ArrayList<>();
                    for (ListItem model:mStore) {
                        if(model.getAnime().getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getAnime().getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getAnime().getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)) {
                            mAdapter.add(model);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = mAdapter;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mAdapter = (List<ListItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class GridViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        @BindView(R.id.txt_title) TextView title;
        @BindView(R.id.txt_anime_eps) TextView eps;
        @BindView(R.id.txt_rated) TextView rated;
        @BindView(R.id.img_lge) ImageView image;
        @BindView(R.id.line) View line;

        GridViewHolder(View itemView) {
            super(itemView);
            eps.setOnClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getAnime().getImage_url_lge(): model.getAnime().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .centerCrop()
                    .into(image);

            switch (mApiPrefs.getTitleLanguage()) {
                case "romaji":
                    title.setText(model.getAnime().getTitle_romaji());
                    break;
                case "english":
                    title.setText(model.getAnime().getTitle_english());
                    break;
                case "japanese":
                    title.setText(model.getAnime().getTitle_japanese());
                    break;
            }

            rated.setText(model.getScore());

            if(model.getAnime().getAiring_status() != null)
                switch (model.getAnime().getAiring_status()) {
                    case "finished airing":
                        if(model.getEpisodes_watched() == model.getAnime().getTotal_episodes())
                            eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getEpisodes_watched(),model.getAnime().getTotal_episodes()));
                        else eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getEpisodes_watched(),model.getAnime().getTotal_episodes()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "currently airing":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getEpisodes_watched(),model.getAnime().getTotal_episodes() < 1?"?":model.getAnime().getTotal_episodes()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet aired":
                        eps.setText(R.string.TBA);
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
                        break;
                    case "cancelled":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getEpisodes_watched(),model.getAnime().getTotal_episodes() < 1?"?":model.getAnime().getTotal_episodes()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateRed));
                        break;
                }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(image);
        }

        @Override
        public void onClick(View v) {
            interactionListener.onClickSeries(mAdapter.get(getAdapterPosition()), v.getId());
        }

        @Override
        public boolean onLongClick(View v) {
            interactionListener.onLongClickSeries(mAdapter.get(getAdapterPosition()));
            return true;
        }
    }

    class CardViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        @BindView(R.id.card_view) CardView cardView;
        @BindView(R.id.img_lge) ImageView image;
        @BindView(R.id.txt_eng_title) TextView eng;
        @BindView(R.id.txt_romanji) TextView romanji;
        @BindView(R.id.txt_anime_type) TextView type;
        @BindView(R.id.txt_anime_eps) TextView eps;
        @BindView(R.id.txt_airing) TextView airing;
        @BindView(R.id.txt_popularity) TextView popularity;
        @BindView(R.id.txt_rated) TextView rated;
        @BindView(R.id.line) FrameLayout line;
        @BindView(R.id.txt_ep_watched) TextView watched;

        CardViewHolder(View itemView) {
            super(itemView);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getAnime().getImage_url_lge(): model.getAnime().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(image);

            eng.setText(model.getAnime().getTitle_english());
            romanji.setText(model.getAnime().getTitle_romaji());
            type.setText(model.getAnime().getType());
            eps.setText(model.getAnime().getTotal_episodes() < 1?mContext.getString(R.string.TBA):String.valueOf(model.getAnime().getTotal_episodes()));
            airing.setText(model.getAnime().getAiring_status());
            popularity.setText(mContext.getString(R.string.text_popularity, model.getAnime().getPopularity()));
            rated.setText(model.getScore());
            watched.setText(String.valueOf(model.getEpisodes_watched()));

            if(model.getAnime().getAiring_status() != null)
                switch (model.getAnime().getAiring_status()) {
                    case "finished airing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "currently airing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet aired":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
                        break;
                    case "cancelled":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateRed));
                        break;
                }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(image);
        }

        @Override
        public void onClick(View v) {
            interactionListener.onClickSeries(mAdapter.get(getAdapterPosition()), v.getId());
        }

        @Override
        public boolean onLongClick(View v) {
            interactionListener.onLongClickSeries(mAdapter.get(getAdapterPosition()));
            return true;
        }
    }

    class ExGridViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        @BindView(R.id.txt_title) TextView title;
        @BindView(R.id.img_lge) ImageView image;
        @BindView(R.id.txt_anime_eps) TextView eps;
        @BindView(R.id.txt_rated) TextView rated;
        @BindView(R.id.line) View line;

        ExGridViewHolder(View itemView) {
            super(itemView);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getAnime().getImage_url_lge(): model.getAnime().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .centerCrop()
                    .into(image);

            switch (mApiPrefs.getTitleLanguage()) {
                case "romaji":
                    title.setText(model.getAnime().getTitle_romaji());
                    break;
                case "english":
                    title.setText(model.getAnime().getTitle_english());
                    break;
                case "japanese":
                    title.setText(model.getAnime().getTitle_japanese());
                    break;
            }

            rated.setText(model.getScore());

            if(model.getAnime().getAiring_status() != null)
                switch (model.getAnime().getAiring_status()) {
                    case "finished airing":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getEpisodes_watched(),model.getAnime().getTotal_episodes()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "currently airing":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getEpisodes_watched(),model.getAnime().getTotal_episodes() < 1?"?":model.getAnime().getTotal_episodes()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet aired":
                        eps.setText(R.string.TBA);
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
                        break;
                    case "cancelled":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getEpisodes_watched(),model.getAnime().getTotal_episodes() < 1?"?":model.getAnime().getTotal_episodes()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateRed));
                        break;
                }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(image);
        }

        @Override
        public void onClick(View v) {
            Anime mSeries = mAdapter.get(getAdapterPosition()).getAnime();
            final Intent starter = new Intent(mContext, AnimeActivity.class);
            switch (v.getId()) {
                case R.id.img_lge:
                    starter.putExtra(AnimeActivity.MODEL_ID_KEY, mSeries.getId());
                    starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.img_lge:
                    new SeriesActionHelper(mContext, KeyUtils.ANIME, mAdapter.get(getAdapterPosition())).execute();
                    break;
            }
            return true;
        }
    }

    class ExCardViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        @BindView(R.id.card_view) CardView cardView;
        @BindView(R.id.img_lge) ImageView image;
        @BindView(R.id.txt_eng_title) TextView eng;
        @BindView(R.id.txt_romanji) TextView romanji;
        @BindView(R.id.txt_anime_type) TextView type;
        @BindView(R.id.txt_anime_eps) TextView eps;
        @BindView(R.id.txt_airing) TextView airing;
        @BindView(R.id.txt_popularity) TextView popularity;
        @BindView(R.id.txt_rated) TextView rated;
        @BindView(R.id.line) FrameLayout line;
        @BindView(R.id.txt_ep_watched) TextView watched;

        ExCardViewHolder(View itemView) {
            super(itemView);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getAnime().getImage_url_lge(): model.getAnime().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(image);

            eng.setText(model.getAnime().getTitle_english());
            romanji.setText(model.getAnime().getTitle_romaji());
            type.setText(model.getAnime().getType());
            eps.setText(model.getAnime().getTotal_episodes() < 1?mContext.getString(R.string.TBA):String.valueOf(model.getAnime().getTotal_episodes()));
            airing.setText(model.getAnime().getAiring_status());
            popularity.setText(mContext.getString(R.string.text_popularity, model.getAnime().getPopularity()));
            rated.setText(model.getScore());
            watched.setText(String.valueOf(model.getEpisodes_watched()));

            if(model.getAnime().getAiring_status() != null)
                switch (model.getAnime().getAiring_status()) {
                    case "finished airing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "currently airing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet aired":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
                        break;
                    case "cancelled":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateRed));
                        break;
                }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(image);
        }

        @Override
        public void onClick(View v) {
            Anime mSeries = mAdapter.get(getAdapterPosition()).getAnime();
            final Intent starter = new Intent(mContext, AnimeActivity.class);
            switch (v.getId()) {
                case R.id.img_lge:
                    starter.putExtra(AnimeActivity.MODEL_ID_KEY, mSeries.getId());
                    starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
                    CircularAnim.fullActivity((FragmentActivity)mContext, v).colorOrImageRes(mPrefs.isLightTheme()?R.color.colorAccent_Ripple:R.color.colorDarkKnight).go(new CircularAnim.OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd() {
                            mContext.startActivity(starter);
                        }
                    });
                    break;
                case R.id.card_view:
                    starter.putExtra(AnimeActivity.MODEL_ID_KEY, mSeries.getId());
                    starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.img_lge:
                    new SeriesActionHelper(mContext, KeyUtils.ANIME, mAdapter.get(getAdapterPosition())).execute();
                    break;
                case R.id.card_view:
                    new SeriesActionHelper(mContext, KeyUtils.ANIME, mAdapter.get(getAdapterPosition())).execute();
                    break;
            }
            return true;
        }
    }
}
