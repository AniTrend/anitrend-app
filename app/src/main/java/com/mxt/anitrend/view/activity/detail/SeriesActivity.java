package com.mxt.anitrend.view.activity.detail;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.detail.AnimePageAdapter;
import com.mxt.anitrend.adapter.pager.detail.MangaPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.base.custom.view.image.WideImageView;
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget;
import com.mxt.anitrend.databinding.ActivitySeriesBinding;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.util.TapTargetUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by max on 2017/12/01.
 * Series activity
 */

public class SeriesActivity extends ActivityBase<Series, SeriesPresenter> implements View.OnClickListener {

    private ActivitySeriesBinding binding;
    private String seriesType;
    private Series model;

    private FavouriteToolbarWidget favouriteWidget;

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_series);
        setPresenter(new SeriesPresenter(getApplicationContext()));
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        disableToolbarTitle();
        setViewModel(true);
        if(getIntent().hasExtra(KeyUtils.arg_id))
            id = getIntent().getLongExtra(KeyUtils.arg_id, 0);
        if(getIntent().hasExtra(KeyUtils.arg_series_type))
            seriesType = getIntent().getStringExtra(KeyUtils.arg_series_type);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBar.setHomeAsUpIndicator(CompatUtil.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        if(seriesType != null) {
            BaseStatePageAdapter baseStatePageAdapter = new AnimePageAdapter(getSupportFragmentManager(), getApplicationContext());
            if (!seriesType.equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]))
                baseStatePageAdapter = new MangaPageAdapter(getSupportFragmentManager(), getApplicationContext());
            baseStatePageAdapter.setParams(getIntent().getExtras());
            viewPager.setAdapter(baseStatePageAdapter);
            viewPager.setOffscreenPageLimit(offScreenLimit + 4);
            smartTabLayout.setViewPager(viewPager);
            onActivityReady();
        } else
            NotifyUtil.createAlerter(this, R.string.text_error_request, R.string.text_unknown_error, R.drawable.ic_warning_white_18dp, R.color.colorStateRed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isAuth = getPresenter().getApplicationPref().isAuthenticated();
        getMenuInflater().inflate(R.menu.series_menu, menu);
        menu.findItem(R.id.action_favourite).setVisible(isAuth);
        menu.findItem(R.id.action_manage).setVisible(isAuth);

        if(isAuth) {
            MenuItem favouriteMenuItem = menu.findItem(R.id.action_favourite);
            favouriteWidget = (FavouriteToolbarWidget) favouriteMenuItem.getActionView();
            if(model != null)
                favouriteWidget.setModel(model);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(model != null) {
            switch (item.getItemId()) { 
                case R.id.action_manage:
                    seriesActionUtil = new SeriesActionUtil.Builder()
                            .setModel(model).build(this);
                    seriesActionUtil.startSeriesAction();
                    break;
            }
        } else
            NotifyUtil.makeText(getApplicationContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(model == null)
            makeRequest();
        else
            updateUI();
    }

    @Override
    protected void updateUI() {
        binding.setModel(model);
        binding.setOnClickListener(this);
        WideImageView.setImage(binding.seriesBanner, model.getImage_url_banner());
        getPresenter().notifyAllListeners(model, false);
        if(favouriteWidget != null)
            favouriteWidget.setModel(model);
        showApplicationTips();
    }

    @Override
    protected void makeRequest() {
        Bundle params = getViewModel().getParams();
        params.putLong(KeyUtils.arg_id, id);
        params.putString(KeyUtils.arg_series_type, seriesType);
        getViewModel().requestData(KeyUtils.SERIES_PAGE_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable Series model) {
        super.onChanged(model);
        this.model = model;
        updateUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.series_banner:
                Intent intent = new Intent(this, ImagePreviewActivity.class);
                intent.putExtra(KeyUtils.arg_model, binding.getModel().getImage_url_banner());
                CompatUtil.startSharedImageTransition(this, view, intent, R.string.transition_image_preview);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(favouriteWidget != null)
            favouriteWidget.onViewRecycled();
        super.onDestroy();
    }

    private void showApplicationTips() {
        if (!TapTargetUtil.isActive(KeyUtils.KEY_DETAIL_TIP) && getPresenter().getApplicationPref().isAuthenticated()) {
            if (getPresenter().getApplicationPref().shouldShowTipFor(KeyUtils.KEY_DETAIL_TIP)) {
                TapTargetUtil.buildDefault(this, R.string.tip_series_options_title, R.string.tip_series_options_message, R.id.action_manage)
                        .setPromptStateChangeListener((prompt, state) -> {
                            if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                                getPresenter().getApplicationPref().disableTipFor(KeyUtils.KEY_DETAIL_TIP);
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                                TapTargetUtil.setActive(KeyUtils.KEY_DETAIL_TIP, true);
                        }).setFocalColour(CompatUtil.getColor(this, R.color.grey_600)).show();
                TapTargetUtil.setActive(KeyUtils.KEY_DETAIL_TIP, false);
            }
        }
    }
}
