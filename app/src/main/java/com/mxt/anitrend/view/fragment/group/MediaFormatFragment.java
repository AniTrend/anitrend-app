package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupMediaAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;

/**
 * Created by max on 2018/01/27.
 * Shared fragment between media for staff and character
 */

public class MediaFormatFragment extends FragmentBaseList<EntityGroup, ConnectionContainer<PageContainer<MediaBase>>, MediaPresenter> {

    private long id;
    private @KeyUtil.MediaType String mediaType;

    private @KeyUtil.RequestType int requestType;

    public static MediaFormatFragment newInstance(Bundle params, @KeyUtil.MediaType String mediaType, @KeyUtil.RequestType int requestType) {
        Bundle args = new Bundle(params);
        args.putString(KeyUtil.arg_mediaType, mediaType);
        args.putInt(KeyUtil.arg_request_type, requestType);
        MediaFormatFragment fragment = new MediaFormatFragment();
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
            requestType = getArguments().getInt(KeyUtil.arg_request_type);
            id = getArguments().getLong(KeyUtil.arg_id);
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
        }
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        setPresenter(new MediaPresenter(getContext()));
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
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, ((MediaBase)data).getId());
                intent.putExtra(KeyUtil.arg_mediaType, ((MediaBase)data).getType());
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
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setModel(((MediaBase)data)).build(getActivity());
                    mediaActionUtil.startSeriesAction();
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
            mAdapter = new GroupMediaAdapter(model, getContext());
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, id)
                .putVariable(KeyUtil.arg_mediaType, mediaType)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(requestType, getContext());
    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<PageContainer<MediaBase>> content) {
        PageContainer<MediaBase> pageContainer;
        if (content != null && (pageContainer = content.getConnection()) != null) {
            if(!pageContainer.isEmpty()) {
                if (pageContainer.hasPageInfo())
                    pageInfo = pageContainer.getPageInfo();
                if (!pageContainer.isEmpty())
                    onPostProcessed(GroupingUtil.groupMediaByFormat(pageContainer.getPageData(), model));
                else
                    onPostProcessed(Collections.emptyList());
            }
        }
        if(model == null)
            onPostProcessed(null);
    }
}
