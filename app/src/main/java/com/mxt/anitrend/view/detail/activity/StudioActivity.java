package com.mxt.anitrend.view.detail.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.Studio;
import com.mxt.anitrend.api.model.StudioSmall;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.view.StatefulRecyclerView;
import com.mxt.anitrend.presenter.detail.SeriesPresenter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.FilterProvider;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/05/15.
 */

public class StudioActivity extends DefaultActivity implements Callback<Studio>,
        SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener, MaterialDialog.SingleButtonCallback {

    public static final String STUDIO_PARAM = "studio_param";

    private final String KEY_MODEL_TEMP = "key_model_temp";

    @BindView(R.id.parent_coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;


    private RecyclerViewAdapter<Series> mAdapter;
    private GridLayoutManager mLayoutManager;
    private SeriesPresenter mPresenter;

    private Studio model;
    private StudioSmall model_temp;
    private MenuItem favMenuItem, filterMenuItem;

    private Snackbar snackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if(getIntent().hasExtra(STUDIO_PARAM))
            model_temp = getIntent().getParcelableExtra(STUDIO_PARAM);
        mPresenter = new SeriesPresenter(null, StudioActivity.this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false); //set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.card_col_size_home));
        recyclerView.setLayoutManager(mLayoutManager);
        progressLayout.showLoading();
        if(model_temp == null) {
            showEmpty();
            snackbar = Snackbar.make(coordinatorLayout, R.string.text_error_request, BaseTransientBottomBar.LENGTH_INDEFINITE).setAction(R.string.Ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            snackbar.show();
        }
        startInit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MODEL_TEMP, model_temp);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null)
            model_temp = savedInstanceState.getParcelable(KEY_MODEL_TEMP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base_detail, menu);
        favMenuItem = menu.findItem(R.id.action_favor_state);
        filterMenuItem = menu.findItem(R.id.action_filter);
        setFavIcon();
        return true;
    }

    protected void setFavIcon() {
        if(favMenuItem != null && model != null) {
            favMenuItem.setVisible(true);
            filterMenuItem.setVisible(true);
            favMenuItem.setIcon(
                    model.isFavourite()?
                            ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp) :
                            ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_white_24dp));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_filter:
                new DialogManager(this).createDialogSelection(getString(R.string.app_filter_sort), R.array.series_sort_types, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        mPresenter.getApiPrefs().saveSort(KeyUtils.SeriesSortTypes[which]);
                        return true;
                    }
                }, this, Arrays.asList(KeyUtils.SeriesSortTypes).indexOf(mPresenter.getApiPrefs().getSort()));
                break;
            case R.id.action_favor_state:
                if(!mPresenter.getAppPrefs().isAuthenticated()) {
                    Snackbar.make(coordinatorLayout, R.string.info_login_req, Snackbar.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
                }
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(model.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(!isDestroyed() || !isFinishing()) {
                            if (response.isSuccessful() && response.body() != null) {
                                mPresenter.displayMessage(model.isFavourite() ? getString(R.string.text_removed_from_favourites): getString(R.string.text_add_to_favourites), StudioActivity.this);
                                model.setFavourite(!model.isFavourite());
                                setFavIcon();
                            } else
                                mPresenter.displayMessage(ErrorHandler.getError(response).toString(), StudioActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(!isDestroyed() || !isFinishing())
                            mPresenter.displayMessage(t.getLocalizedMessage(), StudioActivity.this);
                    }
                }, KeyUtils.ActionType.STUDIO_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.getApiPrefs().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getApiPrefs().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Optionally allowed to override
     */
    @Override
    protected void startInit() {
        super.startInit();
        mActionBar.setTitle(model_temp.getStudio_name());
        if(model == null)
            onRefresh();
        else
            updateUI();
    }

    @Override
    protected void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                setFavIcon();
                mAdapter = new SeriesAnimeAdapter(model.getAnime(), this, mPresenter.getAppPrefs());
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(model.getAnime());
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                showEmpty();
        }
    }

    public void showError(Response body) {
        progressLayout.showError(ContextCompat.getDrawable(this, R.drawable.request_error),
                ErrorHandler.getError(body).toString(), getString(R.string.button_try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressLayout.showLoading();
                        onRefresh();
                    }
                });
    }

    public void showEmpty() {
        progressLayout.showEmpty(ContextCompat.getDrawable(this, R.drawable.request_empty), getString(R.string.layout_empty_response));
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
    public void onResponse(Call<Studio> call, Response<Studio> response) {
        if (!isDestroyed() || !isFinishing())
            if (response.isSuccessful() && response.body() != null) {
                if (response.body().getAnime() == null)
                    showEmpty();
                else {
                    model = FilterProvider.getStudioFilter(response.body(), mPresenter.getApiPrefs());
                    updateUI();
                }
            }
            else
                showError(response);
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<Studio> call, Throwable t) {
        if(!isDestroyed() || !isFinishing()) {
            t.printStackTrace();
            progressLayout.showError(ContextCompat.getDrawable(StudioActivity.this, R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.button_try_again), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressLayout.showLoading();
                    onRefresh();
                }
            });
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        new AsyncTaskFetch<>(this, this, model_temp.getId()).execute(AsyncTaskFetch.RequestType.STUDIO_INFO_REQ);
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        Toast.makeText(this, R.string.text_filter_applying, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        model = FilterProvider.getStudioFilter(model, mPresenter.getApiPrefs());
        updateUI();
    }
}