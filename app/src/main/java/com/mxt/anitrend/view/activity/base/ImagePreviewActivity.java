package com.mxt.anitrend.view.activity.base;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/11/14.
 * ImagePreviewActivity
 */

public class ImagePreviewActivity extends ActivityBase<Void, BasePresenter> {

    @BindView(R.id.preview_image) PhotoView mImageView;
    @BindView(R.id.toolbar_preview_image) Toolbar mToolbar;

    private String mImageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        mImageView.setOnClickListener(view -> mToolbar.animate()
                .alpha(mToolbar.getAlpha() == 1 ? 0: 1)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(getIntent().hasExtra(KeyUtil.arg_model) && !TextUtils.isEmpty(getIntent().getStringExtra(KeyUtil.arg_model))) {
            mImageUri = getIntent().getStringExtra(KeyUtil.arg_model);
            Glide.with(this).load(mImageUri).into(mImageView);
        } else
            NotifyUtil.makeText(this, R.string.layout_empty_response, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.image_preview_download:
                if (requestPermissionIfMissing(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    downloadAttachment();
                else if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    DialogUtil.createMessage(this, R.string.title_permission_write, R.string.text_permission_write, (dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                                break;
                            case NEGATIVE:
                                NotifyUtil.makeText(this, R.string.canceled_by_user, Toast.LENGTH_SHORT).show();
                                break;
                        }
                    });
                return true;
            case R.id.image_preview_share:
            case R.id.action_share:
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, mImageUri);
                startActivity(Intent.createChooser(intent, getResources().getText(R.string.image_preview_share)));
                return true;
            case R.id.image_preview_link:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mImageUri));
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void downloadAttachment() {
        Uri imageUri = Uri.parse(mImageUri);
        DownloadManager.Request r = new DownloadManager.Request(imageUri);
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageUri.getLastPathSegment());
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Start download
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(r);
            NotifyUtil.createAlerter(this, R.string.title_download_info, R.string.text_download_info,
                    R.drawable.ic_cloud_download_white_24dp, R.color.colorStateGreen, KeyUtil.DURATION_SHORT);
        } else
            NotifyUtil.createAlerter(this, R.string.title_download_info, R.string.text_unknown_error,
                    R.drawable.ic_cloud_download_white_24dp, R.color.colorStateRed, KeyUtil.DURATION_SHORT);
    }

    /**
     * Called for each of the requested permissions as they are granted
     *
     * @param permission the current permission granted
     */
    @Override
    protected void onPermissionGranted(@NonNull String permission) {
        super.onPermissionGranted(permission);
        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            downloadAttachment();
    }
}
