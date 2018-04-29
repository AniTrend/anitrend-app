package com.mxt.anitrend.view.sheet;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.GiphyAdapter;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.sheet.BottomSheetGiphyList;
import com.mxt.anitrend.databinding.BottomSheetListBinding;
import com.mxt.anitrend.model.entity.giphy.Gif;
import com.mxt.anitrend.model.entity.giphy.Giphy;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.base.GiphyPreviewActivity;

import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/09.
 * giphy bottom sheet container
 */

public class BottomSheetGiphy extends BottomSheetGiphyList implements MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener {

    private BottomSheetListBinding binding;
    private @KeyUtil.RequestType int requestType;


    public static BottomSheetGiphy newInstance(Bundle bundle) {
        BottomSheetGiphy fragment = new BottomSheetGiphy();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new BasePresenter(getContext());
        mColumnSize = getResources().getInteger(R.integer.grid_giphy_x3);
        isPager = true;
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
        binding = BottomSheetListBinding.inflate(CompatUtil.getLayoutInflater(getActivity()));
        dialog.setContentView(binding.getRoot());
        unbinder = ButterKnife.bind(this, dialog);
        createBottomSheetBehavior(binding.getRoot());
        mLayoutManager = new StaggeredGridLayoutManager(mColumnSize, StaggeredGridLayoutManager.VERTICAL);
        return dialog;
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        toolbarTitle.setText(mTitle);
        toolbarSearch.setVisibility(View.VISIBLE);
        searchView.setOnSearchViewListener(this);
        searchView.setOnQueryTextListener(this);

        if(mAdapter == null)
            mAdapter = new GiphyAdapter(model, getActivity());
        injectAdapter();
        if(presenter.getApplicationPref().shouldShowTipFor(KeyUtil.KEY_GIPHY_TIP)) {
            NotifyUtil.createAlerter(getActivity(), R.string.title_new_feature, R.string.text_giphy_feature,
                    R.drawable.ic_gif_white_24dp, R.color.colorStateBlue, KeyUtil.DURATION_LONG);
            presenter.getApplicationPref().disableTipFor(KeyUtil.KEY_GIPHY_TIP);
        }
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        boolean hasQuery = !TextUtils.isEmpty(searchQuery);
        Bundle bundle = viewModel.getParams();
        bundle.putInt(KeyUtil.arg_page_offset, presenter.getCurrentOffset());
        requestType = hasQuery? KeyUtil.GIPHY_SEARCH_REQ : KeyUtil.GIPHY_TRENDING_REQ;
        if(hasQuery)
            bundle.putString(KeyUtil.arg_search, searchQuery);
        viewModel.requestData(requestType, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Giphy data) {
        presenter.notifyAllListeners(data, false);
        closeDialog();
    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, Giphy data) {
        if(getActivity() != null) {
            String index = KeyUtil.GIPHY_LARGE_DOWN_SAMPLE;
            Gif giphySample = data.getImages().get(index);
            Intent intent = new Intent(getActivity(), GiphyPreviewActivity.class);
            intent.putExtra(KeyUtil.arg_model, giphySample.getUrl());
            getActivity().startActivity(intent);
        }
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        this.searchQuery = query;
        stateLayout.showLoading();
        onRefresh();
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onSearchViewShown() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if(!TextUtils.isEmpty(searchQuery))
            searchView.setQuery(searchQuery,false);
    }

    @Override
    public void onSearchViewClosed() {

    }

    /**
     * Builder class for bottom sheet
     */
    public static class Builder extends BottomSheetBuilder {
        @Override
        public BottomSheetBase build() {
            return newInstance(bundle);
        }
    }
}