package com.mxt.anitrend.view.activity.index;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.AiringPageAdapter;
import com.mxt.anitrend.adapter.pager.index.FeedPageAdapter;
import com.mxt.anitrend.adapter.pager.index.HubPageAdapter;
import com.mxt.anitrend.adapter.pager.index.MangaPageAdapter;
import com.mxt.anitrend.adapter.pager.index.ReviewPageAdapter;
import com.mxt.anitrend.adapter.pager.index.SeasonPageAdapter;
import com.mxt.anitrend.adapter.pager.index.TrendingPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.view.image.AvatarIndicatorView;
import com.mxt.anitrend.base.custom.view.image.HeaderImageView;
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.Version;
import com.mxt.anitrend.presenter.activity.MainPresenter;
import com.mxt.anitrend.service.DownloaderService;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.base.AboutActivity;
import com.mxt.anitrend.view.activity.base.SettingsActivity;
import com.mxt.anitrend.view.activity.detail.NotificationActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.detail.SeriesListActivity;
import com.mxt.anitrend.view.sheet.BottomSheetMessage;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by max on 2017/10/04.
 * Base main_menu activity to show case template
 */

public class MainActivity extends ActivityBase<Void, MainPresenter> implements View.OnClickListener, BaseConsumer.onRequestModelChange<Integer>,
        NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    protected @BindView(R.id.toolbar) Toolbar mToolbar;
    protected @BindView(R.id.page_container) ViewPager mViewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout mNavigationTabStrip;
    protected @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    protected @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    protected @BindView(R.id.nav_view) NavigationView mNavigationView;

    private ActionBarDrawerToggle mDrawerToggle;

    private @IdRes int redirectShortcut;
    private @IdRes int selectedItem;
    private @StringRes int selectedTitle;

    private int mPageIndex;

    private Menu menuItems;

    private MenuItem mHomeFeed, mReviewMenu, mAccountLogin, mSignOutProfile, mManageMenu;

    private HeaderImageView mHeaderView;
    private TextView mUserName;
    private AvatarIndicatorView mUserAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setPresenter(new MainPresenter(getApplicationContext()));
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (savedInstanceState == null)
            redirectShortcut = getIntent().getIntExtra(KeyUtils.arg_redirect, 0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mNavigationView.setNavigationItemSelectedListener(this);
        mViewPager.setOffscreenPageLimit(offScreenLimit);
        mPageIndex = DateUtil.getMenuSelect();
        menuItems = mNavigationView.getMenu();
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if(mSearchView != null)
            mSearchView.setMenuItem(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_donate:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.patreon.com/wax911"));
                startActivity(intent);
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            case R.id.action_share:
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.campaign_link));
                intent.setType("text/plain");
                startActivity(intent);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_discord:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/nqsFGgX"));
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        if(selectedItem == 0)
            selectedItem = getPresenter().getApplicationPref().isAuthenticated()?
                    (redirectShortcut == 0? getPresenter().getApplicationPref().getStartupPage() : redirectShortcut)
                    : (redirectShortcut == 0? R.id.nav_anime : redirectShortcut);
        mNavigationView.setCheckedItem(selectedItem);
        onNavigate(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KeyUtils.arg_redirect, redirectShortcut);
        outState.putInt(KeyUtils.key_navigation_selected, selectedItem);
        outState.putInt(KeyUtils.key_navigation_title, selectedTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            redirectShortcut = savedInstanceState.getInt(KeyUtils.arg_redirect);
            selectedItem = savedInstanceState.getInt(KeyUtils.key_navigation_selected);
            selectedTitle = savedInstanceState.getInt(KeyUtils.key_navigation_title);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        } super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        mDrawerLayout.removeDrawerListener(mDrawerToggle);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        updateUI();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final @IdRes int menu = item.getItemId();
        if(selectedItem != menu)
            onNavigate(menu);
        if(menu != R.id.nav_light_theme && menu != R.id.nav_sign_in && menu != R.id.nav_myanime && menu != R.id.nav_mymanga)
            mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavigate(@IdRes int menu) {
        Intent intent;
        switch (menu) {
            case R.id.nav_home_feed:
                mToolbar.setTitle(getString(R.string.drawer_title_home));
                selectedItem = menu;
                mViewPager.setAdapter(new FeedPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_anime:
                mToolbar.setTitle(getString(R.string.drawer_title_anime));
                selectedItem = menu;
                mViewPager.setAdapter(new SeasonPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                mViewPager.setCurrentItem(mPageIndex, false);
                break;
            case R.id.nav_manga:
                mToolbar.setTitle(getString(R.string.drawer_title_manga));
                selectedItem = menu;
                mViewPager.setAdapter(new MangaPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_trending:
                mToolbar.setTitle(getString(R.string.drawer_title_trending));
                selectedItem = menu;
                mViewPager.setAdapter(new TrendingPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_airing:
                mToolbar.setTitle(getString(R.string.drawer_title_airing));
                mViewPager.setAdapter(new AiringPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                selectedItem = menu;
                break;
            case R.id.nav_myanime:
                intent = new Intent(this, SeriesListActivity.class);
                intent.putExtra(KeyUtils.arg_series_type, KeyUtils.ANIME);
                intent.putExtra(KeyUtils.arg_user_name, getPresenter().getDatabase().getCurrentUser().getName());
                startActivity(intent);
                break;
            case R.id.nav_mymanga:
                intent = new Intent(this, SeriesListActivity.class);
                intent.putExtra(KeyUtils.arg_series_type, KeyUtils.MANGA);
                intent.putExtra(KeyUtils.arg_user_name, getPresenter().getDatabase().getCurrentUser().getName());
                startActivity(intent);
                break;
            case R.id.nav_hub:
                mToolbar.setTitle(getString(R.string.drawer_title_hub));
                mViewPager.setAdapter(new HubPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                selectedItem = menu;
                break;
            case R.id.nav_reviews:
                mToolbar.setTitle(getString(R.string.drawer_title_reviews));
                selectedItem = menu;
                mViewPager.setAdapter(new ReviewPageAdapter(getSupportFragmentManager(), getApplicationContext()));
                mNavigationTabStrip.setViewPager(mViewPager);
                if(getPresenter().getApplicationPref().shouldShowTipFor(KeyUtils.KEY_REVIEW_TYPE_TIP)) {
                    NotifyUtil.createAlerter(this, R.string.title_new_feature, R.string.text_reviews_feature,
                            R.drawable.ic_bookmark_white_24dp, R.color.colorStateBlue, KeyUtils.DURATION_LONG);
                    getPresenter().getApplicationPref().disableTipFor(KeyUtils.KEY_REVIEW_TYPE_TIP);
                }
                break;
            case R.id.nav_sign_in:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.nav_sign_out:
                mBottomSheet = new BottomSheetMessage.Builder()
                    .setText(R.string.drawer_signout_text)
                    .setTitle(R.string.drawer_signout_title)
                    .setPositiveText(R.string.Yes)
                    .setNegativeText(R.string.No)
                    .buildWithCallback(new BottomSheetChoice() {
                        @Override
                        public void onPositiveButton() {
                            WebTokenRequest.invalidateInstance(getApplicationContext());
                            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                            finish();
                            startActivity(intent);
                        }

                        @Override
                        public void onNegativeButton() {

                        }
                    });
                showBottomSheet();
                break;
            case R.id.nav_check_update:
                switch (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    case PERMISSION_GRANTED:
                        mBottomSheet = new BottomSheetMessage.Builder()
                                .setText(R.string.drawer_update_text)
                                .setTitle(R.string.drawer_update_title)
                                .setPositiveText(R.string.Yes)
                                .setNegativeText(R.string.No)
                                .buildWithCallback(new BottomSheetChoice() {
                                    @Override
                                    public void onPositiveButton() {
                                        Version version = getPresenter().getDatabase().getRemoteVersion();
                                        if(version != null && version.isNewerVersion())
                                            DownloaderService.downloadNewVersion(MainActivity.this, version);
                                        else
                                            NotifyUtil.createAlerter(MainActivity.this, getString(R.string.title_update_infodadat),
                                                    getString(R.string.app_no_date), R.drawable.ic_cloud_done_white_24dp, R.color.colorStateGreen);
                                    }

                                    @Override
                                    public void onNegativeButton() {

                                    }
                                });
                        showBottomSheet();
                        break;
                    case PERMISSION_DENIED:
                        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                            DialogUtil.createMessage(MainActivity.this, R.string.title_permission_write, R.string.text_permission_write, (dialog, which) -> {
                                        switch (which) {
                                            case POSITIVE:
                                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                                                break;
                                            case NEGATIVE:
                                                NotifyUtil.makeText(MainActivity.this, R.string.canceled_by_user, Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    });
                        else
                            hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        break;
                }
                break;
            case R.id.nav_light_theme:
                getPresenter().getApplicationPref().toggleTheme();
                recreate();
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateUI() {
        boolean reviewType = getPresenter().getApplicationPref().getReviewType();

        Version version = getPresenter().getDatabase().getRemoteVersion();
        View HeaderContainer = mNavigationView.getHeaderView(0);

        mHeaderView = HeaderContainer.findViewById(R.id.drawer_banner);
        mUserAvatar = HeaderContainer.findViewById(R.id.drawer_avatar_indicator);
        mUserName = HeaderContainer.findViewById(R.id.drawer_app_name);

        mHomeFeed = menuItems.findItem(R.id.nav_home_feed);
        mAccountLogin = menuItems.findItem(R.id.nav_sign_in);
        mSignOutProfile = menuItems.findItem(R.id.nav_sign_out);
        mManageMenu = menuItems.findItem(R.id.nav_header_manage);

        mReviewMenu = menuItems.findItem(R.id.nav_reviews);
        mReviewMenu.setTitle(reviewType?R.string.drawer_title_anime_reviews:R.string.drawer_title_manga_reviews);
        HeaderContainer.findViewById(R.id.banner_clickable).setOnClickListener(this);

        SwitchCompat mReviewTypeSwitch = menuItems.findItem(R.id.nav_reviews).getActionView().findViewById(R.id.app_review_type);

        mReviewTypeSwitch.setChecked(reviewType);
        mReviewTypeSwitch.setOnCheckedChangeListener(this);

        if(getPresenter().getApplicationPref().isAuthenticated())
            setupUserItems();
        else
            mHeaderView.setImageResource(R.drawable.reg_bg);

        if(version != null && version.isNewerVersion()) {
            // If a new version of the application is available on GitHub
            TextView mAppUpdateWidget = menuItems.findItem(R.id.nav_check_update).getActionView().findViewById(R.id.app_update_info);
            mAppUpdateWidget.setText(getString(R.string.app_update, version.getVersion()));
            mAppUpdateWidget.setVisibility(View.VISIBLE);
        }
        checkNewInstallation();
    }

    @Override
    protected void makeRequest() {
        // nothing to request
    }

    private void setupUserItems() {
        User user;
        if((user = getPresenter().getDatabase().getCurrentUser()) != null) {
            mUserName.setText(user.getName());
            mUserAvatar.setImageSrc(user.getAvatar());
            HeaderImageView.setImage(mHeaderView, user.getImage_url_banner());

            if (getPresenter().getApplicationPref().shouldShowTipFor(KeyUtils.KEY_LOGIN_TIP)) {
                NotifyUtil.createLoginToast(MainActivity.this, user);
                getPresenter().getApplicationPref().disableTipFor(KeyUtils.KEY_LOGIN_TIP);
                mBottomSheet = new BottomSheetMessage.Builder()
                        .setText(R.string.login_message)
                        .setTitle(R.string.login_title)
                        .setNegativeText(R.string.Ok)
                        .build();
                showBottomSheet();
            }
            Crashlytics.setUserIdentifier(user.getName());
            getApplicationBase().getAnalytics()
                    .setUserId(user.getName());
        }

        mAccountLogin.setVisible(false);

        mSignOutProfile.setVisible(true);
        mManageMenu.setVisible(true);
        mHomeFeed.setVisible(true);
    }

    /**
     * Checks to see if this instance is a new installation
     */
    private void checkNewInstallation() {
        if(getPresenter().getApplicationPref().isFreshInstall()) {
            getPresenter().getApplicationPref().setFreshInstall();
            mBottomSheet = new BottomSheetMessage.Builder()
                    .setText(R.string.app_intro_guide)
                    .setTitle(R.string.app_intro_title)
                    .setNegativeText(R.string.Ok).build();
            showBottomSheet();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.banner_clickable:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra(KeyUtils.arg_user_name, getPresenter().getDatabase().getCurrentUser().getName());
                    CompatUtil.startSharedImageTransition(MainActivity.this, mHeaderView, intent, R.string.transition_user_banner);
                }
                else
                    onNavigate(R.id.nav_sign_in);
                break;
            default:
                User current = getPresenter().getDatabase().getCurrentUser();
                current.setUnreadNotificationCount(0);
                getPresenter().getDatabase().saveCurrentUser(current);
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                break;
        }
    }

    /**
     * Menu items switches events are handled in here
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
        switch (compoundButton.getId()) {
            case R.id.app_review_type:
                getPresenter().getApplicationPref().setReviewType(state);
                mReviewMenu.setTitle(state?R.string.drawer_title_anime_reviews:R.string.drawer_title_manga_reviews);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(mUserAvatar != null)
            mUserAvatar.onViewRecycled();
        super.onDestroy();
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<Integer> consumer) {
        if(consumer.getRequestMode() == KeyUtils.USER_NOTIFICATION_COUNT)
            NotifyUtil.createAlerter(this, R.string.alerter_notification_title, R.string.alerter_notification_text,
                    R.drawable.ic_notifications_active_white_24dp, R.color.colorAccent);
    }
}

