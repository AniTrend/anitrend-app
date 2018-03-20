package com.mxt.anitrend.base.custom.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout;
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.presenter.activity.ProfilePresenter;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/19.
 */

public abstract class FragmentUserListBase extends FragmentBase<MediaList, ProfilePresenter, User> implements RecyclerLoadListener,
        CustomSwipeRefreshLayout.OnRefreshAndLoadListener, SharedPreferences.OnSharedPreferenceChangeListener {

    protected @BindView(R.id.refreshLayout) CustomSwipeRefreshLayout swipeRefreshLayout;
    protected @BindView(R.id.recyclerView)
    StatefulRecyclerView recyclerView;
    protected @BindView(R.id.stateLayout)
    ProgressLayout stateLayout;

    protected List<MediaList> model;
    protected String query;
    protected boolean isLimit;

    protected RecyclerViewAdapter<MediaList> mAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    private final View.OnClickListener stateLayoutOnClick = view -> {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        showLoading();
        onRefresh();
    };

    private final View.OnClickListener snackBarOnClick = view -> {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        swipeRefreshLayout.setLoading(true);
        makeRequest();
    };

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false); //set to false if somethings fail to work properly
        mLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(mColumnSize), StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);

        swipeRefreshLayout.setOnRefreshAndLoadListener(this);
        CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, getActivity());

        return root;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to Activity.onStart of the containing Activity's lifecycle.
     * In this current context the Event bus is automatically registered for you
     * @see EventBus
     */
    @Override
    public void onStart() {
        super.onStart();
        showLoading();
        /*if(!getPresenter().hasParcel(KeyUtils.key_recycler_state) && model == null)*/
        if(model == null)
            onRefresh();
        else
            updateUI();
    }

    /**
     * Event bus automatically unregistered
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>This corresponds to {@link Activity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KeyUtils.key_pagination, isPager);
        outState.putInt(KeyUtils.key_columns, mColumnSize);
        if(getPresenter() != null) {
            outState.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
            outState.putInt(KeyUtils.arg_page_offset, getPresenter().getCurrentOffset());
        }
    }

    /**
     * Called when all saved state has been restored into the view hierarchy
     * of the fragment.  This can be used to do initialization based on saved
     * state that you are letting the view hierarchy track itself, such as
     * whether check box widgets are currently checked.  This is called
     * after {@link #onActivityCreated(Bundle)} and before
     * {@link #onStart()}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            isPager = savedInstanceState.getBoolean(KeyUtils.key_pagination);
            mColumnSize = savedInstanceState.getInt(KeyUtils.key_columns);
            if(getPresenter() != null) {
                getPresenter().setCurrentPage(savedInstanceState.getInt(KeyUtils.arg_page));
                getPresenter().setCurrentOffset(savedInstanceState.getInt(KeyUtils.arg_page_offset));
            }
        }
    }

    protected void addScrollLoadTrigger() {
        if(isPager)
            if (!recyclerView.hasOnScrollListener()) {
                getPresenter().initListener(mLayoutManager, this);
                recyclerView.addOnScrollListener(getPresenter());
            }
    }

    protected void removeScrollLoadTrigger() {
        if (isPager)
            recyclerView.clearOnScrollListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeScrollLoadTrigger();
    }

    @Override
    public void onResume() {
        super.onResume();
        addScrollLoadTrigger();
    }


    @Override
    public void showError(String error) {
        super.showError(error);
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(swipeRefreshLayout.isLoading())
            swipeRefreshLayout.setLoading(false);
        if(getPresenter() != null && getPresenter().getCurrentPage() > 1 && isPager) {
            if(stateLayout.isLoading())
                stateLayout.showContent();
            snackbar = NotifyUtil.make(stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, snackBarOnClick);
            snackbar.show();
        }
        else {
            showLoading();
            stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_warning_white_18dp, R.color.colorStateOrange),
                    error, getString(R.string.try_again), stateLayoutOnClick);
        }
    }

    @Override
    public void showEmpty(String message) {
        super.showEmpty(message);
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(swipeRefreshLayout.isLoading())
            swipeRefreshLayout.setLoading(false);
        if(getPresenter() != null && getPresenter().getCurrentPage() > 1 && isPager) {
            if(stateLayout.isLoading())
                stateLayout.showContent();
            snackbar = NotifyUtil.make(stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, snackBarOnClick);
            snackbar.show();
        }
        else {
            showLoading();
            stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_warning_white_18dp, R.color.colorStateBlue),
                    message, getString(R.string.try_again), stateLayoutOnClick);
        }
    }

    public void showContent() {
        stateLayout.showContent();
    }

    public void showLoading() {
        stateLayout.showLoading();
    }

    /**
     * While paginating if our request was a success and
     */
    public void setLimitReached() {
        if(getPresenter() != null && getPresenter().getCurrentPage() != 0) {
            swipeRefreshLayout.setLoading(false);
            isLimit = true;
        }
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main_menu thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(getPresenter() != null && isFilterable && ApplicationPref.isKeyFilter(key)) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if(swipeRefreshLayout.isRefreshing() || !stateLayout.isContent()) {
            if (mAdapter != null)
                mAdapter.clearItems();
            getPresenter().onRefreshPage();
            model = null;
        }
        makeRequest();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onLoadMore() {
        swipeRefreshLayout.setLoading(true);
        makeRequest();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearch(String query) {
        if(isAlive() && mAdapter != null && mAdapter.getFilter() != null) {
            this.query = query;
            mAdapter.getFilter().filter(query);
        }
    }

    /**
     * Set your adapter and call this method when you are done to the current
     * parents data, then call this method after
     */
    protected void injectAdapter() {
        if(model != null) {
            mAdapter.setClickListener(this);
            if (recyclerView.getAdapter() == null) {
                if (getActionMode() != null)
                    mAdapter.setActionModeCallback(getActionMode());
                if(mAdapter.getItemCount() != model.size())
                    mAdapter.onItemsInserted(model);
                recyclerView.setAdapter(mAdapter);
            } else {

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    mAdapter.onItemsInserted(model);
                }
                else {
                    if(swipeRefreshLayout.isLoading())
                        swipeRefreshLayout.setLoading(false);
                    mAdapter.onItemRangeInserted(model);
                }

                if (query != null)
                    mAdapter.getFilter().filter(query);
            }
            if (mAdapter.getItemCount() > 0) {
                showContent();
            }
        }
        else
            showEmpty(getString(R.string.layout_empty_response));
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public abstract void onItemClick(View target, MediaList data);

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public abstract void onItemLongClick(View target, MediaList data);

}
