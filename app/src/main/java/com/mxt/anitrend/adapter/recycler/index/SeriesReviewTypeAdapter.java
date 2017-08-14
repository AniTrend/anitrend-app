package com.mxt.anitrend.adapter.recycler.index;

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
import com.mxt.anitrend.api.model.Review;
import com.mxt.anitrend.api.structure.Anime;
import com.mxt.anitrend.api.structure.Manga;
import com.mxt.anitrend.api.structure.ReviewType;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;
import com.mxt.anitrend.view.detail.activity.MangaActivity;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;

import java.util.Locale;

import butterknife.BindView;

/**
 * Created by max on 2017/05/02.
 */

public class SeriesReviewTypeAdapter extends RecyclerViewAdapter<Review> {

    private ApplicationPrefs appPrefs;
    private ApiPreferences apiPrefs;
    private final boolean reviewType;

    public SeriesReviewTypeAdapter(ReviewType adapter, Context context, ApplicationPrefs appPrefs, ApiPreferences apiPrefs) {
        super(appPrefs.getReviewType()?adapter.getAnime():adapter.getManga(), context);
        this.appPrefs = appPrefs;
        this.apiPrefs = apiPrefs;
        this.reviewType = appPrefs.getReviewType();
    }

    @Override
    public RecyclerViewHolder<Review> onCreateViewHolder(ViewGroup parent, int viewType) {
        if(!appPrefs.isNewStyle())
            return new CardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_series_review_type_v2, parent, false));
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_series_review_type, parent, false));
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

    class ImageViewHolder extends RecyclerViewHolder<Review> implements View.OnClickListener {

        @BindView(R.id.img_lge) ImageView review_img;
        @BindView(R.id.review_user) TextView review_user;
        @BindView(R.id.review_rating) TextView review_rating;
        @BindView(R.id.review_heading) TextView review_heading;
        @BindView(R.id.review_summary) TextView review_summary;
        @BindView(R.id.review_up_score) TextView review_up_score;
        @BindView(R.id.review_down_score) TextView review_down_score;
        @BindView(R.id.review_read_more) TextView review_read_more;

        ImageViewHolder(View view) {
            super(view);
            review_img.setOnClickListener(this);
            review_read_more.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(Review model) {

            String title = "";
            String banner;

            if(reviewType) {
                Anime anime = model.getAnime();
                banner = anime.getImage_url_banner();
                if(banner == null)
                    banner = anime.getImage_url_lge();

                switch (apiPrefs.getTitleLanguage()) {
                    case "romaji":
                        title = anime.getTitle_romaji();
                        break;
                    case "english":
                        title = anime.getTitle_english();
                        break;
                    case "japanese":
                        title = anime.getTitle_japanese();
                        break;
                }
            } else {
                Manga manga = model.getManga();
                banner = manga.getImage_url_banner();
                if(banner == null)
                    banner = manga.getImage_url_lge();

                switch (apiPrefs.getTitleLanguage()) {
                    case "romaji":
                        title = manga.getTitle_romaji();
                        break;
                    case "english":
                        title = manga.getTitle_english();
                        break;
                    case "japanese":
                        title = manga.getTitle_japanese();
                        break;
                }
            }

            Glide.with(mContext).load(banner)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(review_img);

            review_up_score.setText(String.format(Locale.getDefault()," %d ",model.getRating()));
            review_down_score.setText(String.format(Locale.getDefault()," %d ",model.getRating_amount() - model.getRating()));

            review_heading.setText(title);
            review_summary.setText(model.getSummary());
            review_user.setText(mContext.getString(R.string.text_reviewed_by, model.getUser().getDisplay_name()));
            review_rating.setText(String.format(Locale.getDefault(), "%d / 100", model.getScore()));
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(review_img);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            switch (view.getId()){
                case R.id.review_read_more:
                    Review review = mAdapter.get(position);
                    new DialogManager(mContext).createDialogMessage(String.format(Locale.getDefault(),
                            mContext.getString(R.string.text_reviewed_by), review.getUser().getDisplay_name()), review.getText());
                    break;
                case R.id.img_lge:
                    Intent starter;
                    if(reviewType) {
                        starter = new Intent(mContext, AnimeActivity.class);
                        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        starter.putExtra(AnimeActivity.MODEL_ID_KEY, mAdapter.get(position).getAnime().getId());
                        starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, mAdapter.get(position).getAnime().getImage_url_banner());
                        mContext.startActivity(starter);
                    } else {
                        starter = new Intent(mContext, MangaActivity.class);
                        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        starter.putExtra(MangaActivity.MODEL_ID_KEY, mAdapter.get(position).getManga().getId());
                        starter.putExtra(MangaActivity.MODEL_BANNER_KEY, mAdapter.get(position).getManga().getImage_url_banner());
                        mContext.startActivity(starter);
                    }
                    break;
            }
        }
    }

    class CardViewHolder extends RecyclerViewHolder<Review> implements View.OnClickListener {

        @BindView(R.id.review_holder) View review_holder;
        @BindView(R.id.review_avatar) ImageView review_avatar;
        @BindView(R.id.review_series_img) ImageView review_series_img;
        @BindView(R.id.review_user) TextView review_user;
        @BindView(R.id.review_heading) TextView review_heading;
        @BindView(R.id.review_date) TextView review_date;
        @BindView(R.id.review_content) TextView review_content;
        @BindView(R.id.review_up_score) TextView review_up_score;
        @BindView(R.id.review_down_score) TextView review_down_score;
        @BindView(R.id.review_read_more) TextView review_read_more;

        CardViewHolder(View view) {
            super(view);
            review_avatar.setOnClickListener(this);
            review_holder.setOnClickListener(this);
            review_read_more.setOnClickListener(this);
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see T
         */
        @Override
        public void onBindViewHolder(Review model) {
            String title = "";
            String show;

            Glide.with(mContext).load(model.getUser().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(review_avatar);

            review_user.setText(String.format(Locale.getDefault(), mContext.getString(R.string.text_reviewed_by), model.getUser().getDisplay_name()));
            review_date.setText(model.getDate());

            review_content.setText(model.getSummary());

            review_up_score.setText(String.format(Locale.getDefault()," %d ",model.getRating()));
            review_down_score.setText(String.format(Locale.getDefault()," %d ",model.getRating_amount() - model.getRating()));

            if(reviewType) {
                Anime anime = model.getAnime();
                show = anime.getImage_url_lge();

                switch (apiPrefs.getTitleLanguage()) {
                    case "romaji":
                        title = anime.getTitle_romaji();
                        break;
                    case "english":
                        title = anime.getTitle_english();
                        break;
                    case "japanese":
                        title = anime.getTitle_japanese();
                        break;
                }
            } else {
                Manga manga = model.getManga();
                show = manga.getImage_url_lge();

                switch (apiPrefs.getTitleLanguage()) {
                    case "romaji":
                        title = manga.getTitle_romaji();
                        break;
                    case "english":
                        title = manga.getTitle_english();
                        break;
                    case "japanese":
                        title = manga.getTitle_japanese();
                        break;
                }
            }

            review_heading.setText(title);

            Glide.with(mContext).load(show)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(review_series_img);
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.Clear(ImageView)
         * <br/>
         *
         * @see Glide
         */
        @Override
        public void onViewRecycled() {
            Glide.clear(review_avatar);
            Glide.clear(review_series_img);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Intent starter;
            switch (v.getId()) {
                case R.id.review_avatar:
                    starter = new Intent(mContext, UserProfileActivity.class);
                    starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    starter.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mAdapter.get(position).getUser());
                    mContext.startActivity(starter);
                    break;
                case R.id.review_holder:
                    if(reviewType) {
                        starter = new Intent(mContext, AnimeActivity.class);
                        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        starter.putExtra(AnimeActivity.MODEL_ID_KEY, mAdapter.get(position).getAnime().getId());
                        starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, mAdapter.get(position).getAnime().getImage_url_banner());
                        mContext.startActivity(starter);
                    } else {
                        starter = new Intent(mContext, MangaActivity.class);
                        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        starter.putExtra(MangaActivity.MODEL_ID_KEY, mAdapter.get(position).getManga().getId());
                        starter.putExtra(MangaActivity.MODEL_BANNER_KEY, mAdapter.get(position).getManga().getImage_url_banner());
                        mContext.startActivity(starter);
                    }
                    break;
                case R.id.review_read_more:
                    Review review = mAdapter.get(position);
                    new DialogManager(mContext).createDialogMessage(String.format(Locale.getDefault(),
                            mContext.getString(R.string.text_reviewed_by), review.getUser().getDisplay_name()), review.getText());
                    break;
            }
        }
    }
}

