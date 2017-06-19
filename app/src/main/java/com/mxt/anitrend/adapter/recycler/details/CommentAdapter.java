package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
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
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.UserActivityReply;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.RecyclerViewHolder;
import com.mxt.anitrend.custom.bottomsheet.BottomSheet;
import com.mxt.anitrend.custom.bottomsheet.BottomSheetLikes;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.PatternMatcher;
import com.mxt.anitrend.view.base.activity.GalleryPreviewActivity;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.base.activity.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by max on 2017/03/13.
 */
public class CommentAdapter extends RecyclerViewAdapter<UserActivityReply> {

    private MultiInteractionListener mListener;
    private ApplicationPrefs mAppPrefs;
    private ApiPreferences mApiPrefs;
    private final UserSmall current;
    private FragmentManager supportFragmentManager;

    public CommentAdapter(List<UserActivityReply> mAdapter, Context mContext, ApplicationPrefs mAppPrefs, ApiPreferences mApiPrefs, MultiInteractionListener mListener, FragmentManager supportFragmentManager) {
        super(mAdapter, mContext);
        this.mAppPrefs = mAppPrefs;
        this.mApiPrefs = mApiPrefs;
        this.mListener = mListener;
        current = mAppPrefs.getMiniUser();
        this.supportFragmentManager = supportFragmentManager;
    }

    @Override
    public RecyclerViewHolder<UserActivityReply> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_comment, parent, false);
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

    private class ViewHolder extends RecyclerViewHolder<UserActivityReply> implements View.OnClickListener {

        private ArrayList<String> mDesc;
        private ArrayList<String> mLinks;

        private View mLikesViewer, mPostContainer;
        private ViewFlipper mFlipper;
        private ImageView mAvatar, mPostExtra;
        private TextView mTime, mUsername, mContent, mLike, mReply, mEdit, mDelete, mGallery;
        String type;

        ViewHolder(View view) {
            super(view);
            mLikesViewer = view.findViewById(R.id.likes_viewer);
            mPostContainer = view.findViewById(R.id.post_extra_holder);
            mFlipper = (ViewFlipper) view.findViewById(R.id.mFlipper);
            mAvatar = (ImageView) view.findViewById(R.id.post_avatar);
            mPostExtra = (ImageView) view.findViewById(R.id.post_extra_img);
            mTime = (TextView) view.findViewById(R.id.post_time);
            mUsername = (TextView) view.findViewById(R.id.post_user_name);
            mContent = (TextView) view.findViewById(R.id.post_text);
            mLike = (TextView) view.findViewById(R.id.post_like);
            mReply = (TextView) view.findViewById(R.id.post_reply);
            mEdit = (TextView) view.findViewById(R.id.post_edit);
            mDelete = (TextView) view.findViewById(R.id.post_delete);
            mGallery = (TextView) view.findViewById(R.id.post_extra_img_gallery) ;
            mContent.setMovementMethod(LinkMovementMethod.getInstance());
            mContent.setFocusable(false);
            mLikesViewer.setOnClickListener(this);
            mFlipper.setOnClickListener(this);
            mReply.setOnClickListener(this);
            mEdit.setOnClickListener(this);
            mDelete.setOnClickListener(this);
            mAvatar.setOnClickListener(this);
            mPostExtra.setOnClickListener(this);
            mGallery.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(UserActivityReply model) {
            mLinks = new ArrayList<>();
            mDesc = new ArrayList<>();
            List<UserSmall> likes = model.getLikes();
            Matcher matcher = PatternMatcher.findMedia(model.getReply());

            mUsername.setText(model.getUser().getDisplay_name());
            mContent.setText(model.getReply_value());
            mLike.setText(String.format(Locale.getDefault(), " %d", likes.size()));
            mTime.setText(String.format(Locale.getDefault()," %s",model.getCreated_at()));
            //We have to wait for the API end point to provide a more convent way get if the current user has a like in the feed post
            if (likes.contains(current)) {
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_red_900_18dp, 0, 0, 0);
            }
            else {
                mLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_grey_600_18dp, 0, 0, 0);
            }

            if(model.getUser().equals(current)) {
                mReply.setVisibility(View.GONE); mEdit.setVisibility(View.VISIBLE); mDelete.setVisibility(View.VISIBLE);
            } else {
                mReply.setVisibility(View.VISIBLE); mEdit.setVisibility(View.GONE); mDelete.setVisibility(View.GONE);
            }

            Glide.with(mContext).load(model.getUser().getImage_url_med())
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

            mPostContainer.setVisibility(mCount > 0?View.VISIBLE:View.GONE);
            if(mCount > 0) if(mCount > 0) {
                mPostContainer.setVisibility(View.VISIBLE);
                boolean isVideo = !mDesc.get(0).equals(PatternMatcher.KEY_IMG);
                if(isVideo) {
                    mGallery.setText((mCount > 1)?R.string.text_multiple_videos:R.string.text_play_video);
                    switch (mDesc.get(0)) {
                        case PatternMatcher.KEY_WEB:
                            Glide.with(mContext)
                                    .load(PatternMatcher.NO_THUMBNAIL)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(mPostExtra);
                            break;
                        default:
                            Glide.with(mContext)
                                    .load(PatternMatcher.getYoutubeThumb(mLinks.get(0)))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(mPostExtra);
                            break;
                    }
                }
                else {
                    Glide.with(mContext).load(mLinks.get(0))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .into(mPostExtra);
                    mGallery.setText(R.string.text_multiple_images);
                }

                if (mCount > 1 || isVideo) {
                    mPostExtra.setVisibility(View.VISIBLE);
                    mGallery.setVisibility(View.VISIBLE);
                }
                else {
                    mGallery.setVisibility(View.GONE);
                    mPostExtra.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(mAvatar);
            Glide.clear(mPostExtra);
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
                        mSheet.show(supportFragmentManager, mSheet.getTag());
                    }
                    else
                        Toast.makeText(mContext, mContext.getString(R.string.text_no_likes), Toast.LENGTH_SHORT).show();
                    return;
                case R.id.post_extra_img_gallery:
                    handleAction();
                    return;
                case R.id.post_extra_img:
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
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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