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
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
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

public class AutoIncrementWidget extends LinearLayout implements CustomView, View.OnClickListener, RetroCallback<MediaList> {

    private WidgetPresenter<MediaList> presenter;
    private @KeyUtils.RequestMode int requestType;
    private WidgetAutoIncrementerBinding binding;
    private MediaList model;

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

    public void setModel(MediaList model, String currentUser) {
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
    public void onResponse(@NonNull Call<MediaList> call, @NonNull Response<MediaList> response) {
        try {
            MediaList mediaList;
            if(response.isSuccessful() && (mediaList = response.body()) != null) {
                boolean isModelCategoryChanged = !mediaList.getStatus().equals(model.getStatus());
                mediaList.setAnime(model.getAnime()); mediaList.setManga(model.getManga()); model = mediaList;
                binding.seriesProgressIncrement.setSeriesModel(model, presenter.isCurrentUser(currentUser));
                presenter.getDatabase().getBoxStore(MediaList.class).put(model); resetFlipperState();
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
    public void onFailure(@NonNull Call<MediaList> call, @NonNull Throwable throwable) {
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
        model.setProgress(model.getProgress() + 1);
        if(SeriesUtil.isIncrementLimitReached(model))
            model.setStatus(KeyUtils.UserAnimeStatus[KeyUtils.COMPLETED]);
        if(SeriesUtil.isIncrementLimitReached(model))
            model.setStatus(KeyUtils.UserMangaStatus[KeyUtils.COMPLETED]);

        presenter.setParams(getParam());
        presenter.requestData(requestType, getContext(), this);
    }

    private Bundle getParam() {
        Bundle bundle = new Bundle();
        bundle.putLong(KeyUtils.arg_id, model.getMediaId());

        bundle.putString(KeyUtils.arg_list_status, model.getStatus());
        bundle.putString(KeyUtils.arg_list_score, model.getScore());
        bundle.putInt(KeyUtils.arg_list_score_raw, model.getScore_raw());
        bundle.putString(KeyUtils.arg_list_notes, model.getNotes());
        bundle.putInt(KeyUtils.arg_list_hidden, model.getPrivate());

        // bundle.putString(KeyUtils.arg_list_advanced_rating, name_of_rating);
        // bundle.putInt(KeyUtils.arg_list_custom_list, model.getCustom_lists()[selected_index]);

        bundle.putInt(KeyUtils.arg_list_watched, model.getProgress());
        bundle.putInt(KeyUtils.arg_list_re_watched, model.getRepeat());

        bundle.putInt(KeyUtils.arg_list_read, model.getChapters_read());
        bundle.putInt(KeyUtils.arg_list_re_read, model.getReread());
        bundle.putInt(KeyUtils.arg_list_volumes, model.getProgressVolumes());

        return bundle;
    }

    private MediaBase getSeriesModel() {
        return model.getAnime() != null ? model.getAnime() : model.getManga();
    }
}
