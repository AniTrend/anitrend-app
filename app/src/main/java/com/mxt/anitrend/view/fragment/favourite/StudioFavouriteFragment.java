package com.mxt.anitrend.view.fragment.favourite;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.StudioActivity;

import java.util.Collections;

/**
 * Created by max on 2018/03/25.
 * StudioFavouriteFragment
 */

public class StudioFavouriteFragment extends FragmentBaseList<StudioBase, ConnectionContainer<Favourite>, BasePresenter> {

    private long userId;

    public static StudioFavouriteFragment newInstance(Bundle params) {
        Bundle args = new Bundle(params);
        StudioFavouriteFragment fragment = new StudioFavouriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            userId = getArguments().getLong(KeyUtil.arg_id);
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_list_x2; isPager = true;
        setViewModel(true);
    }

    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new StudioAdapter(model, getContext());
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, userId)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_STUDIO_FAVOURITES_REQ, getContext());
    }

    @Override
    public void onItemClick(View target, StudioBase data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), StudioActivity.class);
                intent.putExtra(KeyUtil.arg_id, data.getId());
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemLongClick(View target, StudioBase data) {

    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<Favourite> content) {
        if(content != null) {
            if(!content.isEmpty()) {
                PageContainer<StudioBase> pageContainer = content.getConnection().getStudio();
                if(pageContainer.hasPageInfo())
                    pageInfo = pageContainer.getPageInfo();
                onPostProcessed(pageContainer.getPageData());
            }
            else
                onPostProcessed(Collections.emptyList());
        }
        if(model == null)
            onPostProcessed(null);
    }
}
