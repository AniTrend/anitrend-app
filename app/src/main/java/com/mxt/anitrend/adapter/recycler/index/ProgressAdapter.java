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
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.custom.bottomsheet.BottomSheet;
import com.mxt.anitrend.custom.bottomsheet.BottomSheetLikes;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;

import java.util.List;
import java.util.Locale;

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

    private class ViewHolder extends RecyclerViewHolder<UserActivity> implements View.OnLongClickListener {

        private View mLikesViewer;
        private ViewFlipper mFlipper;
        private ImageView mAvatar, mSeriesImage;
        private TextView mHeading, mLike, mComment, mMainUser, mTime, mContent;

        public ViewHolder(View view) {
            super(view);
            mLikesViewer = view.findViewById(R.id.likes_viewer);
            mFlipper = (ViewFlipper) view.findViewById(R.id.mFlipper);
            mAvatar = (ImageView) view.findViewById(R.id.feed_avatar);
            mSeriesImage = (ImageView) view.findViewById(R.id.feed_series_img);
            mHeading = (TextView) view.findViewById(R.id.feed_heading);
            mLike = (TextView) view.findViewById(R.id.feed_like);
            mComment = (TextView) view.findViewById(R.id.feed_comment);
            mMainUser = (TextView) view.findViewById(R.id.feed_main_user);
            mTime = (TextView) view.findViewById(R.id.feed_time);
            mContent = (TextView) view.findViewById(R.id.feed_content);
            mLikesViewer.setOnClickListener(this);
            mFlipper.setOnClickListener(this);
            mSeriesImage.setOnLongClickListener(this);
            mSeriesImage.setOnClickListener(this);
            mComment.setOnClickListener(this);
            mAvatar.setOnClickListener(this);
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


            Glide.with(mContext).load(mAppPrefs.isHD() ? model.getSeries().getImage_url_lge() : model.getSeries().getImage_url_med())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.toolbar_shadow)
                    .into(mSeriesImage);

            mHeading.setText(String.format(Locale.getDefault(), "%s\n\n%s", header, title));
            //We have to wait for the API end point to provide a more convent way get if the current user has a like in the feed post
            if (model.getLikes().contains(current))
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_red_900_18dp, 0, 0, 0);
            else
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_grey_600_18dp, 0, 0, 0);

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
                    if(series.getSeries_type().equals(FilterTypes.SeriesTypes[FilterTypes.SeriesType.ANIME.ordinal()]))
                        new SeriesActionHelper(mContext, FilterTypes.SeriesType.ANIME, series).execute();
                    else
                        new SeriesActionHelper(mContext, FilterTypes.SeriesType.MANGA, series).execute();
                    break;
            }
            return true;
        }
    }
}
