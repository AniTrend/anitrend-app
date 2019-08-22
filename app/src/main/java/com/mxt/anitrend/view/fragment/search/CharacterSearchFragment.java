package com.mxt.anitrend.view.fragment.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.collection.GroupingUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.view.activity.detail.CharacterActivity;

import java.util.Collections;

import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by max on 2017/12/20.
 */

public class CharacterSearchFragment extends FragmentBaseList<RecyclerItem, PageContainer<CharacterBase>, BasePresenter> {

    private String searchQuery;

    public static CharacterSearchFragment newInstance(Bundle args) {
        CharacterSearchFragment fragment = new CharacterSearchFragment();
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
        if(getArguments() != null)
            searchQuery = getArguments().getString(KeyUtil.arg_search);
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        mAdapter = new GroupCharacterAdapter(getContext());
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_search, searchQuery)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtil.arg_sort, KeyUtil.SEARCH_MATCH);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.CHARACTER_SEARCH_REQ, getContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable PageContainer<CharacterBase> content) {
        if(content != null) {
            if(content.hasPageInfo())
                getPresenter().setPageInfo(content.getPageInfo());
            if(!content.isEmpty())
                onPostProcessed(GroupingUtil.INSTANCE.wrapInGroup(content.getPageData()));
            else
                onPostProcessed(Collections.emptyList());
        } else
            onPostProcessed(Collections.emptyList());
        if(mAdapter.getItemCount() < 1)
            onPostProcessed(null);
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, IntPair<RecyclerItem> data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), CharacterActivity.class);
                intent.putExtra(KeyUtil.arg_id, ((CharacterBase)data.getSecond()).getId());
                CompatUtil.INSTANCE.startRevealAnim(getActivity(), target, intent);
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
    public void onItemLongClick(View target, IntPair<RecyclerItem> data) {

    }
}
