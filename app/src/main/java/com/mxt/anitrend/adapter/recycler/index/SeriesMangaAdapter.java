package com.mxt.anitrend.adapter.recycler.index;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DateTimeConverter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.detail.activity.MangaActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class SeriesMangaAdapter extends RecyclerViewAdapter<Series> {

    private ApplicationPrefs mPrefs;

    public SeriesMangaAdapter(List<Series> adapter, FragmentActivity context, ApplicationPrefs prefs) {
        super(adapter, context);
        mPrefs = prefs;
    }

    @Override
    public RecyclerViewHolder<Series> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_series_manga_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder<Series> holder, int position) {
        holder.onBindViewHolder(mAdapter.get(position));
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
                    for (Series model:mStore) {
                        if(model.getTitle_english().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getTitle_japanese().toLowerCase(Locale.getDefault()).contains(filter) ||
                                model.getTitle_romaji().toLowerCase(Locale.getDefault()).contains(filter)) {
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
                mAdapter = (List<Series>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerViewHolder<Series> implements View.OnLongClickListener {

        @BindView(R.id.img_lge) ImageView image;
        @BindView(R.id.txt_eng_title) TextView eng;
        @BindView(R.id.txt_romanji) TextView romanji;
        @BindView(R.id.txt_manga_type) TextView type;
        @BindView(R.id.txt_manga_chapters) TextView chapters;
        @BindView(R.id.txt_airing) TextView airing;
        @BindView(R.id.txt_popularity) TextView popularity;
        @BindView(R.id.txt_startdate) TextView starting;
        @BindView(R.id.txt_manga_volumes) TextView volumes;
        @BindView(R.id.line) FrameLayout line;
        @BindView(R.id.card_view) CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            image.setOnClickListener(this);
            cardView.setOnClickListener(this);
            image.setOnLongClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onBindViewHolder(Series model) {

            Glide.with(mContext).load(mPrefs.isHD()?model.getImage_url_lge(): model.getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(image);

            eng.setText(model.getTitle_english());
            romanji.setText(model.getTitle_romaji());
            type.setText(model.getType());
            chapters.setText(model.getTotal_chapters() < 1?mContext.getString(R.string.TBA):String.valueOf(model.getTotal_chapters()));
            airing.setText(model.getPublishing_status());
            popularity.setText(mContext.getString(R.string.text_popularity, model.getPopularity()));
            starting.setText(String.format("%s: %s", DateTimeConverter.getStartTitle(model.getStart_date_fuzzy()), DateTimeConverter.convertDate(model.getStart_date_fuzzy())));
            volumes.setText(model.getTotal_volumes() < 1?mContext.getString(R.string.TBA):String.valueOf(model.getTotal_volumes()));

            if(model.getPublishing_status() != null)
                switch (model.getPublishing_status()) {
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
            Series mSeries = mAdapter.get(getAdapterPosition());
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
                    if(mPrefs.isAuthenticated())
                        new SeriesActionHelper(mContext, KeyUtils.MANGA, mAdapter.get(getAdapterPosition())).execute();
                    else
                        Toast.makeText(mContext, mContext.getString(R.string.info_login_req), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.card_view:
                    if(mPrefs.isAuthenticated())
                        new SeriesActionHelper(mContext, KeyUtils.MANGA, mAdapter.get(getAdapterPosition())).execute();
                    else
                        Toast.makeText(mContext, mContext.getString(R.string.info_login_req), Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    }
}