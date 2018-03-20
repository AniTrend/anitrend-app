package com.mxt.anitrend.view.fragment.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.Studio;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.FilterProvider;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by max on 2017/12/20.
 * series searching fragment
 */

public class SeriesSearchFragment extends FragmentBaseList<EntityGroup, List<EntityGroup>, BasePresenter> implements PublisherListener<Favourite> {

    private long userId;
    private String searchQuery;
    private @KeyUtils.SeriesType int seriesType;

    public static SeriesSearchFragment newInstance(Bundle bundle, @KeyUtils.SeriesType int seriesType) {
        Bundle args = new Bundle(bundle);
        args.putInt(KeyUtils.arg_series_type, seriesType);
        SeriesSearchFragment fragment = new SeriesSearchFragment();
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
            userId = getArguments().getLong(KeyUtils.arg_user_id);
            searchQuery = getArguments().getString(KeyUtils.arg_search_query);
            seriesType = getArguments().getInt(KeyUtils.arg_series_type);
        }
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_giphy_x3; isPager = false;
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new GroupSeriesAdapter(model, getContext());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        if(TextUtils.isEmpty(searchQuery))
            return;
        Bundle bundle = getViewModel().getParams();
        bundle.putString(KeyUtils.arg_search_query, searchQuery);
        bundle.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().requestData(seriesType == KeyUtils.ANIME? KeyUtils.ANIME_SEARCH_REQ : KeyUtils.MANGA_SEARCH_REQ, getContext());
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
                intent.putExtra(KeyUtils.arg_id, ((Media)data).getId());
                intent.putExtra(KeyUtils.arg_series_type, ((Media)data).getSeries_type());
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
                    if(TextUtils.isEmpty(((Media)data).getSeries_type()))
                        ((Media)data).setSeries_type(KeyUtils.SeriesTypes[seriesType]);
                    seriesActionUtil = new SeriesActionUtil.Builder()
                            .setModel(((Media)data)).build(getActivity());
                    seriesActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Called when the model state is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable List<EntityGroup> content) {
        super.onChanged(FilterProvider.getSeriesGroupFilter(content));
    }

    /**
     * Responds to published events, be sure to add subscribe annotation
     *
     * @param param passed event
     * @see Subscribe
     */
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Favourite param) {
        if(seriesType == KeyUtils.ANIME)
            onChanged(GroupingUtil.getGroupedSeriesType(param.getAnime()));
        else
            onChanged(GroupingUtil.getGroupedSeriesType(param.getManga()));
    }

    /**
     * Responds to published events, be sure to add subscribe annotation
     *
     * @param param passed event
     * @see Subscribe
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPublished(Studio param) {
        if(model == null)
            onChanged(GroupingUtil.getGroupedSeriesType(param.getAnime()));
    }
}
