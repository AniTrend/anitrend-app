package com.mxt.anitrend.viewmodel.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.async.SortHelper;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.StatefulRecyclerView;
import com.mxt.anitrend.event.FragmentCallback;
import com.mxt.anitrend.event.RecyclerLoadListener;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.utils.ErrorHandler;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/04/13.
 * Default fragment for lists of data which includes facilities to instantiate recyclers and inflate layouts
 * <b>N.B. onStart() -> super() method must be called after your desired override implementation</b> <br/>
 *
 * <b>N.B. Optional super().methodName exclusions:</b> <br/>
 *
 * onCreate() - if you have not passed any params of type
 * <br/>
 *
 * @see T
 * <br/>
 *
 * mColumnSize - Must be assigned before you call super().onCreateView inside the inheriting class
 * mListener - Must be assigned before super.onStart() inside the inheriting class
 * mFragmentPresenter - Must be assigned before super.onStart() inside the inheriting class
 */
public abstract class DefaultListFragment <T extends Parcelable> extends Fragment implements Callback<List<T>>, RecyclerLoadListener,
        SwipeRefreshLayout.OnRefreshListener, FragmentCallback<String>, SharedPreferences.OnSharedPreferenceChangeListener {

    public @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    public @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    public @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    protected final static String ARG_KEY = "arg_data";

    private final String KEY_MODEL_STATE = "model_key";
    private final String MODEL_LIMIT = "loading_limit_reached";
    private final String MODEL_PAGE = "current_page_number";
    private final String KEY_PAGINATE = "allowed_paginate";

    protected CommonPresenter<List<T>> mPresenter;
    protected Unbinder unbinder;
    protected List<T> model;

    protected int mPage = 1;
    protected boolean isLimit;
    protected boolean isPaginate;

    protected RecyclerViewAdapter<T> mAdapter;
    private GridLayoutManager mLayoutManager;

    protected SortHelper<T> mSorter;

    protected @IntegerRes int mColumnSize;

    /**
     * Override and set mPresenter, mColumnSize, and fetch argument/s
     * @See CommonPresenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Override this as normal the save instance for your model will be managed for you,
     * so there is no need to to restore the state of your model from save state.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_base_view, container, false);
        unbinder = ButterKnife.bind(this, root);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false); //set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(mColumnSize));
        recyclerView.setLayoutManager(mLayoutManager);
        progressLayout.showLoading();
        return root;
    }

    protected void addScrollLoadTrigger() {
        if (recyclerView != null && !recyclerView.hasOnScrollListener() && isPaginate) {
            mPresenter.initListener(mLayoutManager, mPage, this);
            recyclerView.addOnScrollListener(mPresenter);
        }
    }

    protected void removeScrollLoadTrigger() {
        if (recyclerView != null && isPaginate) {
            recyclerView.clearOnScrollListeners();
        }
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to Activity.onStart of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if(mPresenter.getSavedParse() == null && model == null)
            onRefresh();
        else
            updateUI();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Override as normal, the saving of the model is also managed for
     * you so no need to save it.
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (model != null && model.size() < 45) {
            outState.putParcelableArrayList(KEY_MODEL_STATE, (ArrayList<? extends Parcelable>) model);
            outState.putBoolean(MODEL_LIMIT, isLimit);
            outState.putInt(MODEL_PAGE, mPage);
        }
        outState.putBoolean(KEY_PAGINATE, isPaginate);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            model = savedInstanceState.getParcelableArrayList(KEY_MODEL_STATE);
            mPage = savedInstanceState.getInt(MODEL_PAGE);
            isLimit = savedInstanceState.getBoolean(MODEL_LIMIT);
            isPaginate = savedInstanceState.getBoolean(KEY_PAGINATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        removeScrollLoadTrigger();
        mPresenter.getApiPrefs().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
        mPresenter.destroySuperToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        addScrollLoadTrigger();
        mPresenter.getApiPrefs().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }



    /**
     * No need to call ButterKnife.unbind()
     * method is already called for you
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mPresenter.destroySuperToast();
        if(mSorter != null)
            mSorter.cancel(false);
    }

    /**
     * Is automatically called in the @onStart Method
     */
    protected abstract void updateUI();

    public void showError(Response body) {
        progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error),
                ErrorHandler.getError(body).toString(), getString(R.string.button_try_again), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressLayout.showLoading();
                onRefresh();
            }
        });
    }

    public void showEmpty(String message) {
        progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), message);
    }

    @Override
    public void onResponse(Call<List<T>> call, Response<List<T>> response) {
        if(isVisible() && (!isDetached() || !isRemoving()))
            if(response.isSuccessful() && response.body() != null) {
                if (!isPaginate) {
                    model = response.body();
                } else if (model == null) {
                    model = response.body();
                } else {
                    model.addAll(response.body());
                }
                updateUI();
            } else {
                showError(response);
            }
    }

    @Override
    public void onFailure(Call<List<T>> call, Throwable t) {
        if(isVisible() && (!isDetached() || !isRemoving())) {
            t.printStackTrace();
            progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.button_try_again), new View.OnClickListener() {
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
        mPage = 1;
        model = null;
        if (mPresenter != null) {
            mPresenter.onRefreshPage();
        }
        makeRequest();
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
        mPage = 1;
        model = null;
        if (mPresenter != null)
            mPresenter.onRefreshPage();
        update();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    public abstract void makeRequest();

    /**
     * Provides the next page number
     * @param next next page
     */
    public abstract void onLoadMore(int next);

    /**
     * Normal fragments
     */
    @Override
    public abstract void update();

    /**
     * Search page fragments
     *
     * @param query
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void update(String query) {
        if(mAdapter != null) {
            mAdapter.getFilter().filter(query);
        }
    }
}
