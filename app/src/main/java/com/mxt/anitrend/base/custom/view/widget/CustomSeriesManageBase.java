package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.general.SeriesList;
import com.mxt.anitrend.model.entity.general.SeriesList_;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.KeyUtils;

import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by max on 2018/01/20.
 */

public abstract class CustomSeriesManageBase extends RelativeLayout implements CustomView, View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    protected SeriesPresenter presenter;

    protected SeriesList model;

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
        presenter = new SeriesPresenter(getContext());
    }

    public void setModel(Series model, boolean isNewEntry) {
        if(isNewEntry) {
            this.model = new SeriesList();
            if(model.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]))
                this.model.setAnime(model);
            else
                this.model.setManga(model);
        }
        else
            this.model = findSeriesFor(model.getId());
        bindFields();
        populateFields();
    }

    public void setModel(SeriesBase model, boolean isNewEntry) {
        if(isNewEntry) {
            this.model = new SeriesList();
            if(model.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]))
                this.model.setAnime(model);
            else
                this.model.setManga(model);
        }
        else
            this.model = findSeriesFor(model.getId());
        bindFields();
        populateFields();
    }

    public void setModel(SeriesList model, boolean isNewEntry) {
        if(isNewEntry) {
            this.model = new SeriesList();
            this.model.setManga(model.getManga());
            this.model.setAnime(model.getAnime());
        }
        else
            this.model = findSeriesFor(model.getSeries_id());
        bindFields();
        populateFields();
    }

    public @NonNull SeriesList getModel() {
        return model;
    }

    public Bundle getParam() {
        Bundle bundle = new Bundle();
        bundle.putLong(KeyUtils.arg_id, getSeriesModel().getId());

        bundle.putString(KeyUtils.arg_list_status, model.getList_status());
        bundle.putString(KeyUtils.arg_list_score, model.getScore());
        bundle.putInt(KeyUtils.arg_list_score_raw, model.getScore_raw());
        bundle.putString(KeyUtils.arg_list_notes, model.getNotes());
        bundle.putInt(KeyUtils.arg_list_hidden, model.getPrivate());

        // bundle.putString(KeyUtils.arg_list_advanced_rating, name_of_rating);
        // bundle.putInt(KeyUtils.arg_list_custom_list, model.getCustom_lists()[selected_index]);

        bundle.putInt(KeyUtils.arg_list_watched, model.getEpisodes_watched());
        bundle.putInt(KeyUtils.arg_list_re_watched, model.getRewatched());

        bundle.putInt(KeyUtils.arg_list_read, model.getChapters_read());
        bundle.putInt(KeyUtils.arg_list_re_read, model.getReread());
        bundle.putInt(KeyUtils.arg_list_volumes, model.getVolumes_read());

        return bundle;
    }

    private SeriesList findSeriesFor(long id) {
        Box<SeriesList> box = presenter.getDatabase().getBoxStore(SeriesList.class);
        Query<SeriesList> query = box.query().equal(SeriesList_.series_id, id).build();
        return query.findFirst();
    }

    /**
     * save current views states into the model
     */
    public abstract void persistChanges();

    protected abstract void populateFields();

    protected abstract void bindFields();

    protected SeriesBase getSeriesModel() {
        return model.getAnime() != null ? model.getAnime() : model.getManga();
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
    }
}
