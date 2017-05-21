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
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.RecyclerViewHolder;
import com.mxt.anitrend.custom.bottomsheet.BottomSheet;
import com.mxt.anitrend.custom.bottomsheet.BottomSheetLikes;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.utils.ApiPreferences;
import com.mxt.anitrend.utils.ApplicationPrefs;
import com.mxt.anitrend.utils.PatternMatcher;
import com.mxt.anitrend.view.base.activity.GalleryPreviewActivity;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * Created by max on 2017/03/12.
 */

public class StatusAdapter extends RecyclerViewAdapter<UserActivity> {

    private MultiInteractionListener mListener;
    private ApiPreferences mApiPrefs;
    private Context mContext;

    private final UserSmall current;

    public StatusAdapter(List<UserActivity> mAdapter, Context mContext, ApplicationPrefs mAppPrefs, ApiPreferences mApiPrefs, MultiInteractionListener mListener) {
        super(mAdapter, mContext);
        this.mContext = mContext;
        this.mApiPrefs = mApiPrefs;
        this.mListener = mListener;
        current = mAppPrefs.getMiniUser();
    }

    @Override
    public RecyclerViewHolder<UserActivity> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mAdapter != null?mAdapter.size():0;
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

    private class ViewHolder extends RecyclerViewHolder<UserActivity> {

        private ArrayList<String> mDesc;
        private ArrayList<String> mLinks;
        private ViewFlipper mFlipper;
        private ImageView mAvatar, mStatusExtra;
        private View mLikesViewer, mStatusContainer;
        private TextView mUser, mTime, mLike, mComment, mContent, mEdit, mDelete, mGallery;
        private JCVideoPlayerStandard jcVideoPlayer;

        String type;

        public ViewHolder(View view) {
            super(view);
            mLikesViewer = view.findViewById(R.id.likes_viewer);
            mStatusContainer = view.findViewById(R.id.status_extra_holder);
            mFlipper = (ViewFlipper) view.findViewById(R.id.mFlipper);
            mAvatar = (ImageView) view.findViewById(R.id.status_avatar);
            mStatusExtra = (ImageView) view.findViewById(R.id.status_extra_img);
            mGallery = (TextView) view.findViewById(R.id.status_extra_img_gallery);
            mUser = (TextView) view.findViewById(R.id.status_user);
            mTime = (TextView) view.findViewById(R.id.status_time);
            mLike = (TextView) view.findViewById(R.id.status_like);
            mComment = (TextView) view.findViewById(R.id.status_comment);
            mContent = (TextView) view.findViewById(R.id.status_text);
            mEdit = (TextView) view.findViewById(R.id.status_edit);
            mDelete = (TextView) view.findViewById(R.id.status_delete);
            jcVideoPlayer = (JCVideoPlayerStandard) itemView.findViewById(R.id.embed_video_player);
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

            jcVideoPlayer.setVisibility(View.GONE);

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
                    switch (mDesc.get(0)) {
                        case PatternMatcher.KEY_WEB:
                            jcVideoPlayer.setUp(mLinks.get(0), JCVideoPlayer.SCREEN_LAYOUT_LIST, "Video posted by: "+user.getDisplay_name());
                            Glide.with(mContext)
                                    .load(PatternMatcher.NO_THUMBNAIL)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(jcVideoPlayer.thumbImageView);
                            break;
                        default:
                            mGallery.setText((mCount > 1)?R.string.text_multiple_videos:R.string.text_play_video);
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

                if (mCount > 1 || isVideo)
                    switch (mDesc.get(0)) {
                        case PatternMatcher.KEY_WEB:
                            jcVideoPlayer.setVisibility(View.VISIBLE);
                            mStatusExtra.setVisibility(View.GONE);
                            break;
                        default:
                            jcVideoPlayer.setVisibility(View.GONE);
                            mStatusExtra.setVisibility(View.VISIBLE);
                            mGallery.setVisibility(View.VISIBLE);
                            break;
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
            if(jcVideoPlayer.getVisibility() == View.VISIBLE) {
                Glide.clear(jcVideoPlayer.thumbImageView);
                jcVideoPlayer.release();
            }
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
                    type = mDesc.get(0);
                    if(!type.equals(PatternMatcher.KEY_IMG) && mLinks.size() < 2) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        switch (type) {
                            case PatternMatcher.KEY_WEB:
                                intent.setDataAndType(Uri.parse(mLinks.get(0)), "video/*");
                                break;
                            default:
                                intent.setData(Uri.parse(mLinks.get(0)));
                                break;
                        }
                        mContext.startActivity(intent);
                    }
                    else
                        showSlide();
                    return;
                case R.id.status_extra_img:
                    if(mLinks.size() > 1) {
                        showSlide();
                        return;
                    }
                    type = mDesc.get(0);
                    if(!type.equals(PatternMatcher.KEY_IMG)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        switch (type) {
                            case PatternMatcher.KEY_WEB:
                                intent.setDataAndType(Uri.parse(mLinks.get(0)), "video/*");
                                break;
                            default:
                                intent.setData(Uri.parse(mLinks.get(0)));
                                break;
                        }
                        mContext.startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(mContext, ImagePreviewActivity.class);
                        intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mLinks.get(0));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                    return;
            }
            mListener.onItemClick(getAdapterPosition(), view.getId());
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
