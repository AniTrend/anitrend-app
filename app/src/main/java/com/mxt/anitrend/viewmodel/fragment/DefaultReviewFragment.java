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
import com.mxt.anitrend.presenter.index.FragmentReviewPresenter;
import com.mxt.anitrend.util.ErrorHandler;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/05/02.
 * @see DefaultListFragment
 */
public abstract class DefaultReviewFragment<T extends Parcelable> extends Fragment implements Callback<T>,
        SwipeRefreshLayout.OnRefreshListener, FragmentCallback<String>, SharedPreferences.OnSharedPreferenceChangeListener, RecyclerLoadListener {

    public @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    public @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    public @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    protected final static String ARG_KEY = "arg_data";

    private final String KEY_MODEL_STATE = "model_key";
    private final String MODEL_LIMIT = "loading_limit_reached";
    private final String MODEL_PAGE = "current_page_number";
    private final String MODEL_TYPE = "model_type";

    protected FragmentReviewPresenter mPresenter;
    protected Unbinder unbinder;
    protected T model;

    protected int mPage = 1;
    protected boolean isLimit;
    protected String model_type;

    protected RecyclerViewAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    protected SortHelper<T> mSorter;

    protected @IntegerRes
    int mColumnSize;

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
        if(!recyclerView.hasOnScrollListener()) {
            mPresenter.initListener(mLayoutManager, mPage, this);
            recyclerView.addOnScrollListener(mPresenter);
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
        outState.putString(MODEL_TYPE, model_type);
        /*if(model != null && mPage < 2) {
            outState.putParcelable(KEY_MODEL_STATE, model);
            outState.putInt(MODEL_PAGE, mPage);
            outState.putBoolean(MODEL_LIMIT, isLimit);
        }*/
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            model_type = savedInstanceState.getString(MODEL_TYPE);
            model = savedInstanceState.getParcelable(KEY_MODEL_STATE);
            mPage = savedInstanceState.getInt(MODEL_PAGE);
            isLimit = savedInstanceState.getBoolean(MODEL_LIMIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.getAppPrefs().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
        mPresenter.destroySuperToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getAppPrefs().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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

    public void showEmpty() {
        progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), "No results to display");
    }

    @Override
    public abstract void onResponse(Call<T> call, Response<T> response);

    @Override
    public void onFailure(Call<T> call, Throwable t) {
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
    public abstract void onRefresh();

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
        update();
    }

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