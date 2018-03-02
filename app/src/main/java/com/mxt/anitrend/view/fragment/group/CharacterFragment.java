package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseListSingle;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.detail.CharacterActivity;

import java.util.List;

/**
 * Created by max on 2018/01/18.
 */

public class CharacterFragment extends FragmentBaseListSingle<EntityGroup, List<EntityGroup>, SeriesPresenter, Series> {

    private @KeyUtils.SeriesType int seriesType;
    private long seriesId;

    public static CharacterFragment newInstance(Bundle args) {
        CharacterFragment fragment = new CharacterFragment();
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
        } mColumnSize = R.integer.grid_giphy_x3;
        setPresenter(new SeriesPresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new GroupCharacterAdapter(model, getContext());
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
            if(content.getCharacters() != null && !content.getCharacters().isEmpty()) {
                if(isPager) {
                    if (model == null) model = GroupingUtil.getGroupedRoleCharacters(content);
                    else model.addAll(GroupingUtil.getGroupedRoleCharacters(content));
                }
                else
                    model = GroupingUtil.getGroupedRoleCharacters(content);
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
    public void onItemClick(View target, EntityGroup data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), CharacterActivity.class);
                intent.putExtra(KeyUtils.arg_id, ((CharacterBase)data).getId());
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
    public void onItemLongClick(View target, EntityGroup data) {

    }
}
