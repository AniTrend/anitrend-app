package com.mxt.anitrend.view.base.activity;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.mxt.anitrend.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ImagePreviewActivity extends Activity {

    public static final String IMAGE_SOURCE = "model_image";
    private String imageURL;

    @BindView(R.id.preview_image) PhotoView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            View decorView = window.getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        ButterKnife.bind(this);
        if((imageURL = getIntent().getStringExtra(IMAGE_SOURCE)) != null){
            Glide.with(this).load(imageURL)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mImageView);
        }
        else {
            SuperActivityToast.create(this, new com.github.johnpersano.supertoasts.library.Style(), com.github.johnpersano.supertoasts.library.Style.TYPE_STANDARD)
                    .setText("Sorry, could not resolve content!")
                    .setTypefaceStyle(Typeface.NORMAL)
                    .setIconPosition(com.github.johnpersano.supertoasts.library.Style.ICONPOSITION_LEFT)
                    .setIconResource(R.drawable.ic_close_24dp)
                    .setDuration(com.github.johnpersano.supertoasts.library.Style.DURATION_MEDIUM)
                    .setFrame(com.github.johnpersano.supertoasts.library.Style.FRAME_STANDARD)
                    .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                    .setAnimations(com.github.johnpersano.supertoasts.library.Style.ANIMATIONS_SCALE).show();
        }
    }
}
