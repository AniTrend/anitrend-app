package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetAutoIncrementerBinding;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.general.SeriesList;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesUtil;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/02/22.
 * auto increment widget for changing series progress with just a tap
 */

public class AutoIncrementWidget extends LinearLayout implements CustomView, View.OnClickListener, RetroCallback<SeriesList> {

    private WidgetPresenter<SeriesList> presenter;
    private @KeyUtils.RequestMode int requestType;
    private WidgetAutoIncrementerBinding binding;
    private SeriesList model;

    private String currentUser;

    public AutoIncrementWidget(Context context) {
        super(context);
        onInit();
    }

    public AutoIncrementWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public AutoIncrementWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    public AutoIncrementWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetAutoIncrementerBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
        binding.setOnClickEvent(this);
    }

    @Override
    public void onClick(View view) {
        if(presenter.isCurrentUser(currentUser) && SeriesUtil.isAllowedStatus(model)) {
            if (!SeriesUtil.isIncrementLimitReached(model)) {
                switch (view.getId()) {
                    case R.id.widget_flipper:
                        if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                            binding.widgetFlipper.showNext();
                            updateModelState();
                        } else
                            NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else
                NotifyUtil.makeText(getContext(), SeriesUtil.isAnimeType(getSeriesModel()) ?
                                R.string.text_unable_to_increment_episodes : R.string.text_unable_to_increment_chapters,
                        R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
        }
    }

    public void setModel(SeriesList model, String currentUser) {
        this.model = model; this.currentUser = currentUser;
        binding.seriesProgressIncrement.setSeriesModel(model, presenter.isCurrentUser(currentUser));
        requestType = model.getAnime() != null? KeyUtils.ANIME_LIST_EDIT_REQ : KeyUtils.MANGA_LIST_EDIT_REQ;
    }

    @Override
    public void onViewRecycled() {
        resetFlipperState();
        if(presenter != null)
            presenter.onDestroy();
        model = null;
    }

    private void resetFlipperState() {
        if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

    @Override
    public void onResponse(@NonNull Call<SeriesList> call, @NonNull Response<SeriesList> response) {
        try {
            SeriesList seriesList;
            if(response.isSuccessful() && (seriesList = response.body()) != null) {
                boolean isModelCategoryChanged = !seriesList.getList_status().equals(model.getList_status());
                seriesList.setAnime(model.getAnime()); seriesList.setManga(model.getManga()); model = seriesList;
                binding.seriesProgressIncrement.setSeriesModel(model, presenter.isCurrentUser(currentUser));
                presenter.getDatabase().getBoxStore(SeriesList.class).put(model); resetFlipperState();
                if(isModelCategoryChanged) {
                    NotifyUtil.makeText(getContext(), R.string.text_changes_saved, R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show();
                    presenter.notifyAllListeners(new BaseConsumer<>(requestType, model), false);
                }
            } else {
                resetFlipperState();
                Log.e(this.toString(), ErrorUtil.getError(response));
                NotifyUtil.makeText(getContext(), R.string.text_error_request, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(@NonNull Call<SeriesList> call, @NonNull Throwable throwable) {
        try {
            Log.e(toString(), throwable.getLocalizedMessage());
            throwable.printStackTrace();
            resetFlipperState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateModelState() {
        model.setChapters_read(model.getChapters_read() + 1);
        model.setEpisodes_watched(model.getEpisodes_watched() + 1);
        if(SeriesUtil.isIncrementLimitReached(model))
            model.setList_status(KeyUtils.UserAnimeStatus[KeyUtils.COMPLETED]);
        if(SeriesUtil.isIncrementLimitReached(model))
            model.setList_status(KeyUtils.UserMangaStatus[KeyUtils.COMPLETED]);

        presenter.setParams(getParam());
        presenter.requestData(requestType, getContext(), this);
    }

    private Bundle getParam() {
        Bundle bundle = new Bundle();
        bundle.putLong(KeyUtils.arg_id, model.getSeries_id());

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

    private SeriesBase getSeriesModel() {
        return model.getAnime() != null ? model.getAnime() : model.getManga();
    }
}
