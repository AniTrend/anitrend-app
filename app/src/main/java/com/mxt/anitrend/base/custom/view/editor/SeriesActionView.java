package com.mxt.anitrend.base.custom.view.editor;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.CustomActionAnimeBinding;
import com.mxt.anitrend.databinding.CustomActionMangaBinding;

/**
 * Created by max on 2017/12/02.
 */

public class SeriesActionView extends FrameLayout implements CustomView {

    private CustomActionAnimeBinding animeBinding;
    private CustomActionMangaBinding mangaBinding;

    public SeriesActionView(@NonNull Context context) {
        super(context);
        onInit();
    }

    public SeriesActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public SeriesActionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SeriesActionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {

    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
