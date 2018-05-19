package com.mxt.anitrend.view.sheet;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.UserAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout;
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;
import com.mxt.anitrend.databinding.BottomSheetListBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BottomSheetListUsers extends BottomSheetBase<PageContainer<UserBase>> implements ItemClickListener<UserBase>, Observer<PageContainer<UserBase>>,
        RecyclerLoadListener, CustomSwipeRefreshLayout.OnRefreshAndLoadListener {


    protected List<UserBase> model;

    protected @BindView(R.id.stateLayout)
    ProgressLayout stateLayout;
    protected @BindView(R.id.recyclerView)
    StatefulRecyclerView recyclerView;

    protected RecyclerViewAdapter<UserBase> mAdapter;
    protected StaggeredGridLayoutManager mLayoutManager;

    protected int mColumnSize;
    protected boolean isPager, isLimit;

    private int count;
    private long userId;
    private @KeyUtil.RequestType int requestType;

    private final View.OnClickListener stateLayoutOnClick = view -> {
        stateLayout.showLoading();
        onRefresh();
    };

    public static BottomSheetListUsers newInstance(Bundle bundle) {
        BottomSheetListUsers fragment = new BottomSheetListUsers();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            count = getArguments().getInt(KeyUtil.arg_model);
            userId = getArguments().getLong(KeyUtil.arg_userId);
            requestType = getArguments().getInt(KeyUtil.arg_request_type);
        }
        setViewModel(true); isPager = true;
        presenter = new BasePresenter(getContext());
        mColumnSize = getResources().getInteger(R.integer.single_list_x1);
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     *
     * @param savedInstanceState
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        BottomSheetListBinding binding = BottomSheetListBinding.inflate(CompatUtil.getLayoutInflater(getActivity()));
        dialog.setContentView(binding.getRoot());
        unbinder = ButterKnife.bind(this, dialog);
        createBottomSheetBehavior(binding.getRoot());
        mLayoutManager = new StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        toolbarTitle.setText(getString(mTitle, count));
        searchView.setVisibility(View.GONE);
        stateLayout.showLoading();
        if(CompatUtil.isEmpty(model))
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
        removeScrollLoadTrigger();
        super.onPause();
    }

    @Override
    public void onResume() {
        addScrollLoadTrigger();
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
    private void updateUI() {
        if(mAdapter == null)
            mAdapter = new UserAdapter(model, getContext());
        injectAdapter();
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
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, userId)
                .putVariable(KeyUtil.arg_page, presenter.getCurrentPage());

        viewModel.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        viewModel.requestData(requestType, getContext());
    }

    /**
     * Handles post view model result after extraction or processing
     * @param content The main data model for the class
     */
    protected void onPostProcessed(@Nullable List<UserBase> content) {
        if(content != null) {
            if(content.size() > 0) {
                if(isPager) {
                    if (model == null) model = content;
                    else model.addAll(content);
                }
                else
                    model = content;
                updateUI();
            } else {
                if (isPager || (presenter.getPageInfo() != null && !presenter.getPageInfo().hasNextPage()))
                    setLimitReached();
                if (model == null || model.size() < 1)
                    showEmpty(getString(R.string.layout_empty_response));
            }
        } else
            showEmpty(getString(R.string.layout_empty_response));
    }

    /**
     * Called when the data is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable PageContainer<UserBase> content) {
        if(content != null) {
            if(content.hasPageInfo())
                presenter.setPageInfo(content.getPageInfo());
            if(!content.isEmpty())
                onPostProcessed(content.getPageData());
            else
                onPostProcessed(Collections.emptyList());
        } else
            onPostProcessed(Collections.emptyList());
        if(model == null)
            onPostProcessed(null);
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

    @Override
    public void onItemClick(View target, UserBase data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KeyUtil.arg_id, data.getId());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
        }
    }

    @Override
    public void onItemLongClick(View target, UserBase data) {

    }



    /**
     * Builder class for bottom sheet
     */
    public static class Builder extends BottomSheetBuilder {

        @Override
        public BottomSheetBase build() {
            return newInstance(bundle);
        }

        public Builder setUserId(long userId) {
            bundle.putLong(KeyUtil.arg_userId, userId);
            return this;
        }

        public Builder setModelCount(int count) {
            bundle.putInt(KeyUtil.arg_model, count);
            return this;
        }

        public BottomSheetBuilder setRequestType(@KeyUtil.RequestType int requestType) {
            bundle.putInt(KeyUtil.arg_request_type, requestType);
            return this;
        }
    }
}
