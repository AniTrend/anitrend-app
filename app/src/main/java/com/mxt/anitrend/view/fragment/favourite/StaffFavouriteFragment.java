package com.mxt.anitrend.view.fragment.favourite;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StaffAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.StaffActivity;

import java.util.Collections;

/**
 * Created by max on 2018/03/25.
 * StaffFavouriteFragment
 */

public class StaffFavouriteFragment extends FragmentBaseList<StaffBase, ConnectionContainer<Favourite>, BasePresenter> {

    private long userId;

    public static StaffFavouriteFragment newInstance(Bundle params) {
        Bundle args = new Bundle(params);
        StaffFavouriteFragment fragment = new StaffFavouriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            userId = getArguments().getLong(KeyUtil.arg_id);
        mAdapter = new StaffAdapter(getContext());
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        setViewModel(true);
    }

    @Override
    protected void updateUI() {
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, userId)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_STAFF_FAVOURITES_REQ, getContext());
    }

    @Override
    public void onItemClick(View target, StaffBase data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), StaffActivity.class);
                intent.putExtra(KeyUtil.arg_id, data.getId());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
        }
    }

    @Override
    public void onItemLongClick(View target, StaffBase data) {

    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<Favourite> content) {
        if(content != null) {
            if(!content.isEmpty()) {
                PageContainer<StaffBase> pageContainer = content.getConnection().getStaff();
                if(pageContainer.hasPageInfo())
                    getPresenter().setPageInfo(pageContainer.getPageInfo());
                onPostProcessed(pageContainer.getPageData());
            }
            else
                onPostProcessed(Collections.emptyList());
        } else
            onPostProcessed(Collections.emptyList());
        if(mAdapter.getItemCount() < 1)
            onPostProcessed(null);
    }
}
