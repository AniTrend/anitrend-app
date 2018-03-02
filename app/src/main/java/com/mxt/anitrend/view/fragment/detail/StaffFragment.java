package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseListSingle;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.detail.StaffActivity;

import java.util.List;

/**
 * Created by max on 2018/01/18.
 */

public class StaffFragment extends FragmentBaseListSingle<StaffBase, List<StaffBase>, SeriesPresenter, Series> {

    private @KeyUtils.SeriesType int seriesType;
    private long seriesId;

    public static StaffFragment newInstance(Bundle args) {
        StaffFragment fragment = new StaffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            seriesId = getArguments().getLong(KeyUtils.arg_id);
            seriesType = getArguments().getInt(KeyUtils.arg_series_type);
        }
        setPresenter(new SeriesPresenter(getContext()));
        mColumnSize = R.integer.grid_giphy_x3; isPager = false;
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new StaffAdapter(model, getContext());
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        bundle.putLong(KeyUtils.arg_id, seriesId);
        bundle.putString(KeyUtils.arg_series_type, KeyUtils.SeriesTypes[seriesType]);
        getViewModel().requestData(KeyUtils.SERIES_CHARACTER_PAGE_REQ, getContext());
    }

    @Override
    public void onChanged(@Nullable Series content) {
        if(content != null) {
            if(content.getStaff() != null && !content.getStaff().isEmpty()) {
                if(isPager) {
                    if (model == null) model = content.getStaff();
                    else model.addAll(content.getStaff());
                }
                else
                    model = content.getStaff();
                updateUI();
            } else {
                if (isPager)
                    setLimitReached();
                if (model == null || model.size() < 1)
                    showEmpty(getString(R.string.layout_empty_response));
            }
        } else
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
    public void onItemClick(View target, StaffBase data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), StaffActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getId());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
        }
    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, StaffBase data) {

    }
}
