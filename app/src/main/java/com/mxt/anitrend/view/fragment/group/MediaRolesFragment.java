package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupRoleAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainer;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;

/**
 * Created by max on 2018/01/27.
 * Shared fragment between media for staff and character
 */

public class MediaRolesFragment extends FragmentBaseList<EntityGroup, ConnectionContainer<PageContainer<MediaBase>>, SeriesPresenter> {

    private QueryContainer queryContainer;
    private @KeyUtils.RequestType int requestType;

    public static MediaRolesFragment newInstance(Bundle params, @KeyUtils.MediaType String mediaType, @KeyUtils.RequestType int requestType) {
        Bundle args = new Bundle(params);
        args.putString(KeyUtils.arg_media_type, mediaType);
        args.putInt(KeyUtils.arg_request_type, requestType);
        MediaRolesFragment fragment = new MediaRolesFragment();
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
            requestType = getArguments().getInt(KeyUtils.arg_request_type);
            queryContainer = GraphUtil.getDefaultQuery(true)
                    .setVariable(KeyUtils.arg_id, getArguments().getLong(KeyUtils.arg_id))
                    .setVariable(KeyUtils.arg_media_type, getArguments().getString(KeyUtils.arg_media_type));
        }
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
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
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtils.arg_id, ((MediaBase)data).getId());
                intent.putExtra(KeyUtils.arg_media_type, ((MediaBase)data).getType());
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
            mAdapter = new GroupRoleAdapter(model, getContext());
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        queryContainer.setVariable(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
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
