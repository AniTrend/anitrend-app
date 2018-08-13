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
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.TapTargetUtil;
import com.mxt.anitrend.util.TutorialUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by max on 2017/12/01.
 * Media activity
 */

public class MediaActivity extends ActivityBase<MediaBase, MediaPresenter> implements View.OnClickListener {

    private ActivitySeriesBinding binding;
    private MediaBase model;

    private @KeyUtil.MediaType String mediaType;

    private FavouriteToolbarWidget favouriteWidget;
    private MenuItem manageMenuItem;

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_series);
        setPresenter(new MediaPresenter(getApplicationContext()));
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        disableToolbarTitle();
        setViewModel(true);
        if(getIntent().hasExtra(KeyUtil.arg_id))
            id = getIntent().getLongExtra(KeyUtil.arg_id, -1);
        if(getIntent().hasExtra(KeyUtil.arg_mediaType))
            mediaType = getIntent().getStringExtra(KeyUtil.arg_mediaType);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBar.setHomeAsUpIndicator(CompatUtil.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isAuth = getPresenter().getApplicationPref().isAuthenticated();
        getMenuInflater().inflate(R.menu.media_base_menu, menu);
        menu.findItem(R.id.action_favourite).setVisible(isAuth);

        manageMenuItem = menu.findItem(R.id.action_manage);
        manageMenuItem.setVisible(isAuth);
        setManageMenuItemIcon();

        if(isAuth) {
            MenuItem favouriteMenuItem = menu.findItem(R.id.action_favourite);
            favouriteWidget = (FavouriteToolbarWidget) favouriteMenuItem.getActionView();
            setFavouriteWidgetMenuItemIcon();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(model != null) {
            switch (item.getItemId()) { 
                case R.id.action_manage:
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(model.getId()).build(this);
                    mediaActionUtil.startSeriesAction();
                    break;
                case R.id.action_share:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(),
                            "%s - %s", model.getTitle().getUserPreferred(), model.getSiteUrl()));
                    intent.setType("text/plain");
                    startActivity(intent);
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
        if(mediaType != null) {
            BaseStatePageAdapter baseStatePageAdapter = new AnimePageAdapter(getSupportFragmentManager(), getApplicationContext());
            if (!CompatUtil.equals(mediaType, KeyUtil.ANIME))
                baseStatePageAdapter = new MangaPageAdapter(getSupportFragmentManager(), getApplicationContext());
            baseStatePageAdapter.setParams(getIntent().getExtras());
            viewPager.setAdapter(baseStatePageAdapter);
            viewPager.setOffscreenPageLimit(offScreenLimit);
            smartTabLayout.setViewPager(viewPager);
        } else
            NotifyUtil.createAlerter(this, R.string.text_error_request, R.string.text_unknown_error, R.drawable.ic_warning_white_18dp, R.color.colorStateRed);
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
        if(model != null) {
            binding.setModel(model);
            binding.setOnClickListener(this);
            WideImageView.setImage(binding.seriesBanner, model.getBannerImage());
            setFavouriteWidgetMenuItemIcon(); setManageMenuItemIcon();
            if(getPresenter().getApplicationPref().isAuthenticated()) {
                MaterialTapTargetPrompt.Builder favouritesPrompt = new TutorialUtil().setContext(this)
                        .setFocalColour(R.color.colorGrey600)
                        .setTapTarget(KeyUtil.KEY_DETAIL_TIP)
                        .setApplicationPref(getPresenter().getApplicationPref())
                        .createTapTarget(R.string.tip_series_options_title,
                                R.string.tip_series_options_message, R.id.action_manage);
                TapTargetUtil.showMultiplePrompts(favouritesPrompt);
            }
        }
    }

    @Override
    protected void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_mediaType, mediaType)
                .putVariable(KeyUtil.arg_id, id);

        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_BASE_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable MediaBase model) {
        super.onChanged(model);
        this.model = model;
        updateUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.series_banner:
                Intent intent = new Intent(this, ImagePreviewActivity.class);
                intent.putExtra(KeyUtil.arg_model, model.getBannerImage());
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

    private void setManageMenuItemIcon() {
        if(model != null && model.getMediaListEntry() != null && manageMenuItem != null)
            manageMenuItem.setIcon(CompatUtil.getDrawable(this, R.drawable.ic_mode_edit_white_24dp));
    }

    private void setFavouriteWidgetMenuItemIcon() {
        if(model != null && favouriteWidget != null)
            favouriteWidget.setModel(model);
    }
}
