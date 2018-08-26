package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.spinner.IconArrayAdapter;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by max on 2018/01/20.
 * CustomSeriesManageBase for managing mediaLists
 */

public abstract class CustomSeriesManageBase extends RelativeLayout implements CustomView, AdapterView.OnItemSelectedListener {

    protected MediaPresenter presenter;

    protected MediaList model;

    protected final Map<Integer, Integer> indexIconMap = new HashMap<Integer, Integer>() {{
        put(0, R.drawable.ic_remove_red_eye_white_18dp); put(1, R.drawable.ic_bookmark_white_24dp);
        put(2, R.drawable.ic_done_all_grey_600_24dp); put(3, R.drawable.ic_delete_red_600_18dp);
        put(4, R.drawable.ic_pause_white_18dp); put(5, R.drawable.ic_repeat_white_18dp);
    }};

    public CustomSeriesManageBase(Context context) {
        super(context);
        onInit();
    }

    public CustomSeriesManageBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public CustomSeriesManageBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomSeriesManageBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new MediaPresenter(getContext());
    }

    protected IconArrayAdapter getIconArrayAdapter() {
        IconArrayAdapter iconArrayAdapter = new IconArrayAdapter(getContext(),
                R.layout.adapter_spinner_item, R.id.spinner_text,
                CompatUtil.getStringList(getContext(), R.array.media_list_status));
        iconArrayAdapter.setIndexIconMap(indexIconMap);
        return iconArrayAdapter;
    }

    public void setModel(MediaBase mediaBase) {
        if(mediaBase.getMediaListEntry() != null) {
            this.model = mediaBase.getMediaListEntry();
            this.model.setMedia(mediaBase);
        }
        else {
            this.model = new MediaList();
            this.model.setMediaId(mediaBase.getId());
            this.model.setMedia(mediaBase);
        }
        bindFields();
        populateFields();
    }

    public MediaListOptions getMediaListOptions() {
        return presenter.getDatabase().getCurrentUser().getMediaListOptions();
    }

    public @NonNull MediaList getModel() {
        return model;
    }

    /**
     * Saves the current views states into the model
     * and returns a bundle of the params
     * @see com.mxt.anitrend.util.MediaListUtil
     */
    public abstract Bundle persistChanges();

    protected abstract void populateFields();

    protected abstract void bindFields();

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
    }

    protected MediaBase getSeriesModel() {
        return model.getMedia();
    }
}
