package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
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
import com.mxt.anitrend.util.PatternMatcher;
import com.mxt.anitrend.view.base.activity.GalleryPreviewActivity;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.base.activity.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import butterknife.BindView;

import static com.mxt.anitrend.util.KeyUtils.MESSAGE;
import static com.mxt.anitrend.util.KeyUtils.PROGRESS;
import static com.mxt.anitrend.util.KeyUtils.STATUS;

/**
 * Created by max on 2017/08/12.
 */

public class PublicStatusAdapter extends RecyclerViewAdapter<UserActivity> {

    private MultiInteractionListener mListener;
    private ApiPreferences mApiPrefs;
    private Context mContext;

    private final UserSmall current;

    public PublicStatusAdapter(List<UserActivity> mAdapter, Context mContext, ApplicationPrefs mAppPrefs, ApiPreferences mApiPrefs, MultiInteractionListener mListener) {
        super(mAdapter, mContext);
        this.mContext = mContext;
        this.mListener = mListener;
        this.mApiPrefs = mApiPrefs;
        current = mAppPrefs.getMiniUser();
    }

    @Override
    public RecyclerViewHolder<UserActivity> onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean isStatus = viewType == STATUS || viewType == MESSAGE;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(isStatus?R.layout.adapter_status:R.layout.adapter_user_progress, parent, false);
        if(isStatus)
            return new StatusHolder(view);
        return new ProgressHolder(view);
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {
        if(mAdapter.get(position).getActivity_type().equals(KeyUtils.ActivityTypes[STATUS]))
            return STATUS;
        else if(mAdapter.get(position).getActivity_type().equals(KeyUtils.ActivityTypes[MESSAGE]))
            return MESSAGE;
        return PROGRESS;
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

    class ProgressHolder extends RecyclerViewHolder<UserActivity> implements View.OnLongClickListener {

        @BindView(R.id.likes_viewer) View mLikesViewer;
        @BindView(R.id.mFlipper)
        ViewFlipper mFlipper;
        @BindView(R.id.feed_avatar)
        ImageView mAvatar;
        @BindView(R.id.feed_series_img) ImageView mSeriesImage;
        @BindView(R.id.feed_heading)
        TextView mHeading;
        @BindView(R.id.feed_like) TextView mLike;
        @BindView(R.id.feed_comment) TextView mComment;
        @BindView(R.id.feed_main_user) TextView mMainUser;
        @BindView(R.id.feed_time) TextView mTime;
        @BindView(R.id.feed_content) TextView mContent;
        @BindView(R.id.feed_delete) TextView mDelete;

        ProgressHolder(View view) {
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

    class StatusHolder extends RecyclerViewHolder<UserActivity> {

        private ArrayList<String> mDesc;
        private ArrayList<String> mLinks;
        @BindView(R.id.mFlipper) ViewFlipper mFlipper;
        @BindView(R.id.status_avatar) ImageView mAvatar;
        @BindView(R.id.status_extra_img) ImageView mStatusExtra;
        @BindView(R.id.likes_viewer) View mLikesViewer;
        @BindView(R.id.status_extra_holder) View mStatusContainer;
        @BindView(R.id.status_user) TextView mUser;
        @BindView(R.id.status_time) TextView mTime;
        @BindView(R.id.status_like) TextView  mLike;
        @BindView(R.id.status_comment) TextView mComment;
        @BindView(R.id.status_text) TextView mContent;
        @BindView(R.id.status_edit) TextView mEdit;
        @BindView(R.id.status_delete) TextView mDelete;
        @BindView(R.id.status_extra_img_gallery) TextView mGallery;

        String type;

        StatusHolder(View view) {
            super(view);
            mContent.setMovementMethod(LinkMovementMethod.getInstance());
            mContent.setFocusable(false);
            mLikesViewer.setOnClickListener(this);
            mFlipper.setOnClickListener(this);
            mEdit.setOnClickListener(this);
            mDelete.setOnClickListener(this);
            mStatusExtra.setOnClickListener(this);
            mComment.setOnClickListener(this);
            mAvatar.setOnClickListener(this);
            mGallery.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(UserActivity model) {
            mLinks = new ArrayList<>();
            mDesc = new ArrayList<>();
            UserSmall user = model.getUsers().get(0);
            List<UserSmall> likes = model.getLikes();
            Matcher matcher = PatternMatcher.findMedia(model.getValue());

            mComment.setText(String.format(Locale.getDefault(), " %d", model.getReply_count()));
            mLike.setText(String.format(Locale.getDefault(), " %d", likes.size()));

            mTime.setText(model.getCreated_at());
            mUser.setText(user.getDisplay_name());
            mContent.setText(model.getSpannedValue());

            //We have to wait for the API end point to provide a more convent way get if the current user has a like in the feed post
            if(model.getLikes().contains(current))
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_red_900_18dp,0,0,0);
            else
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_grey_600_18dp,0,0,0);

            if(model.getUsers().contains(current)) {
                mDelete.setVisibility(View.VISIBLE);
                mEdit.setVisibility(View.VISIBLE);
            } else {
                mDelete.setVisibility(View.GONE);
                mEdit.setVisibility(View.GONE);
            }

            Glide.with(mContext).load(model.getUsers().get(0).getImage_url_med())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mAvatar);

            if(mFlipper.getDisplayedChild() != 0)
                mFlipper.showPrevious();

            while(matcher.find()) {
                int gc = matcher.groupCount();
                String tag = matcher.group(gc - 1);
                String media = matcher.group(gc);
                mDesc.add(tag);
                if(tag.equals(PatternMatcher.KEY_YOU))
                    mLinks.add(PatternMatcher.buildYoutube(media.replace("(", "").replace(")", "")));
                else
                    mLinks.add(media.replace("(", "").replace(")", ""));
            }
            int mCount = mLinks.size();

            mStatusContainer.setVisibility(mCount > 0?View.VISIBLE:View.GONE);
            if(mCount > 0) {
                mStatusContainer.setVisibility(View.VISIBLE);
                boolean isVideo = !mDesc.get(0).equals(PatternMatcher.KEY_IMG);
                if(isVideo) {
                    mGallery.setText((mCount > 1)?R.string.text_multiple_videos:R.string.text_play_video);
                    switch (mDesc.get(0)) {
                        case PatternMatcher.KEY_WEB:
                            Glide.with(mContext)
                                    .load(PatternMatcher.NO_THUMBNAIL)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(mStatusExtra);
                            break;
                        default:
                            Glide.with(mContext)
                                    .load(PatternMatcher.getYoutubeThumb(mLinks.get(0)))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(mStatusExtra);
                            break;
                    }
                }
                else {
                    Glide.with(mContext).load(mLinks.get(0))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .into(mStatusExtra);
                    mGallery.setText(R.string.text_multiple_images);
                }

                if (mCount > 1 || isVideo) {
                    mStatusExtra.setVisibility(View.VISIBLE);
                    mGallery.setVisibility(View.VISIBLE);
                }
                else {
                    mGallery.setVisibility(View.GONE);
                    mStatusExtra.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(mAvatar);
            if(mStatusExtra.getVisibility() == View.VISIBLE)
                Glide.clear(mStatusExtra);
        }

        @Override
        public void onClick(View view) {
            // Used to intercept some click handlers
            switch (view.getId())
            {
                case R.id.mFlipper:
                    if (mFlipper.getDisplayedChild() == 0) {
                        mFlipper.showNext();
                    }
                    else {
                        Toast.makeText(mContext, mContext.getString(R.string.busy_please_wait), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case R.id.likes_viewer:
                    try {
                        int size = mAdapter.get(getAdapterPosition()).getLikes().size();
                        if(size > 0) {
                            BottomSheet mSheet = BottomSheetLikes.newInstance(mContext.getString(R.string.title_bottom_sheet_likes, size), mAdapter.get(getAdapterPosition()).getLikes());
                            mSheet.show(((FragmentActivity)mContext).getSupportFragmentManager(), mSheet.getTag());
                        }
                        else
                            Toast.makeText(mContext, mContext.getString(R.string.text_no_likes), Toast.LENGTH_SHORT).show();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.status_extra_img_gallery:
                    handleAction();
                    return;
                case R.id.status_extra_img:
                    handleAction();
                    return;
            }
            mListener.onItemClick(getAdapterPosition(), view.getId());
        }

        void handleAction() {
            Intent intent;
            if (this.mLinks.size() > 1) {
                showSlide();
                return;
            }
            this.type = this.mDesc.get(0);
            if (this.type.equals(PatternMatcher.KEY_IMG)) {
                intent = new Intent(mContext, ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, this.mLinks.get(0));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return;
            }
            switch (type) {
                case PatternMatcher.KEY_WEB:
                    intent = new Intent(mContext, VideoPlayerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(VideoPlayerActivity.URL_VIDEO_LINK, mLinks.get(0));
                    break;
                default:
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(mLinks.get(0)));
                    break;
            }
            mContext.startActivity(intent);
        }

        void showSlide() {
            Intent preview = new Intent(mContext, GalleryPreviewActivity.class);
            preview.putStringArrayListExtra(GalleryPreviewActivity.PARAM_TYPE_LIST, mDesc);
            preview.putStringArrayListExtra(GalleryPreviewActivity.PARAM_IMAGE_LIST, mLinks);
            preview.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(preview);
        }
    }
}
