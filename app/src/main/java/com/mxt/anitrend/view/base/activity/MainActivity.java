package com.mxt.anitrend.view.base.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.google.android.youtube.player.YouTubeIntents;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.AiringPageAdapter;
import com.mxt.anitrend.adapter.pager.index.HomePageAdapter;
import com.mxt.anitrend.adapter.pager.index.HubPageAdapter;
import com.mxt.anitrend.adapter.pager.index.MangaPageAdapter;
import com.mxt.anitrend.adapter.pager.index.MyAnimePageAdapter;
import com.mxt.anitrend.adapter.pager.index.MyMangaPageAdapter;
import com.mxt.anitrend.adapter.pager.index.ReviewPageAdapter;
import com.mxt.anitrend.adapter.pager.index.SeasonPageAdapter;
import com.mxt.anitrend.adapter.pager.index.TrendingPageAdapter;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.base.custom.service.ServiceScheduler;
import com.mxt.anitrend.base.custom.view.widget.bottomsheet.BottomSheet;
import com.mxt.anitrend.base.custom.view.widget.bottomsheet.BottomSheetMessage;
import com.mxt.anitrend.base.interfaces.event.ApplicationInitListener;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.base.custom.async.AsyncTaskFetch;
import com.mxt.anitrend.base.custom.async.RequestApiAction;
import com.mxt.anitrend.presenter.index.MainPresenter;
import com.mxt.anitrend.util.AppVersionTracking;
import com.mxt.anitrend.util.DateTimeConverter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.ImeAction;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.index.activity.LoginActivity;
import com.mxt.anitrend.view.index.activity.NotificationActivity;
import com.mxt.anitrend.view.index.activity.ProfileActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.wefor.circularanim.CircularAnim;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends DefaultActivity implements MaterialSearchBar.OnSearchActionListener, PopupMenu.OnMenuItemClickListener, ApplicationInitListener,
        Callback<Integer>, View.OnClickListener, BottomSheetItemClickListener, MaterialDialog.SingleButtonCallback,
        SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, TextWatcher {

    public static final String USER_PROF = "current_user_profile";
    public static final String REDIRECT = "app_shortcut";

    private String MODEL_SAVE_KEY = "saved_model_main";
    private String NAVIGATION_POSITION = "navigation_selection";
    private String SHORTCUT_NAVIGATION = "shortcut_pointer";
    private String MODEL_USER_KEY = "user_model_main";
    private String TIME_ELAPSE_TRIGGER = "time_elapse_trigger";
    private String VIEW_PAGER_OFF_SCREEN = "off_screen_limit";

    private MainPresenter mPresenter;

    @BindView(R.id.page_container) ViewPager mViewPager;
    @BindView(R.id.nts_center) SmartTabLayout mNavigationTabStrip;
    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.searchBar) MaterialSearchBar searchBar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.nav_view) NavigationView mNavigationView;

    private BottomSheetBehavior mFilterBehavior;
    private BottomSheetMessage mBuilder;

    private static final int REQUEST_CODE = 128;

    private int mOffScreenLimit = 3;

    private boolean isClosing;

    private User mCurrentUser;
    private long mLastSynced;
    private int mShortcut;
    private int mNavigationIndex;
    private int mPageIndex;
    private Search mModel;
    private Menu menuItems;

    private MenuItem mHomeMenu, mAccountLogin, mAccountProfile, mSignOutProfile, mAiringMenu, mMyAnimeMenu, mMyMangaMenu, mReviewMenu, mHubMenu, mNewStyleMenu, mUpdateMenu;

    private ImageView user_banner, user_avatar;
    private TextView user_name, user_notifications, app_update;
    private SwitchCompat mThemeSwitch, mStyleSwitch, mReviewTypeSwitch;
    private AsyncTaskFetch<Integer> userNotificationCountFetch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if(savedInstanceState == null) {
            Intent intent = getIntent();
            mShortcut = intent.getIntExtra(REDIRECT, 0);
            mCurrentUser = intent.getParcelableExtra(USER_PROF);
        } else {
            mShortcut = savedInstanceState.getInt(SHORTCUT_NAVIGATION);
            mNavigationIndex = savedInstanceState.getInt(NAVIGATION_POSITION);
            mCurrentUser = savedInstanceState.getParcelable(MODEL_USER_KEY);
            mLastSynced = savedInstanceState.getLong(TIME_ELAPSE_TRIGGER);
            mOffScreenLimit = savedInstanceState.getInt(VIEW_PAGER_OFF_SCREEN);
        }
        mPresenter = new MainPresenter(MainActivity.this);
        mNavigationView.setNavigationItemSelectedListener(this);
        //mNavigationView.setItemIconTintList(null); disabled to allow tinting of icons by theme
        menuItems = mNavigationView.getMenu();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mPageIndex = DateTimeConverter.getMenuSelect();
        updateUI();
        int res;
        if(savedInstanceState == null) {
            if(mPresenter.getAppPrefs().isAuthenticated()) {
                res = mShortcut == 0? mPresenter.getDefaultPrefs().getStartupPage():mShortcut;
                mNavigationView.setCheckedItem(res);
                Display(res);
            }
            else {
                res = mShortcut == 0?R.id.nav_anime:mShortcut;
                mNavigationView.setCheckedItem(res);
                Display(res);
            }
        } else {
            res = mShortcut == 0?mNavigationIndex:mShortcut;
            mNavigationView.setCheckedItem(res);
            Display(res);
        }
    }

    @Override
    public void onLowMemory() {
        mOffScreenLimit = 1;
        if(mViewPager != null)
            mViewPager.setOffscreenPageLimit(mOffScreenLimit);
        super.onLowMemory();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(mNavigationIndex == id)
            return true;
        if(id != R.id.nav_account_profile && id != R.id.nav_list_style && id != R.id.nav_light_theme)
            mDrawer.closeDrawer(GravityCompat.START);
        Display(id);
        return true;
    }

    public void Display(int id) {
        switch (id) {
            case R.id.nav_home:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_home));
                mNavigationIndex = id;
                HomePageAdapter mHomePageAdapter = new HomePageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mHomePageAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_anime:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_anime));
                mNavigationIndex = id;
                SeasonPageAdapter mSeasonPageAdapter = new SeasonPageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mSeasonPageAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                mViewPager.setCurrentItem(mPageIndex, true);
                break;
            case R.id.nav_manga:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_manga));
                mNavigationIndex = id;
                MangaPageAdapter mMangaPageAdapter = new MangaPageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mMangaPageAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_trending:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_trending));
                mNavigationIndex = id;
                TrendingPageAdapter mTrendingPagedAdapter = new TrendingPageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mTrendingPagedAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_airing:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_airing));
                mNavigationIndex = id;
                AiringPageAdapter mAiringPagedAdapter = new AiringPageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mAiringPagedAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_myanime:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_myanime));
                mNavigationIndex = id;
                MyAnimePageAdapter myAnimePageAdapter = new MyAnimePageAdapter(getSupportFragmentManager(), mCurrentUser.getId(), getApplicationContext());
                mViewPager.setAdapter(myAnimePageAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_mymanga:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_mymanga));
                mNavigationIndex = id;
                MyMangaPageAdapter myMangaPageAdapter = new MyMangaPageAdapter(getSupportFragmentManager(), mCurrentUser.getId(), getApplicationContext());
                mViewPager.setAdapter(myMangaPageAdapter);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_hub:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_hub));
                mNavigationIndex = id;
                HubPageAdapter mAniTrendHub = new HubPageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mAniTrendHub);
                mNavigationTabStrip.setViewPager(mViewPager);
                break;
            case R.id.nav_reviews:
                searchBar.setPlaceHolder(getString(R.string.drawer_title_reviews));
                mNavigationIndex = id;
                ReviewPageAdapter mReviews = new ReviewPageAdapter(getSupportFragmentManager(), getApplicationContext());
                mViewPager.setAdapter(mReviews);
                mNavigationTabStrip.setViewPager(mViewPager);
                if(mPresenter.getAppPrefs().getReviewsTip()) {
                    mPresenter.createAlerter(MainActivity.this, getString(R.string.title_new_feature), getString(R.string.text_reviews_feature),
                            R.drawable.ic_bookmark_white_24dp, R.color.colorStateBlue);
                    mPresenter.getAppPrefs().setReviewsTip();
                }
                break;
            case R.id.nav_filter:
                if(mFilterBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    mFilterBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mFilterBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.nav_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.campaign_link));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.nav_account_action:
                Intent requestIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(requestIntent, LoginActivity.LOGIN_RESULT);
                break;
            case R.id.nav_account_profile:
                Intent profile_intent = new Intent(this, ProfileActivity.class);
                profile_intent.putExtra(ProfileActivity.PROFILE_INTENT_KEY, mCurrentUser);
                startActivity(profile_intent);
                break;
            case R.id.nav_sign_out:
                mBuilder = new BottomSheetMessage.Builder()
                        .setTitle(R.string.drawer_signout_title)
                        .setText(R.string.drawer_signout_text)
                        .setPositive(R.string.Yes)
                        .setNegative(R.string.No)
                        .setCallback(BottomSheet.SheetButtonEvents.CALLBACK_SIGN_OUT).build();
                mBuilder.show(getSupportFragmentManager(), mBuilder.getTag());
                break;
            case R.id.nav_check_update:
                switch (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    case PERMISSION_GRANTED:
                        mBuilder = new BottomSheetMessage.Builder()
                                .setTitle(R.string.drawer_update_title)
                                .setText(R.string.drawer_update_text)
                                .setPositive(R.string.Yes)
                                .setNegative(R.string.No)
                                .setCallback(BottomSheet.SheetButtonEvents.CALLBACK_UPDATE_CHECKER).build();
                        mBuilder.show(getSupportFragmentManager(), mBuilder.getTag());
                        break;
                    case PERMISSION_DENIED:
                        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DialogManager(MainActivity.this).createDialogMessage(getString(R.string.title_permission_write),
                                    getString(R.string.text_permission_write), getString(R.string.Ok),
                                    getString(R.string.Cancel), new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            switch (which) {
                                                case POSITIVE:
                                                    dialog.dismiss();
                                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                                                    break;
                                                case NEUTRAL:
                                                    break;
                                                case NEGATIVE:
                                                    dialog.dismiss();
                                                    Toast.makeText(MainActivity.this, R.string.canceled_by_user, Toast.LENGTH_SHORT).show();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        else {
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                        break;
                }
                break;
            case R.id.nav_light_theme:
                mThemeSwitch.toggle();
                break;
            case R.id.nav_list_style:
                mStyleSwitch.toggle();
                break;
            default:
                break;
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE) {
            if(grantResults.length > 0)
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    try {
                        Display(R.id.nav_check_update);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                else
                    Toast.makeText(this, R.string.text_permission_required, Toast.LENGTH_SHORT).show();
        }
    }

    private void welcomeLogin(Intent data) {
        if (data != null && data.hasExtra(LoginActivity.KEY_USER_DEFAULT_LOGIN)) {
            mCurrentUser = data.getParcelableExtra(LoginActivity.KEY_USER_DEFAULT_LOGIN);
            if (mCurrentUser == null)
                recreate();
            data.getIntExtra(LoginActivity.KEY_NOTIFICATION_COUNT, 0);
            setUserItems();
            mBuilder = new BottomSheetMessage.Builder()
                    .setTitle(R.string.login_title)
                    .setText(R.string.login_message)
                    .setNegative(R.string.Ok).build();
            mBuilder.show(getSupportFragmentManager(), mBuilder.getTag());

            // schedule the job to run
            if (mPresenter.getDefaultPrefs().isNotificationEnabled())
                new ServiceScheduler(getApplicationContext()).scheduleJob();

            mPresenter.createAlerter(MainActivity.this, getString(R.string.login_welcome_title, mCurrentUser.getDisplay_name()), getString(R.string.login_welcome_text),
                    R.drawable.ic_bubble_chart_white_24dp, R.color.colorStateGreen);
        } else {
            mBuilder = new BottomSheetMessage.Builder()
                    .setTitle(R.string.login_error_title)
                    .setText(R.string.login_error_text)
                    .setCallback(BottomSheet.SheetButtonEvents.CALLBACK_SIGN_OUT)
                    .setPositive(R.string.Ok).build();
            mBuilder.show(getSupportFragmentManager(), mBuilder.getTag());
        }
    }

    /**
     * After a successful login
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
            if(requestCode == LoginActivity.LOGIN_RESULT)
                welcomeLogin(data);
    }

    @Override
    protected void updateUI() {
        mPresenter.setOnInitCallback(this);

        boolean review_type = mPresenter.getAppPrefs().getReviewType();

        View header_view = mNavigationView.getHeaderView(0);

        user_banner = (ImageView) header_view.findViewById(R.id.drawer_banner);
        user_avatar = (ImageView) header_view.findViewById(R.id.drawer_avatar);
        user_name = (TextView) header_view.findViewById(R.id.drawer_app_name);

        LinearLayout user_header = (LinearLayout) header_view.findViewById(R.id.banner_clickable);

        mReviewTypeSwitch = (SwitchCompat) menuItems.findItem(R.id.nav_reviews).getActionView().findViewById(R.id.app_review_type);
        mThemeSwitch = (SwitchCompat) menuItems.findItem(R.id.nav_light_theme).getActionView().findViewById(R.id.app_theme_light);

        mNewStyleMenu = menuItems.findItem(R.id.nav_list_style);
        mStyleSwitch = (SwitchCompat) mNewStyleMenu.getActionView().findViewById(R.id.app_mal_style);

        mAccountLogin = menuItems.findItem(R.id.nav_account_action);
        mAccountProfile = menuItems.findItem(R.id.nav_account_profile);

        mUpdateMenu = menuItems.findItem(R.id.nav_check_update);
        app_update = (TextView) mUpdateMenu.getActionView().findViewById(R.id.app_update_info);

        mSignOutProfile = menuItems.findItem(R.id.nav_sign_out);
        mHomeMenu = menuItems.findItem(R.id.nav_home);
        mAiringMenu = menuItems.findItem(R.id.nav_airing);
        mMyAnimeMenu = menuItems.findItem(R.id.nav_myanime);
        mMyMangaMenu = menuItems.findItem(R.id.nav_mymanga);
        mHubMenu = menuItems.findItem(R.id.nav_hub);
        mReviewMenu = menuItems.findItem(R.id.nav_reviews);
        mReviewTypeSwitch.setChecked(review_type);
        mReviewTypeSwitch.setOnCheckedChangeListener(this);
        mThemeSwitch.setChecked(mPresenter.getAppPrefs().isLightTheme());
        mThemeSwitch.setOnCheckedChangeListener(this);
        mStyleSwitch.setChecked(mPresenter.getAppPrefs().isNewStyle());
        mStyleSwitch.setOnCheckedChangeListener(this);
        searchBar.setOnSearchActionListener(this);
        searchBar.addTextChangeListener(this);
        searchBar.inflateMenu(R.menu.menu_main_options);
        searchBar.getMenu().setOnMenuItemClickListener(this);
        mReviewMenu.setTitle(review_type?R.string.drawer_title_anime_reviews:R.string.drawer_title_manga_reviews);
        if(mPresenter.getAppPrefs().isAuthenticated()) {
            if(mCurrentUser != null) {
                setUserItems();
                onNewNotification();
            } else {
                mPresenter.createAlerter(MainActivity.this, getString(R.string.text_user_model), getString(R.string.text_auth_unknown), R.drawable.ic_close_white_24dp, R.color.colorStateOrange);
            }
        }

        View bottomSheet = new BottomSheetBuilder(this, coordinatorLayout)
                .setMode(BottomSheetBuilder.MODE_GRID)
                .setBackgroundColorResource(R.color.colorDarkKnight)
                .setItemTextColorResource(R.color.white)
                .setMenu(R.menu.menu_main_filter)
                .setItemClickListener(this).createView();

        mFilterBehavior = BottomSheetBehavior.from(bottomSheet);
        mFilterBehavior.setBottomSheetCallback(null);

        mViewPager.setOffscreenPageLimit(mOffScreenLimit);

        mPresenter.startExecution();
        if(mModel == null)
            mModel = mPresenter.getSearchModel();
        user_header.setOnClickListener(this);
    }

    /**
     * Runs once to check if users has youtube installed
     */
    private void checkStatus(){
        if(!YouTubeIntents.canResolvePlayVideoIntent(this))
        {
            mPresenter.createSuperToast(MainActivity.this,
                    getString(R.string.init_youtube_missing),
                    R.drawable.ic_play_circle_outline_white_24dp,
                    Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                    PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_BLUE_GREY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(MODEL_SAVE_KEY, mModel);
        outState.putInt(NAVIGATION_POSITION, mNavigationIndex);
        outState.putParcelable(MODEL_USER_KEY, mCurrentUser);
        outState.putInt(SHORTCUT_NAVIGATION, mShortcut);
        outState.putLong(TIME_ELAPSE_TRIGGER, mLastSynced);
        outState.putInt(VIEW_PAGER_OFF_SCREEN, mOffScreenLimit);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mModel = (Search)savedInstanceState.getSerializable(MODEL_SAVE_KEY);
    }

    /**
     * Destroy all fragments and loaders.
     */
    @Override
    protected void onDestroy() {
        if(userNotificationCountFetch != null)
            userNotificationCountFetch.cancel(false);
        super.onDestroy();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        mPresenter.getDefaultPrefs().getPreferences().unregisterOnSharedPreferenceChangeListener(MainActivity.this);
        if(userNotificationCountFetch != null)
            userNotificationCountFetch.cancel(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getDefaultPrefs().getPreferences().registerOnSharedPreferenceChangeListener(MainActivity.this);
        if(mLastSynced == 0L) {
            mLastSynced = System.currentTimeMillis();
            return;
        }
        if(mPresenter.getAppPrefs().isAuthenticated() && !isFinishing() || !isDestroyed()) {
            //request user notification count after every 5 minutes
            if(System.currentTimeMillis() / 1000 > mLastSynced / 1000 + (60 * 5)) {
                mLastSynced = System.currentTimeMillis();
                userNotificationCountFetch = new AsyncTaskFetch<>(this, getApplicationContext());
                userNotificationCountFetch.execute(AsyncTaskFetch.RequestType.USER_NOTIFICATION_COUNT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START))
            mDrawer.closeDrawer(GravityCompat.START);
        else if(mFilterBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            mFilterBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else if(!isClosing) {
            isClosing = true;
            mPresenter.createSuperToast(MainActivity.this,
                    getString(R.string.text_confirm_exit),
                    R.drawable.ic_info_outline_white_18dp,
                    Style.TYPE_STANDARD, Style.DURATION_MEDIUM,
                    PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_TEAL));
        }
        else
            super.onBackPressed();
    }

    /**
     * Invoked when SearchBar opened or closed
     */
    @Override
    public void onSearchStateChanged(boolean state) {

    }

    /**
     * Material Search bar will check if the text is valid
     */
    @Override
    public void onSearchConfirmed(CharSequence charSequence) {
        ImeAction.hideSoftKeyboard(this);
        if(!TextUtils.isEmpty(charSequence)) {
            mModel.setQuery(charSequence.toString());
            startSearch();
        } else {
            Snackbar.make(coordinatorLayout, R.string.text_search_empty, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Material Search Bar button clicks
     */
    @Override
    public void onButtonClicked(int i) {
        switch (i){
            case MaterialSearchBar.BUTTON_NAVIGATION:
                //open navigation drawer
                mDrawer.openDrawer(GravityCompat.START);
                break;
        }
    }

    /**
     * Material Search Bar menu items
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ImeAction.hideSoftKeyboard(this);
        switch (item.getItemId()) {
            case R.id.action_create_post:
                if (mPresenter.getAppPrefs().isAuthenticated()) {
                    Intent intent = new Intent(this, ComposerActivity.class);
                    intent.putExtra(ComposerActivity.ARG_ACTION_TYPE, KeyUtils.ActionType.ACTIVITY_CREATE);
                    startActivity(intent);
                }
                else
                    Display(R.id.nav_account_action);
                break;
            case R.id.action_changelog:
                onUpdatedVersion();
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_help:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_anitrend_forum))));
                break;
        }
        return true;
    }

    /**
     * When the search query has been submitted
     */
    private void startSearch() {
        CircularAnim.fullActivity(this, searchBar).colorOrImageRes(mPresenter.getAppPrefs().isLightTheme()?R.color.colorAccent_Ripple:R.color.colorDarkKnight).go(new CircularAnim.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                Intent searchAction = new Intent(MainActivity.this, SearchResultActivity.class);
                searchAction.putExtra(SearchResultActivity.QUERY, searchBar.getText());
                startActivity(searchAction);
            }
        });
    }

    /**
     * If this is a new application installation
     */
    @Override
    public void onNewInstallation() {
        checkStatus();
        final MaterialTapTargetPrompt.Builder fNavigation = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(com.mancj.materialsearchbar.R.id.mt_nav)
                .setPrimaryText(R.string.tip_navigation_title)
                .setSecondaryText(R.string.tip_navigation_text)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                    }

                    @Override
                    public void onHidePromptComplete() {
                        onUpdatedVersion();
                    }
                });

        final MaterialTapTargetPrompt.Builder fSearch = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(com.mancj.materialsearchbar.R.id.mt_search)
                .setPrimaryText(R.string.tip_search_title)
                .setSecondaryText(R.string.tip_search_message)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                    }

                    @Override
                    public void onHidePromptComplete() {
                        fNavigation.show();
                    }
                });

        MaterialTapTargetPrompt.Builder fMenu = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(com.mancj.materialsearchbar.R.id.mt_menu)
                .setPrimaryText(R.string.tip_options_title)
                .setSecondaryText(R.string.tip_options_text)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                    }

                    @Override
                    public void onHidePromptComplete() {
                        fSearch.show();
                    }
                });

        fMenu.show();

    }

    /**
     * If the application version has changed
     */
    @Override
    public void onUpdatedVersion() {
        new DialogManager(MainActivity.this).createChangeLog();
        if(mPresenter.getAppPrefs().getMainTip()) {
            mBuilder = new BottomSheetMessage.Builder()
                    .setTitle(R.string.app_intro_title)
                    .setText(R.string.app_intro_guide)
                    .setCallback(BottomSheet.SheetButtonEvents.CALLBACK_APP_INTRO)
                    .setPositive(R.string.Ok).build();
            mBuilder.show(getSupportFragmentManager(), mBuilder.getTag());
        }
    }

    @Override
    public void onNormalStart() {
        AppVersionTracking remote = mPresenter.getAppPrefs().getRepoVersions();
        if(remote.checkAgainstCurrent()) {
            // there is a new version of getRepoVersions()
            app_update.setText(Html.fromHtml(getString(R.string.app_update, remote.getVersion())));
            app_update.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When the API end point returns a notification count
     */
    @Override
    public void onNewNotification() {
        int nCount = mCurrentUser.getNotifications();
        if(nCount > 0) {
            user_notifications.setVisibility(View.VISIBLE);
            if(nCount > 1)
                user_notifications.setText(getString(R.string.text_notifications, nCount));
            else
                user_notifications.setText(getString(R.string.text_notification, nCount));
        } else {
            user_notifications.setVisibility(View.GONE);
        }
    }

    /**
     * Bottom sheet for our filter options
     */
    @Override
    public void onBottomSheetItemClick(MenuItem item) {
        mFilterBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        switch (item.getItemId()) {

            case R.id.action_sort:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_sort), R.array.series_sort_types, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        mPresenter.getApiPrefs().saveSort(KeyUtils.SeriesSortTypes[which]);
                        mModel.setSort_by(KeyUtils.SeriesSortTypes[which]);
                        //fire event handler to all child view pagers
                        //mPresenter.notifyAllItems();
                        return true;
                    }
                }, this, Arrays.asList(KeyUtils.SeriesSortTypes).indexOf(mPresenter.getApiPrefs().getSort()));
                break;

            case R.id.action_order:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_order), R.array.order_by_types, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        mPresenter.getApiPrefs().saveOrder(KeyUtils.OrderTypes[which]);
                        mModel.setOrder_by(KeyUtils.OrderTypes[which]);
                        //fire event handler to all child view pagers
                        //mPresenter.notifyAllItems();
                        return true;
                    }
                }, this, Arrays.asList(KeyUtils.OrderTypes).indexOf(mPresenter.getApiPrefs().getOrder()));
                break;

            case R.id.action_genre:
                new DialogManager(this).createDialogChecks(getString(R.string.app_filter_genres), mPresenter.getGenres(), new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        /**
                         If you want to preselect any items, pass an array of indices (resource or literal) in place of null in itemsCallbackMultiChoice().
                         Later, you can update the selected indices using setSelectedIndices(Integer[]) on the MaterialDialog instance, if you're not using a custom adapter.

                         If you do not set a positive action button using positiveText(), the dialog will automatically call the multi choice callback when user presses the positive action button.
                         The dialog will also dismiss itself, unless auto dismiss is turned off.

                         If you make a call to alwaysCallMultiChoiceCallback(), the multi choice callback will be called every time the user selects/unselects an item.
                         */
                        mPresenter.saveGenres(which, text, MainActivity.this);
                        return true;
                    }
                }, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                mPresenter.getApiPrefs().resetGenres();
                                //mPresenter.notifyAllItems();
                                break;
                        }
                    }
                }, mPresenter.getSelectedGenres());
                break;

            case R.id.action_type:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_show_type), R.array.series_types, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        mPresenter.getApiPrefs().saveShowType(KeyUtils.ShowTypes[which]);
                        mModel.setSeries_type(KeyUtils.ShowTypes[which]);
                        //fire event handler to all child view pagers
                        //mPresenter.notifyAllItems();
                        return true;
                    }
                }, this, Arrays.asList(KeyUtils.ShowTypes).indexOf(mPresenter.getApiPrefs().getShowType()));
                break;

            case R.id.action_year:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_year), DateTimeConverter.getYearRanges(), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        mPresenter.getApiPrefs().saveYear(Integer.valueOf(text.toString()));
                        mModel.setYear(Integer.valueOf(text.toString()));
                        //fire event handler to all child view pagers
                        //mPresenter.notifyAllItems();
                        return true;
                    }
                }, this, Arrays.asList(DateTimeConverter.getStringYearRanges()).indexOf(mPresenter.getApiPrefs().getYear()));
                break;

            case R.id.action_status:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_status), R.array.anime_status_types, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         */
                        mPresenter.getApiPrefs().saveStatus(KeyUtils.AnimeStatusTypes[which]);
                        mModel.setItem_status(KeyUtils.AnimeStatusTypes[which]);
                        //fire event handler to all child view pagers
                        //mPresenter.notifyAllItems();
                        return true;
                    }
                }, this, Arrays.asList(KeyUtils.AnimeStatusTypes).indexOf(mPresenter.getApiPrefs().getStatus()));
                break;
        }
    }

    /**
     * When Material Dialog on click occurs
     */
    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

        if(mNavigationIndex == R.id.nav_anime || mNavigationIndex == R.id.nav_manga || mNavigationIndex == R.id.nav_myanime || mNavigationIndex == R.id.nav_mymanga || mNavigationIndex == R.id.nav_hub) {
            mPresenter.createSuperToast(MainActivity.this,
                    getString(R.string.text_filter_applying),
                    R.drawable.ic_reset,
                    Style.TYPE_PROGRESS_BAR, Style.DURATION_VERY_SHORT,
                    PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_CYAN));
        } else {
            mPresenter.createSuperToast(MainActivity.this,
                    getString(R.string.text_filter_restriction),
                    R.drawable.ic_info_outline_white_18dp,
                    Style.TYPE_STANDARD, Style.DURATION_VERY_SHORT,
                    PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_ORANGE));
        }
    }

    /**
     * After authentication, set the correct prefs and states
     */
    public void setUserItems() {
        if (mCurrentUser != null) {
            mAccountProfile.setVisible(true);
            mHomeMenu.setVisible(true);
            mAiringMenu.setVisible(true);
            mMyAnimeMenu.setVisible(true);
            mMyMangaMenu.setVisible(true);
            mReviewMenu.setVisible(true);
            mHubMenu.setVisible(true);
            mNewStyleMenu.setVisible(true);
            mAccountLogin.setVisible(false);
            mSignOutProfile.setVisible(true);
            user_notifications = (TextView) mAccountProfile.getActionView().findViewById(R.id.user_notification_count);
            user_notifications.setOnClickListener(this);

            user_name.setText(mCurrentUser.getDisplay_name());
            Glide.with(this)
                    .load(mCurrentUser.getImage_url_lge())
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(user_avatar) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            user_avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            Glide.with(this)
                    .load(mCurrentUser.getImage_url_banner())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(user_banner);
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.text_login_error, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    /**
     * Menu items switches events are handled in here
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
        switch (compoundButton.getId()) {
            case R.id.app_theme_light:
                mPresenter.getAppPrefs().setLightTheme(state);
                recreate();
                break;
            case R.id.app_mal_style:
                mPresenter.getAppPrefs().setNewStyle(state);
                recreate();
                break;
            case R.id.app_review_type:
                mPresenter.getAppPrefs().setReviewType(state);
                mReviewMenu.setTitle(state?R.string.drawer_title_anime_reviews:R.string.drawer_title_manga_reviews);
                break;
        }
    }

    /**
     * Navigation drawer item clicks handler
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.banner_clickable:
                if(mPresenter.getAppPrefs().isAuthenticated()) {
                    Intent profile_intent = new Intent(this, ProfileActivity.class);
                    profile_intent.putExtra(ProfileActivity.PROFILE_INTENT_KEY, mCurrentUser);
                    TransitionHelper.startSharedTransitionActivity(MainActivity.this, user_banner, profile_intent);
                } else {
                    Display(R.id.nav_account_action);
                }
                break;
            case R.id.user_notification_count:
                user_notifications.setVisibility(View.GONE);
                mCurrentUser.setNotifications(0);
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                break;
        }
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     */
    @Override
    public void onResponse(Call<Integer> call, Response<Integer> response) {
        if(response.isSuccessful() && response.body() != null && (!isDestroyed() || !isFinishing())) {
            try {
                mCurrentUser.setNotifications(response.body());
                if(response.body() > 0) {
                    user_notifications.setVisibility(View.VISIBLE);
                    mPresenter.createAlerter(MainActivity.this, getString(R.string.alerter_notification_title), getString(R.string.alerter_notification_text),
                            R.drawable.ic_notifications_active_white_24dp, R.color.colorAccent);

                    if(response.body() > 1)
                        user_notifications.setText(getString(R.string.text_notifications, response.body()));
                    else
                        user_notifications.setText(getString(R.string.text_notification, response.body()));
                } else {
                    user_notifications.setVisibility(View.GONE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    @Override
    public void onFailure(Call<Integer> call, Throwable t) {
        if(!isDestroyed() || !isFinishing()) {
            call.cancel();
            t.printStackTrace();
        }
    }

    /**
     * Shared preference changes callback
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(this.getLocalClassName(), "onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)");
        ServiceScheduler sh = new ServiceScheduler(getApplicationContext());
        if(key.equals(getString(R.string.pref_key_sync_frequency)))
            if(mPresenter.getAppPrefs().isAuthenticated() && sharedPreferences.getBoolean(getString(R.string.pref_key_new_message_notifications),true))
                sh.scheduleJob();
        else if (key.equals(getString(R.string.pref_key_new_message_notifications)))
            if(sharedPreferences.getBoolean(getString(R.string.pref_key_new_message_notifications),true))
                if(mPresenter.getAppPrefs().isAuthenticated())
                    sh.scheduleJob();
            else
                sh.cancelJob();
    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * are about to be replaced by new text with length <code>after</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * This method is called to notify you that, somewhere within
     * <code>s</code>, the text has been changed.
     * It is legitimate to make further changes to <code>s</code> from
     * this callback, but be careful not to get yourself into an infinite
     * loop, because any changes you make will cause this method to be
     * called again recursively.
     * (You are not told where the change took place because other
     * afterTextChanged() methods may already have made other changes
     * and invalidated the offsets.  But if you need to know here,
     * you can use {@link Spannable#setSpan} in {@link #onTextChanged}
     * to mark your place and then look up from here where the span
     * ended up.
     */
    @Override
    public void afterTextChanged(Editable s) {
        mPresenter.notifyAllItems(s.toString().toLowerCase(Locale.getDefault()));
    }
}
