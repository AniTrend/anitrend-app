package com.mxt.anitrend.view.detail.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.rubensousa.floatingtoolbar.FloatingToolbar;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.details.MangaPageAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.presenter.detail.SeriesPresenter;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MangaActivity extends DefaultActivity implements FloatingToolbar.ItemClickListener, Callback<Series>,
                                                                       MaterialTapTargetPrompt.OnHidePromptListener {

    public final static String MODEL_ID_KEY = "MODEL_KEY";
    public final static String MODEL_BANNER_KEY = "MODEL_BANNER_KEY";

    private final String SAVED_INSTANCE_ID_KEY = "KEY_PAGE_ID";
    private final String SAVED_INSTANCE_BANNER_MODEL = "KEY_MODEL_BANNER";
    private final String SAVED_INSTANCE_SERIES_MODEL = "KEY_MODEL_SERIES";

    private int mId;
    private String mBanner;
    private Series mSeries;

    @BindView(R.id.app_bar) AppBarLayout appBarLayout;

    @BindView(R.id.sections_tabs) TabLayout tabLayout;
    @BindView(R.id.sections_viewpager) ViewPager viewPager;


    @BindView(R.id.scrollProgressLayout) ProgressLayout progressLayout;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.floatingToolbar) FloatingToolbar mFloatingToolbar;
    @BindView(R.id.fab) FloatingActionButton fab;

    @BindView(R.id.detail_model_banner) ImageView mBannerImage;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private ActionBar mActionBar;
    private MenuItem favMenuItem;

    private SeriesPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if ((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
        if(mIntentData != null) {
            mId = Integer.valueOf(mIntentData);
        }
        else {
            Intent intent = getIntent();
            mId = intent.getIntExtra(MODEL_ID_KEY, 0);
            if (intent.hasExtra(MODEL_BANNER_KEY))
                mBanner = intent.getStringExtra(MODEL_BANNER_KEY);
        }
        mActionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        progressLayout.showLoading();
        viewPager.setOffscreenPageLimit(3);
        attachEventListeners();
        mPresenter = new SeriesPresenter(KeyUtils.SeriesTypes[KeyUtils.MANGA], getApplicationContext());
        if(mId != 0 && mSeries != null)
            updateUI();
        else {
            if(mId == 0)
                progressLayout.showError(ContextCompat.getDrawable(this, R.drawable.request_error), getString(R.string.text_error_request), getString(R.string.Go_Back), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            else
                mPresenter.beginAsync(this, mId);
        }

        setUpBanner();
        if(mPresenter.getAppPrefs().getDetailTip())
            showHelp();
    }

    private void setUpBanner() {
        if (mBanner != null) {
            Glide.with(this)
                    .load(mBanner)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .centerCrop()
                    .into(mBannerImage);

            mBannerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MangaActivity.this, ImagePreviewActivity.class);
                    intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mBanner);
                    TransitionHelper.startSharedImageTransition(MangaActivity.this, view, getString(R.string.transition_image_preview), intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_main, menu);
        favMenuItem = menu.findItem(R.id.action_favor_state);
        if(mSeries != null)
            favMenuItem.setVisible(mSeries.isFavourite());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favor_state:
                mPresenter.displayMessage(mSeries.isFavourite()?getString(R.string.text_item_in_favourites):getString(R.string.text_item_not_in_favourites), this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attach event listeners and other properties
     */
    private void attachEventListeners() {
        mFloatingToolbar.setClickListener(this);
        mFloatingToolbar.attachFab(fab);
    }

    public void setTitleToolbar(String title) {
        collapsingToolbarLayout.setTitle(title);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
    }

    public void setAdapters() {
        viewPager.setAdapter(new MangaPageAdapter(getSupportFragmentManager(), mSeries, getApplicationContext()));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_INSTANCE_ID_KEY, mId);
        outState.putString(SAVED_INSTANCE_BANNER_MODEL, mBanner);
        outState.putParcelable(SAVED_INSTANCE_SERIES_MODEL, mSeries);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mId = savedInstanceState.getInt(SAVED_INSTANCE_ID_KEY, 0);
            mBanner = savedInstanceState.getString(SAVED_INSTANCE_BANNER_MODEL);
            mSeries = savedInstanceState.getParcelable(SAVED_INSTANCE_SERIES_MODEL);
        }
    }

    @Override
    public void onBackPressed() {
        if(mFloatingToolbar.isShown())
            mFloatingToolbar.hide();
        else
            super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void showHelp() {
        new MaterialTapTargetPrompt.Builder(this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(findViewById(R.id.fab))
                .setPrimaryText(R.string.tip_series_options_title)
                .setSecondaryText(R.string.tip_series_options_message)
                .setOnHidePromptListener(this).show();
    }

    /**
     * Called when the use touches the prompt view,
     * but before the prompt is removed from view.
     *
     * @param event The touch event that triggered the dismiss or finish.
     * @param tappedTarget True if the prompt focal point was touched.
     */
    @Override
    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

    }

    /**
     * Called after the prompt has been removed from view.
     */
    @Override
    public void onHidePromptComplete() {
        mPresenter.getAppPrefs().setDetailTip();
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<Series> call, Response<Series> response) {
        if (!isDestroyed() || !isFinishing()) {
            if (response.isSuccessful() && response.body() != null) {
                mSeries = response.body();
                updateUI();
            } else {
                appBarLayout.setExpanded(false, false);
                progressLayout.showError(ContextCompat.getDrawable(this, R.drawable.request_error), getString(R.string.text_error_request), getString(R.string.Go_Back), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<Series> call, Throwable t) {
        progressLayout.showError(ContextCompat.getDrawable(this, R.drawable.request_error), t.getMessage(), getString(R.string.Go_Back), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void updateUI() {
        if(favMenuItem != null)
            favMenuItem.setVisible(mSeries.isFavourite());
        if(mBanner == null) {
            if(mSeries.getImage_url_banner() != null) {
                mBanner = mSeries.getImage_url_banner();
                setUpBanner();
            }
        }
        setAdapters();
        progressLayout.showContent();
    }

    @Override
    public void onItemClick(MenuItem item) {
        if(!mPresenter.getAppPrefs().isAuthenticated()) {
            mPresenter.displayMessage(getString(R.string.text_please_sign_in), this);
            return;
        } if(mSeries == null){
            mPresenter.displayMessage(getString(R.string.text_activity_loading), this);
            return;
        }
        try {
            switch (item.getItemId()) {
                case R.id.action_favourite:
                    Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(mSeries.getId());
                    RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.errorBody() == null) {
                                if (!isDestroyed() || !isFinishing()) {
                                    mPresenter.displayMessage(mSeries.isFavourite()?getString(R.string.text_removed_from_favourites):getString(R.string.text_add_to_favourites), MangaActivity.this);
                                    favMenuItem.setVisible(!mSeries.isFavourite());
                                    mPresenter.beginAsync(new Callback<Series>() {
                                        @Override
                                        public void onResponse(Call<Series> call, Response<Series> response) {
                                            if(!isDestroyed() || !isFinishing())
                                                if(response.isSuccessful() && response.body() != null)
                                                    mSeries = response.body();
                                                else
                                                    mPresenter.displayMessage(ErrorHandler.getError(response).toString(), MangaActivity.this);

                                        }

                                        @Override
                                        public void onFailure(Call<Series> call, Throwable t) {
                                            if(!isDestroyed() || !isFinishing())
                                                try {
                                                    mPresenter.displayMessage(t.getLocalizedMessage(), MangaActivity.this);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                        }
                                    }, mSeries.getId());
                                }
                            }
                            else
                                mPresenter.displayMessage(ErrorHandler.getError(response).toString(), MangaActivity.this);
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            if(!isDestroyed() || !isFinishing())
                                try {
                                    mPresenter.displayMessage(t.getCause().getMessage(), MangaActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                    }, KeyUtils.ActionType.MANGA_FAVOURITE, actionIdBased);
                    userPostActions.execute();
                    break;
                case R.id.action_add_to_list:
                    new SeriesActionHelper(this, KeyUtils.MANGA, mSeries).execute();
                    break;
                case R.id.action_write_review:
                    mPresenter.displayMessage(getString(R.string.text_feature_on_hold), this);
                    break;
                case R.id.action_share:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_share_series,mSeries.getTitle_english(), mSeries.getTitle_japanese(), mSeries.getId(),getString(R.string.campaign_link)));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(MenuItem item) {

    }

}
