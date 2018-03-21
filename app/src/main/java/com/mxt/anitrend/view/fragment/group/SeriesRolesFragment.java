package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupRoleCharacterAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Character;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by max on 2018/01/27.
 */

public class SeriesRolesFragment extends FragmentBaseList<EntityGroup, List<EntityGroup>, SeriesPresenter> {

    private @KeyUtils.MediaType
    int seriesType;

    public static SeriesRolesFragment newInstance(Bundle args) {
        SeriesRolesFragment fragment = new SeriesRolesFragment();
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
            seriesType = getArguments().getInt(KeyUtils.arg_series_type);
        mColumnSize = R.integer.grid_giphy_x3;
        setPresenter(new SeriesPresenter(getContext()));
        setViewModel(true);
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
                Intent intent = new Intent(getActivity(), SeriesActivity.class);
                intent.putExtra(KeyUtils.arg_id, ((MediaBase)data).getId());
                intent.putExtra(KeyUtils.arg_series_type, ((MediaBase)data).getSeries_type());
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
        switch (target.getId()) {
            case R.id.container:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    seriesActionUtil = new SeriesActionUtil.Builder()
                            .setModel(((MediaBase)data)).build(getActivity());
                    seriesActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new GroupRoleCharacterAdapter(model, getContext());
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Character param) {
        if(seriesType == KeyUtils.ANIME)
            onChanged(GroupingUtil.getGroupedSeriesBaseType(param.getAnime()));
        else
            onChanged(GroupingUtil.getGroupedSeriesBaseType(param.getManga()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Staff param) {
        if(seriesType == KeyUtils.ANIME)
            onChanged(GroupingUtil.getGroupedSeriesBaseType(param.getAnime_staff()));
        else
            onChanged(GroupingUtil.getGroupedSeriesBaseType(param.getManga_staff()));
    }
}
