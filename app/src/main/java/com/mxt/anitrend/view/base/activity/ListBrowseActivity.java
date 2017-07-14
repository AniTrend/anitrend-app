package com.mxt.anitrend.view.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.MyAnimePageAdapter;
import com.mxt.anitrend.adapter.pager.index.MyMangaPageAdapter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.presenter.detail.UserProfilePresenter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/05/01.
 */
public class ListBrowseActivity extends DefaultActivity implements MaterialDialog.SingleButtonCallback {

    public static final String USER_ID = "user_id";
    public static final String CONT_TYPE = "content_type";

    private String KEY_USER_MODEL = "user_model_key";
    private String KEY_MODEL_TYPE = "model_type_key";

    @BindView(R.id.page_container) ViewPager mViewPager;
    @BindView(R.id.nts_center) SmartTabLayout mNavigationTab;
    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private Snackbar snackbar;
    private ActionBar mActionBar;

    private UserProfilePresenter mPresenter;

    private int mUserId;
    private int mContentType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_browse);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(savedInstanceState == null) {
            Intent intent = getIntent();
            mUserId = intent.getIntExtra(USER_ID, 0);
            mContentType = intent.getIntExtra(CONT_TYPE, -1);
        }
        mPresenter = new UserProfilePresenter(this, mUserId);
        startInit();
    }

    /**
     * Optionally allowed to override
     */
    @Override
    protected void startInit() {
        if(mUserId != 0 && mContentType != -1)
            updateUI();
        else {
            snackbar = Snackbar.make(coordinatorLayout, R.string.text_error_request, BaseTransientBottomBar.LENGTH_INDEFINITE).setAction(R.string.Ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void updateUI() {
        if (mContentType == KeyUtils.ANIME) {
            mActionBar.setTitle(R.string.title_anime_list);
            mViewPager.setAdapter(new MyAnimePageAdapter(getSupportFragmentManager(), mUserId, getApplicationContext()));
        } else {
            mActionBar.setTitle(R.string.title_manga_list);
            mViewPager.setAdapter(new MyMangaPageAdapter(getSupportFragmentManager(), mUserId, getApplicationContext()));
        }
        mViewPager.setOffscreenPageLimit(3);
        mNavigationTab.setViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_sort), R.array.series_sort_types, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        mPresenter.getApiPrefs().saveSort(KeyUtils.SeriesSortTypes[which]);
                        return true;
                    }
                }, this, Arrays.asList(KeyUtils.SeriesSortTypes).indexOf(mPresenter.getApiPrefs().getSort()));
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_USER_MODEL, mUserId);
        outState.putInt(KEY_MODEL_TYPE, mContentType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mUserId = savedInstanceState.getInt(KEY_USER_MODEL);
            mContentType = savedInstanceState.getInt(KEY_MODEL_TYPE);
        }
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        Toast.makeText(this, R.string.text_filter_applying, Toast.LENGTH_SHORT).show();
    }
}