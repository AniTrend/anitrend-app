package com.mxt.anitrend.view.activity.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.view.image.WideImageView;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/22.
 * giphy preview activity
 */

public class GiphyPreviewActivity extends ActivityBase<Void, BasePresenter> implements RequestListener<Drawable> {

    @BindView(R.id.preview_image) PhotoView previewImage;
    @BindView(R.id.preview_progress) CircularProgressView previewProgress;
    @BindView(R.id.preview_credits) WideImageView previewCredits;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giphy_preview);
        ButterKnife.bind(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(getIntent().hasExtra(KeyUtil.arg_model) && !TextUtils.isEmpty(getIntent().getStringExtra(KeyUtil.arg_model)))
            Glide.with(this).load(getIntent().getStringExtra(KeyUtil.arg_model))
                    .listener(this).into(previewImage);
        else
            NotifyUtil.makeText(this, R.string.layout_empty_response, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
        onActivityReady();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        previewCredits.setImageResource(!CompatUtil.INSTANCE.isLightTheme(this) ? R.drawable.powered_by_giphy_light : R.drawable.powered_by_giphy_dark);
        updateUI();
    }

    @Override
    protected void updateUI() {

    }

    @Override
    protected void makeRequest() {

    }

    /**
     * Called when an exception occurs during a load, immediately before
     * {@link Target#onLoadFailed(Drawable)}. Will only be called if we currently want to display an
     * image for the given model in the given target. It is recommended to create a single instance
     * per activity/fragment rather than instantiate a new object for each call to {@code
     * Glide.load()} to avoid object churn.
     * <p>
     * <p> It is safe to reload this or a different model or change what is displayed in the target at
     * this point. For example:
     * <pre>
     * {@code
     * public void onLoadFailed(Exception e, T model, Target target, boolean isFirstResource) {
     *     target.setPlaceholder(R.drawable.a_specific_error_for_my_exception);
     *     Glide.load(model).into(target);
     * }
     * }
     * </pre>
     * </p>
     * <p>
     * <p> Note - if you want to reload this or any other model after an exception, you will need to
     * include all relevant builder calls (like centerCrop, placeholder etc).
     *
     * @param e               The maybe {@code null} exception containing information about why the
     *                        request failed.
     * @param model           The model we were trying to load when the exception occurred.
     * @param target          The {@link Target} we were trying to load the image into.
     * @param isFirstResource {@code true} if this exception is for the first resource to load.
     * @return {@code true} if the listener has handled updating the target for the given exception,
     * {@code false} to allow Glide's request to update the target.
     */
    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        return false;
    }

    /**
     * Called when a load completes successfully, immediately before {@link
     * Target#onResourceReady(Object, Transition)}.
     *
     * @param resource        The resource that was loaded for the target.
     * @param model           The specific model that was used to load the image.
     * @param target          The target the model was loaded into.
     * @param dataSource      The {@link DataSource} the resource was loaded from.
     * @param isFirstResource {@code true} if this is the first resource to in this load to be
     *                        loaded into the target. For example when loading a thumbnail and a
     *                        full-sized image, this will be {@code true} for the first image to
     *                        load and {@code false} for the second.
     * @return {@code true} if the listener has handled setting the resource on the target,
     * {@code false} to allow Glide's request to update the target.
     * Setting the resource includes handling animations, be sure to take that into account.
     */
    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        if(isAlive())
            previewProgress.setVisibility(View.GONE);
        return false;
    }
}
