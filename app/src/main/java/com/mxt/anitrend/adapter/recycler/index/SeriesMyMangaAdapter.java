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
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.api.structure.Manga;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.custom.event.SeriesInteractionListener;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.view.detail.activity.MangaActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by Maxwell on 12/23/2016.
 */
public class SeriesMyMangaAdapter extends RecyclerViewAdapter<ListItem> {

    private ApplicationPrefs mPrefs;
    private ApiPreferences mApiPres;
    private SeriesInteractionListener interactionListener;

    public SeriesMyMangaAdapter(List<ListItem> adapter, Context context, ApplicationPrefs prefs, ApiPreferences apiPrefs, SeriesInteractionListener event) {
        super(adapter, context);
        interactionListener = event;
        mApiPres = apiPrefs;
        mPrefs = prefs;
    }

    public SeriesMyMangaAdapter(List<ListItem> adapter, Context context, ApplicationPrefs prefs, ApiPreferences apiPrefs) {
        super(adapter, context);
        mApiPres = apiPrefs;
        mPrefs = prefs;
    }

    @Override
    public RecyclerViewHolder<ListItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(!mPrefs.isNewStyle()?R.layout.adapter_series_my_manga:R.layout.adapter_series_new_style, parent, false);
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
                        if(model.getManga().getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getManga().getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getManga().getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)) {
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

    private class CardViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        private TextView eng, romanji, volumes, chapters, publishing, popularity, rated, read_chaps, read_volumes;
        private ImageView image;
        private FrameLayout line;
        private CardView cardView;

        CardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            image = (ImageView)itemView.findViewById(R.id.img_lge);
            eng = (TextView)itemView.findViewById(R.id.txt_eng_title);
            romanji = (TextView)itemView.findViewById(R.id.txt_romanji);
            volumes = (TextView)itemView.findViewById(R.id.txt_manga_volumes);
            chapters = (TextView)itemView.findViewById(R.id.txt_manga_chapters);
            publishing = (TextView)itemView.findViewById(R.id.txt_publishing);
            popularity = (TextView)itemView.findViewById(R.id.txt_popularity);
            rated = (TextView)itemView.findViewById(R.id.txt_rated);
            line = (FrameLayout) itemView.findViewById(R.id.line);
            read_chaps = (TextView) itemView.findViewById(R.id.txt_manga_chapters_read);
            read_volumes = (TextView) itemView.findViewById(R.id.txt_manga_volumes_read);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getManga().getImage_url_lge(): model.getManga().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(image);

            eng.setText(model.getManga().getTitle_english());
            romanji.setText(model.getManga().getTitle_romaji());
            volumes.setText(String.valueOf(model.getManga().getTotal_volumes()));
            chapters.setText(model.getManga().getTotal_chapters() < 1?mContext.getString(R.string.TBA):String.valueOf(model.getManga().getTotal_chapters()));
            publishing.setText(model.getManga().getPublishing_status());
            popularity.setText(mContext.getString(R.string.text_popularity, model.getManga().getPopularity()));
            rated.setText(model.getScore());
            read_chaps.setText(String.valueOf(model.getChapters_read()));
            read_volumes.setText(String.valueOf(model.getVolumes_read()));

            if(model.getManga().getPublishing_status() != null)
                switch (model.getManga().getPublishing_status()) {
                    case "finished publishing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "publishing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet published":
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

    private class GridViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

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
            Glide.with(mContext).load(mPrefs.isHD()?model.getManga().getImage_url_lge(): model.getManga().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .centerCrop()
                    .into(image);

            switch (mApiPres.getTitleLanguage()) {
                case "romaji":
                    title.setText(model.getManga().getTitle_romaji());
                    break;
                case "english":
                    title.setText(model.getManga().getTitle_english());
                    break;
                case "japanese":
                    title.setText(model.getManga().getTitle_japanese());
                    break;
            }

            rated.setText(model.getScore());

            if(model.getManga().getPublishing_status() != null)
                switch (model.getManga().getPublishing_status()) {
                    case "finished publishing":
                        if(model.getChapters_read() == model.getManga().getTotal_chapters())
                            eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getChapters_read(),model.getManga().getTotal_chapters()));
                        else eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getChapters_read(),model.getManga().getTotal_chapters()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "publishing":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getChapters_read(),model.getManga().getTotal_chapters() < 1?"?":model.getManga().getTotal_chapters()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet published":
                        if(model.getManga().getTotal_chapters() == 0)
                            eps.setText(R.string.TBA);
                        else
                            eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getChapters_read(),model.getManga().getTotal_chapters() < 1?"?":model.getManga().getTotal_chapters()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
                        break;
                    case "cancelled":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s +", model.getChapters_read(),model.getManga().getTotal_chapters() < 1?"?":model.getManga().getTotal_chapters()));
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

    private class ExCardViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        private TextView eng, romanji, volumes, chapters, publishing, popularity, rated, read_chaps, read_volumes;
        private ImageView image;
        private FrameLayout line;
        private CardView cardView;

        ExCardViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            image = (ImageView)itemView.findViewById(R.id.img_lge);
            eng = (TextView)itemView.findViewById(R.id.txt_eng_title);
            romanji = (TextView)itemView.findViewById(R.id.txt_romanji);
            volumes = (TextView)itemView.findViewById(R.id.txt_manga_volumes);
            chapters = (TextView)itemView.findViewById(R.id.txt_manga_chapters);
            publishing = (TextView)itemView.findViewById(R.id.txt_publishing);
            popularity = (TextView)itemView.findViewById(R.id.txt_popularity);
            rated = (TextView)itemView.findViewById(R.id.txt_rated);
            line = (FrameLayout) itemView.findViewById(R.id.line);
            read_chaps = (TextView) itemView.findViewById(R.id.txt_manga_chapters_read);
            read_volumes = (TextView) itemView.findViewById(R.id.txt_manga_volumes_read);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getManga().getImage_url_lge(): model.getManga().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(image);

            eng.setText(model.getManga().getTitle_english());
            romanji.setText(model.getManga().getTitle_romaji());
            volumes.setText(String.valueOf(model.getManga().getTotal_volumes()));
            chapters.setText(model.getManga().getTotal_chapters() < 1?mContext.getString(R.string.TBA):String.valueOf(model.getManga().getTotal_chapters()));
            publishing.setText(model.getManga().getPublishing_status());
            popularity.setText(mContext.getString(R.string.text_popularity, model.getManga().getPopularity()));
            rated.setText(model.getScore());
            read_chaps.setText(String.valueOf(model.getChapters_read()));
            read_volumes.setText(String.valueOf(model.getVolumes_read()));

            if(model.getManga().getPublishing_status() != null)
                switch (model.getManga().getPublishing_status()) {
                    case "finished publishing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "publishing":
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet published":
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
            Manga mSeries = mAdapter.get(getAdapterPosition()).getManga();
            final Intent starter = new Intent(mContext, MangaActivity.class);
            switch (v.getId()) {
                case R.id.img_lge:
                    starter.putExtra(MangaActivity.MODEL_ID_KEY, mSeries.getId());
                    starter.putExtra(MangaActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
                    CircularAnim.fullActivity((FragmentActivity)mContext, v).colorOrImageRes(mPrefs.isLightTheme()?R.color.colorAccent_Ripple:R.color.colorDarkKnight).go(new CircularAnim.OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd() {
                            mContext.startActivity(starter);
                        }
                    });
                    break;
                case R.id.card_view:
                    starter.putExtra(MangaActivity.MODEL_ID_KEY, mSeries.getId());
                    starter.putExtra(MangaActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.img_lge:
                    new SeriesActionHelper(mContext, KeyUtils.MANGA, mAdapter.get(getAdapterPosition())).execute();
                    break;
                case R.id.card_view:
                    new SeriesActionHelper(mContext, KeyUtils.MANGA, mAdapter.get(getAdapterPosition())).execute();
                    break;
            }
            return true;
        }
    }

    private class ExGridViewHolder extends RecyclerViewHolder<ListItem> implements View.OnLongClickListener {

        private TextView title, eps, rated;
        private ImageView image;
        private View line;

        ExGridViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.txt_title);
            image = (ImageView)itemView.findViewById(R.id.img_lge);
            eps = (TextView)itemView.findViewById(R.id.txt_anime_eps);
            rated = (TextView)itemView.findViewById(R.id.txt_rated);
            line = itemView.findViewById(R.id.line);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(ListItem model) {
            Glide.with(mContext).load(mPrefs.isHD()?model.getManga().getImage_url_lge(): model.getManga().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .centerCrop()
                    .into(image);

            switch (mApiPres.getTitleLanguage()) {
                case "romaji":
                    title.setText(model.getManga().getTitle_romaji());
                    break;
                case "english":
                    title.setText(model.getManga().getTitle_english());
                    break;
                case "japanese":
                    title.setText(model.getManga().getTitle_japanese());
                    break;
            }

            rated.setText(model.getScore());

            if(model.getManga().getPublishing_status() != null)
                switch (model.getManga().getPublishing_status()) {
                    case "finished publishing":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getChapters_read(),model.getManga().getTotal_chapters()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateGreen));
                        break;
                    case "publishing":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getChapters_read(),model.getManga().getTotal_chapters() < 1?"?":model.getManga().getTotal_chapters()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateBlue));
                        break;
                    case "not yet published":
                        if(model.getManga().getTotal_chapters() == 0)
                            eps.setText(R.string.TBA);
                        else
                            eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getChapters_read(),model.getManga().getTotal_chapters() < 1?"?":model.getManga().getTotal_chapters()));
                        line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorStateOrange));
                        break;
                    case "cancelled":
                        eps.setText(String.format(Locale.getDefault(), "%s/%s", model.getChapters_read(),model.getManga().getTotal_chapters() < 1?"?":model.getManga().getTotal_chapters()));
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
            Manga mSeries = mAdapter.get(getAdapterPosition()).getManga();
            final Intent starter = new Intent(mContext, MangaActivity.class);
            switch (v.getId()) {
                case R.id.img_lge:
                    starter.putExtra(MangaActivity.MODEL_ID_KEY, mSeries.getId());
                    starter.putExtra(MangaActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
                    mContext.startActivity(starter);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.img_lge:
                    new SeriesActionHelper(mContext, KeyUtils.MANGA, mAdapter.get(getAdapterPosition())).execute();
                    break;
            }
            return true;
        }
    }
}
