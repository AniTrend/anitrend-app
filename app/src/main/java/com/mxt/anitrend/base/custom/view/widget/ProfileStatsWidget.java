package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetProfileStatsBinding;
import com.mxt.anitrend.model.entity.anilist.UserStats;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.detail.MediaListActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/26.
 * status widget
 */

public class ProfileStatsWidget extends FrameLayout implements CustomView, View.OnClickListener, RetroCallback<ConnectionContainer<UserStats>> {

    private WidgetProfileStatsBinding binding;
    private WidgetPresenter<ConnectionContainer<UserStats>> presenter;

    private UserStats model;
    private QueryContainerBuilder queryContainer;

    private Bundle bundle;

    private final String placeHolder = "..";

    public ProfileStatsWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public ProfileStatsWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public ProfileStatsWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ProfileStatsWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        queryContainer = GraphUtil.getDefaultQuery(false);
        binding = WidgetProfileStatsBinding.inflate(LayoutInflater.from(getContext()), this, true);
        // loading place holder data
        binding.userAnimeTime.setText(placeHolder);
        binding.userMangaChaps.setText(placeHolder);
        binding.userAnimeTotal.setText(placeHolder);
        binding.userMangaTotal.setText(placeHolder);

        int textColor = ContextCompat.getColor(getContext(),R.color.white);
        binding.userAnimeTime.setTextColor(textColor);
        binding.userMangaChaps.setTextColor(textColor);
        binding.userAnimeTotal.setTextColor(textColor);
        binding.userMangaTotal.setTextColor(textColor);
    }

    public void updateUI() {
        binding.setClickListener(this);
        binding.userAnimeTime.setText(getAnimeTime(model.getWatchedTime()));
        binding.userMangaChaps.setText(getMangaChaptersCount(model.getChaptersRead()));

        if(model.getAnimeStatusDistribution() != null && !model.getAnimeStatusDistribution().isEmpty())
            binding.userAnimeTotal.setText(getMapCount(model.getAnimeStatusDistribution()));

        if(model.getMangaStatusDistribution() != null && !model.getMangaStatusDistribution().isEmpty())
            binding.userMangaTotal.setText(getMapCount(model.getMangaStatusDistribution()));
    }

    public void setParams(Bundle bundle) {
        this.bundle = bundle;
        queryContainer.putVariable(KeyUtils.arg_id, bundle.getLong(KeyUtils.arg_id))
                .putVariable(KeyUtils.arg_userName, bundle.getString(KeyUtils.arg_userName));
        presenter.getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        presenter.requestData(KeyUtils.MUT_RATE_REVIEW, getContext(), this);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
        model = null;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.user_anime_time_container:
                Snackbar.make(this, getContext().getString(R.string.text_user_anime_time, getAnimeTime(model.getWatchedTime())), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.user_manga_chaps_container:
                Snackbar.make(this, getContext().getString(R.string.text_user_manga_chapters, getMangaChaptersCount(model.getChaptersRead())), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.user_anime_total_container:
                intent = new Intent(getContext(), MediaListActivity.class);

                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtils.arg_mediaType, KeyUtils.ANIME);
                getContext().startActivity(intent);
                break;
            case R.id.user_manga_total_container:
                intent = new Intent(getContext(), MediaListActivity.class);

                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtils.arg_mediaType, KeyUtils.MANGA);
                getContext().startActivity(intent);
                break;
        }
    }

    @Override
    public void onResponse(@NonNull Call<ConnectionContainer<UserStats>> call, @NonNull Response<ConnectionContainer<UserStats>> response) {
        try {
            ConnectionContainer<UserStats> connectionContainer;
            if(response.isSuccessful() && (connectionContainer = response.body()) != null) {
                if(!connectionContainer.isEmpty()) {
                    model = connectionContainer.getConnection();
                    updateUI();
                }
            } else
                Log.e(this.toString(), ErrorUtil.getError(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(@NonNull Call<ConnectionContainer<UserStats>> call, @NonNull Throwable throwable) {
        try {
            Log.e(toString(), throwable.getLocalizedMessage());
            throwable.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAnimeTime(int animeTime) {
        float item_time = animeTime / 60;
        if(item_time > 60) {
            item_time /= 24;
            if(item_time > 365)
                return getContext().getString(R.string.anime_time_years, item_time/365);
            return getContext().getString(R.string.anime_time_days, item_time);
        }
        return getContext().getString(R.string.anime_time_hours, item_time);
    }

    public String getMangaChaptersCount(long manga_chap) {
        if(manga_chap > 1000)
            return String.format(Locale.getDefault(), "%.1f K", (float)manga_chap/1000);
        return String.format(Locale.getDefault(), "%d", manga_chap);
    }

    public String getMapCount(HashMap<String, Integer> map) {
        int totalCount = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet())
            totalCount += entry.getValue();

        if(totalCount > 1000)
            return String.format(Locale.getDefault(), "%d K", totalCount/1000);
        return String.format(Locale.getDefault(),"%d", totalCount);
    }
}
