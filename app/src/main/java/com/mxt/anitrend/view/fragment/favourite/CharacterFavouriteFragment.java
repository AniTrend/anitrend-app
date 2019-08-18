package com.mxt.anitrend.view.fragment.favourite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import io.github.wax911.library.model.request.QueryContainerBuilder;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.activity.detail.CharacterActivity;

import java.util.Collections;

/**
 * Created by max on 2018/03/25.
 * CharacterFavouriteFragment
 */

public class CharacterFavouriteFragment extends FragmentBaseList<RecyclerItem, ConnectionContainer<Favourite>, BasePresenter> {

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
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        mAdapter = new GroupCharacterAdapter(getContext());
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
    }

    @Override
    protected void updateUI() {
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, userId)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_CHARACTER_FAVOURITES_REQ, getContext());
    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<Favourite> content) {
        if(content != null) {
            if(!content.isEmpty()) {
                PageContainer<CharacterBase> pageContainer = content.getConnection().getCharacters();
                if(pageContainer.hasPageInfo())
                    getPresenter().setPageInfo(pageContainer.getPageInfo());
                onPostProcessed(GroupingUtil.INSTANCE.wrapInGroup(pageContainer.getPageData()));
            }
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
