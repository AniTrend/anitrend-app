package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetProfileStatsBinding;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.presenter.activity.ProfilePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.detail.SeriesListActivity;

import java.util.HashMap;

/**
 * Created by max on 2017/11/26.
 * status widget
 */

public class ProfileStatsWidget extends FrameLayout implements CustomView, View.OnClickListener {

    private WidgetProfileStatsBinding binding;
    private ProfilePresenter presenter;
    private User user;

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
        presenter = new ProfilePresenter(getContext());
        binding = WidgetProfileStatsBinding.inflate(LayoutInflater.from(getContext()), this, true);
        // loading place holder data
        binding.userAnimeTime.setText("..");
        binding.userMangaChaps.setText("..");
        binding.userAnimeTotal.setText("..");
        binding.userMangaTotal.setText("..");

        int textColor = ContextCompat.getColor(getContext(),R.color.white);
        binding.userAnimeTime.setTextColor(textColor);
        binding.userMangaChaps.setTextColor(textColor);
        binding.userAnimeTotal.setTextColor(textColor);
        binding.userMangaTotal.setTextColor(textColor);
    }

    public void setModel(User user) {
        this.user = user;
        binding.setClickListener(this);

        binding.userAnimeTime.setText(presenter.getAnimeTime(user.getAnime_time()));
        binding.userMangaChaps.setText(presenter.getMangaChaptersCount(user.getManga_chap()));

        if(user.getStats() != null && user.getStats().getStatus_distribution() != null) {
            HashMap<String, HashMap<String, Integer>> statusDistribution = user.getStats().getStatus_distribution();
            if(statusDistribution.containsKey(KeyUtils.SeriesTypes[KeyUtils.ANIME]))
                binding.userAnimeTotal.setText(presenter.getMapCount(statusDistribution.get(KeyUtils.SeriesTypes[KeyUtils.ANIME])));
            if(statusDistribution.containsKey(KeyUtils.SeriesTypes[KeyUtils.MANGA]))
                binding.userMangaTotal.setText(presenter.getMapCount(statusDistribution.get(KeyUtils.SeriesTypes[KeyUtils.MANGA])));
        }
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.user_anime_time_container:
                Snackbar.make(this, getContext().getString(R.string.text_user_anime_time, presenter.getAnimeTime(user.getAnime_time())), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.user_manga_chaps_container:
                Snackbar.make(this, getContext().getString(R.string.text_user_manga_chapters, presenter.getMangaChaptersCount(user.getManga_chap())), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.user_anime_total_container:
                intent = new Intent(getContext(), SeriesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtils.arg_series_type, KeyUtils.ANIME);
                intent.putExtra(KeyUtils.arg_user_name, user.getName());
                getContext().startActivity(intent);
                break;
            case R.id.user_manga_total_container:
                intent = new Intent(getContext(), SeriesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtils.arg_series_type, KeyUtils.MANGA);
                intent.putExtra(KeyUtils.arg_user_name, user.getName());
                getContext().startActivity(intent);
                break;
        }
    }
}
