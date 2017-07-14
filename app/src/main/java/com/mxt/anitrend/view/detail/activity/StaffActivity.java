package com.mxt.anitrend.view.detail.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.details.StaffPageAdapter;
import com.mxt.anitrend.api.model.Staff;
import com.mxt.anitrend.api.model.StaffSmall;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class StaffActivity extends DefaultActivity implements Callback<Staff> {

    public final static String STAFF_INTENT_KEY = "character_small_intent_key";

    @BindView(R.id.scrollProgressLayout) ProgressLayout progressLayout;
    @BindView(R.id.parent_coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.nts_center) SmartTabLayout tabLayout;

    private final String KEY_MINI_STAFF = "mini_staff_key";
    private final String KEY_FULL_STAFF = "full_staff_key";

    private Staff mStaff;
    private StaffSmall mStaffSmall;

    private ActionBar mActionBar;
    private MenuItem favMenuItem;
    private ApplicationPrefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if ((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
        if(getIntent().hasExtra(STAFF_INTENT_KEY)) {
            mStaffSmall = getIntent().getParcelableExtra(STAFF_INTENT_KEY);
        }
        progressLayout.showLoading();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        prefs = new ApplicationPrefs(this);
        if(savedInstanceState != null) {
            mStaff = savedInstanceState.getParcelable(KEY_FULL_STAFF);
            mStaffSmall = savedInstanceState.getParcelable(KEY_MINI_STAFF);
            updateUI();
        } else {
            new AsyncTaskFetch<>(this, getApplicationContext(), mStaffSmall.getId()).execute(AsyncTaskFetch.RequestType.STAFF_INFO_REQ);
        }
        mActionBar.setTitle(mStaffSmall.getName_first());
    }

    @Override
    protected void updateUI() {
        setFavIcon();
        StaffPageAdapter mStaffViewAdapter = new StaffPageAdapter(getSupportFragmentManager(), mStaff, getApplicationContext());
        mViewPager.setAdapter(mStaffViewAdapter);
        tabLayout.setViewPager(mViewPager);
        progressLayout.showContent();
    }

    private void setFavIcon() {
        if(favMenuItem != null && mStaff != null) {
            favMenuItem.setVisible(true);
            favMenuItem.setIcon(
                    mStaff.isFavourite()?
                            ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp) :
                            ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_white_24dp));

            if(prefs.getStaffTip()){
                new MaterialTapTargetPrompt.Builder(StaffActivity.this)
                        //or use ContextCompat.getColor(this, R.color.colorAccent)
                        .setFocalColourFromRes(R.color.colorAccent)
                        .setBackgroundColourFromRes(R.color.colorDarkKnight)
                        .setTarget(toolbar.getChildAt(toolbar.getChildCount() - 1))
                        .setPrimaryText(R.string.tip_character_options_title)
                        .setSecondaryText(R.string.tip_character_options_message)
                        .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                            @Override
                            public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                            }

                            @Override
                            public void onHidePromptComplete() {
                                prefs.setStaffTip();
                            }
                        }).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_generic_detail, menu);
        favMenuItem = menu.findItem(R.id.action_favor_state);
        setFavIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favor_state:
                if(!prefs.isAuthenticated()) {
                    Snackbar.make(coordinatorLayout, R.string.text_please_sign_in, Snackbar.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
                }

                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(mStaff.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(!isDestroyed() || !isFinishing()) {
                            if (response.isSuccessful() && response.body() != null) {
                                Snackbar.make(coordinatorLayout, mStaff.isFavourite() ? R.string.text_removed_from_favourites : R.string.text_add_to_favourites, Snackbar.LENGTH_SHORT).show();
                                mStaff.setFavourite(!mStaff.isFavourite());
                                setFavIcon();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(!isDestroyed() || !isFinishing())
                            Toast.makeText(StaffActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }, KeyUtils.ActionType.STAFF_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MINI_STAFF, mStaffSmall);
        outState.putParcelable(KEY_FULL_STAFF, mStaff);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResponse(Call<Staff> call, Response<Staff> response) {
        if(response.isSuccessful() && response.body() != null) {
            if(!isDestroyed() || !isFinishing())
                try {
                    mStaff = response.body();
                    updateUI();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
        }
    }

    @Override
    public void onFailure(Call<Staff> call, Throwable t) {
        if(!isDestroyed() || !isFinishing()) {
            progressLayout.showError(ContextCompat.getDrawable(this, R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.try_again), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                new AsyncTaskFetch<>(StaffActivity.this, getApplicationContext(), mStaffSmall.getId()).execute(AsyncTaskFetch.RequestType.STAFF_INFO_REQ);
                }
            });
            t.printStackTrace();
        }
    }
}
