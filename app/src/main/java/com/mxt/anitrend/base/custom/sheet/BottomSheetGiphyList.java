package com.mxt.anitrend.base.custom.sheet;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout;
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;
import com.mxt.anitrend.base.interfaces.event.ResponseCallback;
import com.mxt.anitrend.model.entity.giphy.Giphy;
import com.mxt.anitrend.model.entity.giphy.GiphyContainer;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import butterknife.BindView;

/**
 * Created by max on 2017/12/09.
 * giphy loading list bottom sheet
 */

public abstract class BottomSheetGiphyList extends BottomSheetBase implements ItemClickListener<Giphy>, Observer<GiphyContainer>,
        ResponseCallback, RecyclerLoadListener, CustomSwipeRefreshLayout.OnRefreshAndLoadListener {

    protected GiphyContainer container;

    protected @BindView(R.id.stateLayout) ProgressLayout stateLayout;
    protected @BindView(R.id.recyclerView) StatefulRecyclerView recyclerView;

    protected RecyclerViewAdapter<Giphy> mAdapter;
    protected StaggeredGridLayoutManager mLayoutManager;
    protected ViewModelBase<GiphyContainer> viewModel;

    protected int mColumnSize;
    protected boolean isPager, isLimit;

    private final View.OnClickListener stateLayoutOnClick = view -> {
        stateLayout.showLoading();
        onRefresh();
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewModel(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        stateLayout.showLoading();
        if(mAdapter.getItemCount() < 1)
            onRefresh();
        else
            updateUI();
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
        outState.putBoolean(KeyUtil.key_pagination, isPager);
        outState.putInt(KeyUtil.key_columns, mColumnSize);
        if(presenter != null) {
            outState.putInt(KeyUtil.arg_page, presenter.getCurrentPage());
            outState.putInt(KeyUtil.arg_page_offset, presenter.getCurrentOffset());
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
            isPager = savedInstanceState.getBoolean(KeyUtil.key_pagination);
            mColumnSize = savedInstanceState.getInt(KeyUtil.key_columns);
            if(presenter != null) {
                presenter.setCurrentPage(savedInstanceState.getInt(KeyUtil.arg_page));
                presenter.setCurrentOffset(savedInstanceState.getInt(KeyUtil.arg_page_offset));
            }
        }
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
        removeScrollLoadTrigger();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        addScrollLoadTrigger();
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
        if (mAdapter.getItemCount() < 1)
            stateLayout.showEmpty(CompatUtil.INSTANCE.getDrawable(getContext(),  R.drawable.ic_new_releases_white_24dp, R.color.colorStateBlue), getString(R.string.layout_empty_response));
        else
            stateLayout.showContent();
    }

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
        if(presenter != null && presenter.getCurrentOffset() != 0)
            isLimit = true;
    }

    @Override
    public void onRefresh() {
        if(mAdapter != null)
            mAdapter.clearDataSet();
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
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    protected abstract void updateUI();

    /**
     * All new or updated network requests should be handled in this method
     */
    public abstract void makeRequest();

    /**
     * Called when the data is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable GiphyContainer content) {
        if(content != null && !CompatUtil.INSTANCE.isEmpty(content.getData())) {
            if(isPager) {
                if (mAdapter.getItemCount() < 1)
                    mAdapter.onItemsInserted(content.getData());
                else
                    mAdapter.onItemRangeInserted(content.getData());
            }
            else
                mAdapter.onItemsInserted(content.getData());
            updateUI();
        } else {
            if (isPager)
                setLimitReached();
            if (mAdapter.getItemCount() < 1)
                showEmpty(getString(R.string.layout_empty_response));
        }
    }

    @Override
    public void showError(String error) {
        super.showError(error);
        stateLayout.showError(CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_emoji_cry),
                error, getString(R.string.try_again), stateLayoutOnClick);
    }

    @Override
    public void showEmpty(String message) {
        super.showEmpty(message);
        stateLayout.showError(CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_emoji_sweat),
                message, getString(R.string.try_again) , stateLayoutOnClick);
    }

}
