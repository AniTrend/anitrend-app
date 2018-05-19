package com.mxt.anitrend.base.custom.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/10/27.
 * Custom status view of airing or publishing status
 */

public class SeriesStatusWidget extends FrameLayout implements CustomView {

    public SeriesStatusWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public SeriesStatusWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public SeriesStatusWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeriesStatusWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    /** Give the current airing status of the series */
    @BindingAdapter("seriesStatus")
    public static void setStatus(SeriesStatusWidget view, MediaBase model) {
        if(model != null) {
            @KeyUtil.MediaStatus String mediaStatus = !TextUtils.isEmpty(model.getStatus()) ? model.getStatus() : KeyUtil.NOT_YET_RELEASED;
            switch (mediaStatus) {
                case KeyUtil.RELEASING:
                    view.setBackgroundColor(CompatUtil.getColor(view.getContext(), R.color.colorStateBlue));
                    break;
                case KeyUtil.FINISHED:
                    view.setBackgroundColor(CompatUtil.getColor(view.getContext(), R.color.colorStateGreen));
                    break;
                case KeyUtil.NOT_YET_RELEASED:
                    view.setBackgroundColor(CompatUtil.getColor(view.getContext(), R.color.colorStateOrange));
                    break;
                default:
                    view.setBackgroundColor(CompatUtil.getColor(view.getContext(), R.color.colorStateRed));
                    break;
            }
        }
    }

    /** Give the current airing status of the series */
    @BindingAdapter("seriesStatus")
    public static void setStatus(SeriesStatusWidget view, Media model) {
        if(model != null)
            setStatus(view, (MediaBase) model);
    }

    /** Give the current airing status of the series */
    @BindingAdapter("seriesStatus")
    public static void setStatus(SeriesStatusWidget view, MediaList mediaList) {
        if (mediaList != null)
            setStatus(view, mediaList.getMedia());
    }

    /** Give the current airing status of the series */
    @BindingAdapter("airingStatus")
    public static void setAiringStatus(SeriesStatusWidget view, MediaList mediaList) {
        if(mediaList != null && mediaList.getMedia() != null && mediaList.getMedia().getNextAiringEpisode() != null) {
            if(mediaList.getMedia().getNextAiringEpisode().getEpisode() - mediaList.getProgress() > 1) {
                view.setBackgroundColor(CompatUtil.getColor(view.getContext(), R.color.colorStateOrange));
                return;
            }
        }
        view.setBackgroundColor(CompatUtil.getColorFromAttr(view.getContext(), R.attr.cardColor));
    }
}
