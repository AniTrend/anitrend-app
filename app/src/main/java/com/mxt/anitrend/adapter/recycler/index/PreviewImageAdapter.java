package com.mxt.anitrend.adapter.recycler.index;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.graphics.Palette;
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
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.mxt.anitrend.R;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.custom.cardgallary.CardAdapterHelper;
import com.mxt.anitrend.util.PatternMatcher;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;

import java.util.List;

public class PreviewImageAdapter extends RecyclerViewAdapter<String> {

    private CardAdapterHelper mCardAdapterHelper = new CardAdapterHelper();
    private final Activity mAppReference;
    private final List<String> mTypes;

    public PreviewImageAdapter(List<String> mAdapter, List<String> mTypes, Activity mContext) {
        super(mAdapter, mContext.getApplicationContext());
        this.mAppReference = mContext;
        this.mTypes = mTypes;
    }

    @Override
    public RecyclerViewHolder<String> onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_card_image, parent, false);
        mCardAdapterHelper.onCreateViewHolder(parent, itemView);
        return new ViewHolder(itemView);
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

    private class ViewHolder extends RecyclerViewHolder<String> implements RequestListener<String, GlideDrawable>, Palette.PaletteAsyncListener {

        private ImageView mImageView;
        private FrameLayout mFrameLayout;
        private TextView mLabel;

        ViewHolder(final View view) {
            super(view);
            mFrameLayout = (FrameLayout) view.findViewById(R.id.preview_container);
            mImageView = (ImageView) view.findViewById(R.id.image_preview);
            mLabel = (TextView) view.findViewById(R.id.preview_label);
            mImageView.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(String url) {
            switch (mTypes.get(getAdapterPosition())) {
                case PatternMatcher.KEY_IMG:
                    Glide.with(mContext).load(url)
                            .listener(this)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(mImageView);
                    break;
                case PatternMatcher.KEY_YOU:
                    Glide.with(mContext)
                            .load(PatternMatcher.getYoutubeThumb(url))
                            .listener(this)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(mImageView);
                    mLabel.setText(R.string.text_play_video);
                    mLabel.setVisibility(View.VISIBLE);
                    break;
                default:
                    Glide.with(mContext).load(PatternMatcher.NO_THUMBNAIL)
                            .listener(this)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mImageView);
                    mLabel.setText(R.string.text_play_video);
                    mLabel.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(mImageView);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_preview:
                    String type = mTypes.get(getAdapterPosition());
                    Intent intent;
                    switch (type) {
                        case PatternMatcher.KEY_WEB:
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(mAdapter.get(getAdapterPosition())), "video/*");
                            mContext.startActivity(intent);
                            break;
                        case PatternMatcher.KEY_YOU:
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(mAdapter.get(getAdapterPosition())));
                            mContext.startActivity(intent);
                            break;
                        case PatternMatcher.KEY_IMG:
                            intent = new Intent(mContext, ImagePreviewActivity.class);
                            intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mAdapter.get(getAdapterPosition()));
                            TransitionHelper.startSharedImageTransition(mAppReference, v, mContext.getString(R.string.transition_image_preview), intent);
                            break;
                    }
                    break;
            }
        }

        /**
         * Called when an exception occurs during a load. Will only be called if we currently want to display an image
         * for the given model in the given target. It is recommended to create a single instance per activity/fragment
         * rather than instantiate a new object for each call to {@code Glide.load()} to avoid object churn.
         * <p>
         * <p>
         * It is safe to reload this or a different model or change what is displayed in the target at this point.
         * For example:
         * <pre>
         * {@code
         * public void onException(Exception e, T model, Target target, boolean isFirstResource) {
         *     target.setPlaceholder(R.drawable.a_specific_error_for_my_exception);
         *     Glide.load(model).into(target);
         * }
         * }
         * </pre>
         * </p>
         * <p>
         * <p>
         * Note - if you want to reload this or any other model after an exception, you will need to include all
         * relevant builder calls (like centerCrop, placeholder etc).
         * </p>
         *
         * @param e               The exception, or null.
         * @param model           The model we were trying to load when the exception occurred.
         * @param target          The {@link Target} we were trying to load the image into.
         * @param isFirstResource True if this exception is for the first resource to load.
         * @return True if the listener has handled updating the target for the given exception, false to allow
         * Glide's request to update the target.
         */
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

        /**
         * Called when a load completes successfully, immediately after
         * {@link Target#onResourceReady(Object, GlideAnimation)}.
         *
         * @param resource          The resource that was loaded for the target.
         * @param model             The specific model that was used to load the image.
         * @param target            The target the model was loaded into.
         * @param isFromMemoryCache True if the load completed synchronously (useful for determining whether or not to
         *                          animate)
         * @param isFirstResource   True if this is the first resource to in this load to be loaded into the target. For
         *                          example when loading a thumbnail and a fullsize image, this will be true for the first
         *                          image to load and false for the second.
         * @return True if the listener has handled setting the resource on the target (including any animations), false to
         * allow Glide's request to update the target (again including animations).
         */
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            try {
                Palette.Builder mBuilder = Palette.from(((GlideBitmapDrawable) resource.getCurrent()).getBitmap());
                mBuilder.generate(this);
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            }
            return false;
        }

        /**
         * Called when the {@link Palette} has been generated.
         *
         * @param palette
         */
        @Override
        public void onGenerated(Palette palette) {
            int color;
            if((color = palette.getDominantColor(0)) != 0) {
                mFrameLayout.setBackgroundColor(color);
            }
            else if((color = palette.getVibrantColor(0)) != 0) {
                mFrameLayout.setBackgroundColor(color);
            }
            else if((color = palette.getDarkVibrantColor(0)) != 0) {
                mFrameLayout.setBackgroundColor(color);
            }
            else if((color = palette.getDarkMutedColor(0)) != 0) {
                mFrameLayout.setBackgroundColor(color);
            }
        }
    }

}
