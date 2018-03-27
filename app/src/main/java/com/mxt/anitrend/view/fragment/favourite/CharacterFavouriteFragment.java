package com.mxt.anitrend.view.fragment.favourite;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.CharacterActivity;

import java.util.Collections;

/**
 * Created by max on 2018/03/25.
 * CharacterFavouriteFragment
 */

public class CharacterFavouriteFragment extends FragmentBaseList<EntityGroup, ConnectionContainer<Favourite>, BasePresenter> {

    private long userId;

    public static CharacterFavouriteFragment newInstance(Bundle params) {
        Bundle args = new Bundle(params);
        CharacterFavouriteFragment fragment = new CharacterFavouriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            userId = getArguments().getLong(KeyUtil.arg_id);
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        setViewModel(true);
    }

    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new GroupCharacterAdapter(model, getContext());
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, userId)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_CHARACTER_FAVOURITES_REQ, getContext());
    }

    @Override
    public void onItemClick(View target, EntityGroup data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), CharacterActivity.class);
                intent.putExtra(KeyUtil.arg_id, ((CharacterBase)data).getId());
                CompatUtil.startRevealAnim(getActivity(), target, intent);
                break;
        }
    }

    @Override
    public void onItemLongClick(View target, EntityGroup data) {

    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<Favourite> content) {
        if(content != null) {
            if(!content.isEmpty()) {
                PageContainer<CharacterBase> pageContainer = content.getConnection().getCharacter();
                if(pageContainer.hasPageInfo())
                    pageInfo = pageContainer.getPageInfo();
                onPostProcessed(GroupingUtil.wrapInGroup(pageContainer.getPageData()));
            }
            else
                onPostProcessed(Collections.emptyList());
        }
        if(model == null)
            onPostProcessed(null);
    }
}
