package com.mxt.anitrend.view.fragment.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrend;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.MediaUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;

public class MediaTrendListFragment extends FragmentBaseList<MediaBase, PageContainer<MediaTrend>, MediaPresenter> {

    private boolean isCompatType;

    public static MediaTrendListFragment newInstance(Bundle params, boolean isCompatType) {
        Bundle args = new Bundle(params);
        args.putBoolean(KeyUtil.arg_media_compact, isCompatType);
        MediaTrendListFragment fragment = new MediaTrendListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static MediaTrendListFragment newInstance(Bundle params) {
        Bundle args = new Bundle(params);
        args.putBoolean(KeyUtil.arg_media_compact, false);
        MediaTrendListFragment fragment = new MediaTrendListFragment();
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
            isCompatType = getArguments().getBoolean(KeyUtil.arg_media_compact);
        isPager = true; isFilterable = true;
        mColumnSize = R.integer.single_list_x1;
        setPresenter(new MediaPresenter(getContext()));
        setViewModel(true);
    }

    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new MediaAdapter(model, getContext(), isCompatType);
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_sort, getPresenter().getApplicationPref().getMediaTrendSort())
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_TREND_REQ, getContext());
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
                intent.putExtra(KeyUtil.arg_id, data.getId());
                intent.putExtra(KeyUtil.arg_mediaType, data.getType());
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
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setModel(data).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onChanged(@Nullable PageContainer<MediaTrend> content) {
        if(content != null) {
            if(content.hasPageInfo())
                pageInfo = content.getPageInfo();
            if(!content.isEmpty())
                onPostProcessed(MediaUtil.mapMediaTrend(content.getPageData()));
        }
        if(model == null)
            onPostProcessed(Collections.emptyList());
    }
}