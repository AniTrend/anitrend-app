package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetProfileStatsBinding;
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.graphql.AniGraphErrorUtilKt;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.view.activity.detail.MediaListActivity;

import java.util.Locale;

import io.github.wax911.library.model.request.QueryContainerBuilder;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by max on 2017/11/26.
 * status widget
 */

public class ProfileStatsWidget extends FrameLayout implements CustomView, View.OnClickListener, RetroCallback<ConnectionContainer<UserStatisticTypes>> {

    private WidgetProfileStatsBinding binding;
    private WidgetPresenter<ConnectionContainer<UserStatisticTypes>> presenter;

    @Nullable
    private UserStatisticTypes model;
    private QueryContainerBuilder queryContainer;

    @Nullable
    private Bundle bundle;
    private final String TAG = ProfileStatsWidget.class.getSimpleName();

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
        queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false);
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

        binding.setClickListener(this);
    }

    public void updateUI() {
        if (model != null) {
            binding.userAnimeTime.setText(getAnimeTime(model.getAnime().getMinutesWatched()));
            binding.userMangaChaps.setText(getMangaChaptersCount(model.getManga().getChaptersRead()));
            binding.userAnimeTotal.setText(getCount(model.getAnime().getCount()));
            binding.userMangaTotal.setText(getCount(model.getManga().getCount()));
        }
    }

    public void setParams(@Nullable Bundle bundle) {
        this.bundle = bundle;
        if(bundle.containsKey(KeyUtil.arg_id))
            queryContainer.putVariable(KeyUtil.arg_id, bundle.getLong(KeyUtil.arg_id));
        else
            queryContainer.putVariable(KeyUtil.arg_userName, bundle.getString(KeyUtil.arg_userName));
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        presenter.requestData(KeyUtil.USER_STATS_REQ, getContext(), this);
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
                if (model != null)
                    Snackbar.make(this,
                            getContext().getString(
                                    R.string.text_user_anime_time,
                                    getAnimeTime(model.getAnime().getMinutesWatched())
                            ), Snackbar.LENGTH_LONG
                    ).show();
                break;
            case R.id.user_manga_chaps_container:
                if (model != null)
                    Snackbar.make(this,
                            getContext().getString(
                                    R.string.text_user_manga_chapters,
                                    getMangaChaptersCount(model.getManga().getChaptersRead())
                            ), Snackbar.LENGTH_LONG
                    ).show();
                break;
            case R.id.user_anime_total_container:
                if (bundle != null) {
                    intent = new Intent(getContext(), MediaListActivity.class);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME);
                    getContext().startActivity(intent);
                } else {
                    Timber.w("Parent activity returned provided empty bundle params");
                    NotifyUtil.INSTANCE.makeText(
                            getContext(),
                            R.string.login_error_title,
                            Toast.LENGTH_SHORT
                    ).show();
                }
                break;
            case R.id.user_manga_total_container:
                if (bundle != null) {
                    intent = new Intent(getContext(), MediaListActivity.class);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtil.arg_mediaType, KeyUtil.MANGA);
                    getContext().startActivity(intent);
                } else {
                    Timber.w("Parent activity returned provided empty bundle params");
                    NotifyUtil.INSTANCE.makeText(
                            getContext(),
                            R.string.login_error_title,
                            Toast.LENGTH_SHORT
                    ).show();
                }
                break;
        }
    }

    @Override
    public void onResponse(@NonNull Call<ConnectionContainer<UserStatisticTypes>> call, @NonNull Response<ConnectionContainer<UserStatisticTypes>> response) {
        try {
            ConnectionContainer<UserStatisticTypes> connectionContainer;
            if(response.isSuccessful() && (connectionContainer = response.body()) != null) {
                if(!connectionContainer.isEmpty()) {
                    model = connectionContainer.getConnection();
                    updateUI();
                }
            } else {
                Timber.tag(TAG).w(AniGraphErrorUtilKt.apiError(response));
            }
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(@NonNull Call<ConnectionContainer<UserStatisticTypes>> call, @NonNull Throwable throwable) {
        try {
            Timber.tag(TAG).w(throwable);
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
            e.printStackTrace();
        }
    }

    public String getAnimeTime(@Nullable Integer animeTime) {
        if(animeTime == null || animeTime < 1)
            return placeHolder;
        float item_time = animeTime / 60f;
        if(item_time > 60) {
            item_time /= 24;
            if(item_time > 365)
                return getContext().getString(R.string.anime_time_years, item_time/365);
            return getContext().getString(R.string.anime_time_days, item_time);
        }
        return getContext().getString(R.string.anime_time_hours, item_time);
    }

    public String getMangaChaptersCount(@Nullable Integer manga_chap) {
        if(manga_chap == null || manga_chap < 1)
            return placeHolder;
        if(manga_chap > 1000)
            return String.format(Locale.getDefault(), "%.1f K", (float)manga_chap/1000);
        return String.format(Locale.getDefault(), "%d", manga_chap);
    }

    public String getCount(int totalCount) {
        if(totalCount >= 1000)
            return String.format(Locale.getDefault(), "%.1f K", (float)totalCount/1000);
        return String.format(Locale.getDefault(),"%d", totalCount);
    }
}
