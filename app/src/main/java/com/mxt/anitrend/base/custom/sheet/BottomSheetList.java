package com.mxt.anitrend.base.custom.sheet;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout;
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.List;

import butterknife.BindView;

public abstract class BottomSheetList<T extends Parcelable> extends BottomSheetBase<List<T>> implements ItemClickListener<T>, Observer<List<T>>, RecyclerLoadListener, CustomSwipeRefreshLayout.OnRefreshAndLoadListener {

    protected List<T> model;

    protected @BindView(R.id.stateLayout) ProgressLayout stateLayout;
    protected @BindView(R.id.recyclerView) StatefulRecyclerView recyclerView;

    protected RecyclerViewAdapter<T> mAdapter;
    protected StaggeredGridLayoutManager mLayoutManager;

    protected int mColumnSize;
    protected boolean isPager, isLimit;

    private final View.OnClickListener stateLayoutOnClick = view -> {
        stateLayout.showLoading();
        onRefresh();
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            model = getArguments().getParcelableArrayList(KeyUtil.arg_list_model);
        setViewModel(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        stateLayout.showLoading();
        if(model == null)
            onRefresh();
        else
            updateUI();
    }

    protected void addScrollLoadTrigger() {
        if(isPager)
            if (!recyclerView.hasOnScrollListener()) {
                presenter.initListener(mLayoutManager, this);
                recyclerView.addOnScrollListener(presenter);
            }
    }

    protected void removeScrollLoadTrigger() {
        if (isPager)
            recyclerView.clearOnScrollListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        addScrollLoadTrigger();
    }

    @Override
    public void onResume() {
        removeScrollLoadTrigger();
        super.onResume();
    }

    /**
     * Set your adapter and call this method when you are done to the current
     * parents data, then call this method after
     */
    protected void injectAdapter() {
        mAdapter.setClickListener(this);
        if (recyclerView.getAdapter() == null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setNestedScrollingEnabled(true);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter);
        }
        else
            mAdapter.onItemsInserted(model);
        if (mAdapter.getItemCount() < 1)
            stateLayout.showEmpty(CompatUtil.getDrawable(getContext(),  R.drawable.ic_new_releases_white_24dp, R.color.colorStateBlue), getString(R.string.layout_empty_response));
        else
            stateLayout.showContent();
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    protected abstract void updateUI();

    @SuppressWarnings("unchecked")
    protected void setViewModel(boolean stateSupported) {
        if(viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(ViewModelBase.class);
            viewModel.setContext(getContext());
            if(!viewModel.getModel().hasActiveObservers())
                viewModel.getModel().observe(this, this);
            if(stateSupported)
                viewModel.setState(this);
        }
    }

    /**
     * While paginating if our request was a success and
     */
    public void setLimitReached() {
        if(presenter != null && presenter.getCurrentPage() != 0)
            isLimit = true;
    }

    @Override
    public void onRefresh() {
        model = null;
        if(mAdapter != null)
            mAdapter.clearItems();
        if (isPager && presenter != null)
            presenter.onRefreshPage();
        makeRequest();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onLoadMore() {
        makeRequest();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    public abstract void makeRequest();

    /**
     * Called when the data is changed.
     *
     * @param data The new data
     */
    @Override
    public void onChanged(@Nullable List<T> data) {
        Log.d(TAG, "onChanged(@Nullable List<T> data) invoked");
    }

    @Override
    public void showError(String error) {
        super.showError(error);
        stateLayout.showLoading();
        stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_emoji_cry),
                error, getString(R.string.try_again), stateLayoutOnClick);
    }

    @Override
    public void showEmpty(String message) {
        super.showEmpty(message);
        stateLayout.showLoading();
        stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_emoji_sweat),
                message, getString(R.string.try_again) , stateLayoutOnClick);
    }
}