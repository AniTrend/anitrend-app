package com.mxt.anitrend.view.fragment.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter;
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;

/**
 * Created by max on 2017/12/20.
 * series searching fragment
 */

public class MediaSearchFragment extends FragmentBaseList<MediaBase, PageContainer<MediaBase>, BasePresenter> {

    private String searchQuery;
    private @KeyUtils.MediaType String mediaType;

    public static MediaSearchFragment newInstance(Bundle bundle, @KeyUtils.MediaType String mediaType) {
        Bundle args = new Bundle(bundle);
        args.putString(KeyUtils.arg_mediaType, mediaType);
        MediaSearchFragment fragment = new MediaSearchFragment();
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
            searchQuery = getArguments().getString(KeyUtils.arg_search);
            mediaType = getArguments().getString(KeyUtils.arg_mediaType);
        }
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new MediaAdapter(model, getContext(), true);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtils.arg_search, searchQuery)
                .putVariable(KeyUtils.arg_mediaType, mediaType)
                .putVariable(KeyUtils.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtils.arg_sort, KeyUtils.SEARCH_MATCH);
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtils.MEDIA_SEARCH_REQ, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, MediaBase data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getId());
                intent.putExtra(KeyUtils.arg_mediaType, data.getType());
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
    public void onItemLongClick(View target, MediaBase data) {
        switch (target.getId()) {
            case R.id.container:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    seriesActionUtil = new SeriesActionUtil.Builder()
                            .setModel(data).build(getActivity());
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
    public void onChanged(@Nullable PageContainer<MediaBase> content) {
        if(content != null) {
            if(content.hasPageInfo())
                pageInfo = content.getPageInfo();
            if(!content.isEmpty())
                onPostProcessed(content.getPageData());
            else
                onPostProcessed(Collections.emptyList());
        }
        if(model == null)
            onPostProcessed(null);
    }
}
