package com.mxt.anitrend.view.base.activity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.PreviewImageAdapter;
import com.mxt.anitrend.base.custom.view.widget.cardgallary.CardScaleHelper;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017-04-07.
 */
public class GalleryPreviewActivity extends DefaultActivity {

    @BindView(R.id.speedRecycler) RecyclerView mRecyclerView;
    private CardScaleHelper mCardScaleHelper = null;

    public final static String PARAM_IMAGE_LIST = "param_image_list";
    public final static String PARAM_TYPE_LIST = "param_type_list";

    private final String IMAGE_LIST_KEY = "image_list_key";
    private final String TYPE_LIST_KEY = "image_type_key";

    private List<String> mImages, mTypes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setTransparentStatusBar();
        ButterKnife.bind(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(getIntent().hasExtra(PARAM_IMAGE_LIST))
            mImages = getIntent().getStringArrayListExtra(PARAM_IMAGE_LIST);
        if(getIntent().hasExtra(PARAM_TYPE_LIST))
            mTypes = getIntent().getStringArrayListExtra(PARAM_TYPE_LIST);
        if(savedInstanceState != null) {
            mImages = savedInstanceState.getStringArrayList(IMAGE_LIST_KEY);
            mTypes = savedInstanceState.getStringArrayList(TYPE_LIST_KEY);
        }
        updateUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
    }

    @Override
    protected void updateUI() {
        if(mImages == null) {
            SuperActivityToast.create(this, new com.github.johnpersano.supertoasts.library.Style(), com.github.johnpersano.supertoasts.library.Style.TYPE_STANDARD)
                    .setText(getString(R.string.layout_empty_response))
                    .setTypefaceStyle(Typeface.NORMAL)
                    .setIconPosition(com.github.johnpersano.supertoasts.library.Style.ICONPOSITION_LEFT)
                    .setIconResource(R.drawable.ic_close_24dp)
                    .setDuration(com.github.johnpersano.supertoasts.library.Style.DURATION_MEDIUM)
                    .setFrame(com.github.johnpersano.supertoasts.library.Style.FRAME_STANDARD)
                    .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                    .setAnimations(com.github.johnpersano.supertoasts.library.Style.ANIMATIONS_SCALE).show();
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mRecyclerView.setAdapter(new PreviewImageAdapter(mImages, mTypes, this));
            mCardScaleHelper = new CardScaleHelper();
            mCardScaleHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(IMAGE_LIST_KEY, (ArrayList<String>) mImages);
        outState.putStringArrayList(TYPE_LIST_KEY, (ArrayList<String>) mTypes);
        super.onSaveInstanceState(outState);
    }
}
