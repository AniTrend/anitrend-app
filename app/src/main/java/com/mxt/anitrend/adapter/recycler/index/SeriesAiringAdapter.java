package com.mxt.anitrend.adapter.recycler.index;

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
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.custom.event.SeriesInteractionListener;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DateTimeConverter;

import java.util.List;
import java.util.Locale;

/**
 * Created by max on 2017/03/04.
 */
public class SeriesAiringAdapter extends RecyclerViewAdapter<ListItem> {

    private FragmentActivity mContext;
    private ApplicationPrefs mPrefs;
    private ApiPreferences mApiPrefs;
    private SeriesInteractionListener interactionListener;
    private final boolean isHD;

    public SeriesAiringAdapter(List<ListItem> adapter, FragmentActivity context, ApplicationPrefs prefs, ApiPreferences apiPrefs,SeriesInteractionListener callback) {
        super(adapter, context);
        mAdapter = adapter;
        mContext = context;
        interactionListener = callback;
        mApiPrefs = apiPrefs;
        mPrefs = prefs;
        isHD = mPrefs.isHD();
    }

    @Override
    public RecyclerViewHolder<ListItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mPrefs.isNewStyle()?R.layout.adapter_series_airing_v2:R.layout.adapter_series_anime_list, parent, false);
        return mPrefs.isNewStyle()?new GridViewHolder(view):new CardViewHolder(view);
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

    private class CardViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        //declare all view controls here:
        private TextView eng, romanji, type, eps, airing, popularity, starting, nxt_ep;
        private ImageView image;
        private FrameLayout line;
        private CardView cardView;

        CardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            image = (ImageView)itemView.findViewById(R.id.img_lge);
            eng = (TextView)itemView.findViewById(R.id.txt_eng_title);
            romanji = (TextView)itemView.findViewById(R.id.txt_romanji);
            type = (TextView)itemView.findViewById(R.id.txt_anime_type);
            eps = (TextView)itemView.findViewById(R.id.txt_anime_eps);
            airing = (TextView)itemView.findViewById(R.id.txt_airing);
            popularity = (TextView)itemView.findViewById(R.id.txt_popularity);
            starting = (TextView)itemView.findViewById(R.id.txt_startdate);
            line = (FrameLayout) itemView.findViewById(R.id.line);
            nxt_ep = (TextView) itemView.findViewById(R.id.txt_last_updated);
            image.setOnClickListener(this);
            cardView.setOnClickListener(this);
            image.setOnLongClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {

            eng.setText(model.getAnime().getTitle_english());
            romanji.setText(model.getAnime().getTitle_romaji());
            type.setText(model.getAnime().getType());
            eps.setText(String.format(Locale.getDefault(), "Watched %s/%s", model.getEpisodes_watched(),model.getAnime().getTotal_episodes() < 1?"?":model.getAnime().getTotal_episodes()));
            airing.setText(model.getAnime().getAiring_status());
            popularity.setText(mContext.getString(R.string.text_popularity, model.getAnime().getPopularity()));
            starting.setText(String.format("%s",model.getScore()));
            nxt_ep.setText(DateTimeConverter.getNextEpDate(model.getAnime().getAiring()));

            if(model.getAnime().getAiring().getNext_episode() - model.getEpisodes_watched() > 1)
                line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
            else
                line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorBlueGrey100));

            Glide.with(mContext).load(isHD?model.getAnime().getImage_url_lge(): model.getAnime().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(image);
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

    private class GridViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        //declare all view controls here:
        private TextView title, eps, info;
        private ImageView image;
        private View line;

        GridViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.txt_title);
            image = (ImageView)itemView.findViewById(R.id.img_lge);
            eps = (TextView)itemView.findViewById(R.id.txt_anime_eps);
            info = (TextView)itemView.findViewById(R.id.txt_anime_info);
            line = itemView.findViewById(R.id.line);
            eps.setOnClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {

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

            info.setText(DateTimeConverter.getNextEpDate(model.getAnime().getAiring()));
            eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getEpisodes_watched(),model.getAnime().getTotal_episodes() < 1?"?":model.getAnime().getTotal_episodes()));
            if(model.getAnime().getAiring().getNext_episode() - model.getEpisodes_watched() > 1)
                line.setVisibility(View.VISIBLE);
            else
                line.setVisibility(View.GONE);

            Glide.with(mContext).load(isHD?model.getAnime().getImage_url_lge(): model.getAnime().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .centerCrop()
                    .into(image);
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
}