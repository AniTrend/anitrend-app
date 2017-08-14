package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.base.custom.async.SeriesActionHelper;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.custom.view.widget.bottomsheet.BottomSheet;
import com.mxt.anitrend.base.custom.view.widget.bottomsheet.BottomSheetLikes;
import com.mxt.anitrend.base.interfaces.event.MultiInteractionListener;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

/**
 * Created by max on 2017/03/10.
 */
public class ProgressAdapter extends RecyclerViewAdapter<UserActivity> {

    private MultiInteractionListener mListener;
    private ApplicationPrefs mAppPrefs;
    private ApiPreferences mApiPrefs;
    private Context mContext;
    private final UserSmall current;

    public ProgressAdapter(List<UserActivity> mAdapter, Context mContext, ApplicationPrefs mAppPrefs, ApiPreferences mApiPrefs, MultiInteractionListener mListener) {
        super(mAdapter, mContext);
        this.mContext = mContext;
        this.mAppPrefs = mAppPrefs;
        this.mApiPrefs = mApiPrefs;
        this.mListener = mListener;
        current = mAppPrefs.getMiniUser();
    }

    @Override
    public RecyclerViewHolder<UserActivity> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_progress, parent, false);
        return new ViewHolder(view);
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

    class ViewHolder extends RecyclerViewHolder<UserActivity> implements View.OnLongClickListener {

        @BindView(R.id.likes_viewer) View mLikesViewer;
        @BindView(R.id.mFlipper) ViewFlipper mFlipper;
        @BindView(R.id.feed_avatar) ImageView mAvatar;
        @BindView(R.id.feed_series_img) ImageView mSeriesImage;
        @BindView(R.id.feed_heading) TextView mHeading;
        @BindView(R.id.feed_like) TextView mLike;
        @BindView(R.id.feed_comment) TextView mComment;
        @BindView(R.id.feed_main_user) TextView mMainUser;
        @BindView(R.id.feed_time) TextView mTime;
        @BindView(R.id.feed_content) TextView mContent;
        @BindView(R.id.feed_delete) TextView mDelete;

        public ViewHolder(View view) {
            super(view);
            mLikesViewer.setOnClickListener(this);
            mFlipper.setOnClickListener(this);
            mSeriesImage.setOnLongClickListener(this);
            mSeriesImage.setOnClickListener(this);
            mComment.setOnClickListener(this);
            mAvatar.setOnClickListener(this);
            mDelete.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(UserActivity model) {

            List<UserSmall> users = model.getUsers();
            List<UserSmall> likes = model.getLikes();

            Glide.with(mContext).load(model.getUsers().get(0).getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAvatar);

            String header;
            String content = null;
            String title = null;

            mMainUser.setText(users.get(0).getDisplay_name());
            mComment.setText(String.format(Locale.getDefault(), " %d", model.getReply_count()));
            mLike.setText(String.format(Locale.getDefault(), " %d", likes.size()));

            switch (mApiPrefs.getTitleLanguage()) {
                case "romaji":
                    title = model.getSeries().getTitle_romaji();
                    content = model.getSeries().getTitle_english();
                    break;
                case "english":
                    title = model.getSeries().getTitle_english();
                    content = model.getSeries().getTitle_romaji();
                    break;
                case "japanese":
                    title = model.getSeries().getTitle_japanese();
                    content = model.getSeries().getTitle_romaji();
                    break;
            }

            mContent.setText(content);
            mTime.setText(model.getCreated_at());
            if (users.size() > 1) {
                if (model.getValue().equals(""))
                    header = String.format(Locale.getDefault(), "%s & %d other users %s", model.getUsers().get(0).getDisplay_name(), users.size() - 1, model.getStatus());
                else
                    header = String.format(Locale.getDefault(), "%s & %d other users %s %s of:", model.getUsers().get(0).getDisplay_name(), users.size() - 1, model.getStatus(), model.getValue());
            } else {
                if (model.getValue().equals(""))
                    header = String.format(Locale.getDefault(), "%s %s:", model.getUsers().get(0).getDisplay_name(), model.getStatus());
                else
                    header = String.format(Locale.getDefault(), "%s %s %s of:", model.getUsers().get(0).getDisplay_name(), model.getStatus(), model.getValue());
            }

            Glide.with(mContext).load(model.getSeries().getImage_url_lge())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .into(mSeriesImage);

            mHeading.setText(String.format(Locale.getDefault(), "%s\n\n%s", header, title));
            //We have to wait for the API end point to provide a more convent way get if the current user has a like in the feed post
            if (model.getLikes().contains(current))
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_red_900_18dp, 0, 0, 0);
            else
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_grey_600_18dp, 0, 0, 0);

            mDelete.setVisibility(model.getUsers().contains(current)?View.VISIBLE:View.GONE);

            if(mFlipper.getDisplayedChild() != 0)
                mFlipper.showPrevious();
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(mAvatar);
            Glide.clear(mSeriesImage);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.mFlipper:
                    if (mFlipper.getDisplayedChild() == 0)
                        mFlipper.showNext();
                    else {
                        Toast.makeText(mContext, R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case R.id.likes_viewer:
                    int size = mAdapter.get(getAdapterPosition()).getLikes().size();
                    if(size > 0) {
                        BottomSheet mSheet = BottomSheetLikes.newInstance(mContext.getString(R.string.title_bottom_sheet_likes, size), mAdapter.get(getAdapterPosition()).getLikes());
                        mSheet.show(((FragmentActivity)mContext).getSupportFragmentManager(), mSheet.getTag());
                    }
                    else
                        Toast.makeText(mContext, mContext.getString(R.string.text_no_likes), Toast.LENGTH_SHORT).show();
                    return;
            }
            mListener.onItemClick(getAdapterPosition(), view.getId());
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.feed_series_img:
                    Series series = mAdapter.get(getAdapterPosition()).getSeries();
                    if(series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]))
                        new SeriesActionHelper(mContext, KeyUtils.ANIME, series).execute();
                    else
                        new SeriesActionHelper(mContext, KeyUtils.MANGA, series).execute();
                    break;
            }
            return true;
        }
    }
}
