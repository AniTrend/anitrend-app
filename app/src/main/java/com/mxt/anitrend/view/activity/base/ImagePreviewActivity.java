package com.mxt.anitrend.view.activity.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/11/14.
 * ImagePreviewActivity
 */

public class ImagePreviewActivity extends ActivityBase<Void, BasePresenter> {

    @BindView(R.id.preview_image) PhotoView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        ButterKnife.bind(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(getIntent().hasExtra(KeyUtils.arg_model) && !TextUtils.isEmpty(getIntent().getStringExtra(KeyUtils.arg_model)))
            Glide.with(this).load(getIntent().getStringExtra(KeyUtils.arg_model)).into(mImageView);
        else
            NotifyUtil.makeText(this, R.string.layout_empty_response, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        updateUI();
    }

    @Override
    protected void updateUI() {

    }

    @Override
    protected void makeRequest() {

    }
}
