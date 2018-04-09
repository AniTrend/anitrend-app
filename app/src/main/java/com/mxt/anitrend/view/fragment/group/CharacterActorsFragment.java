package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupActorAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.model.entity.group.EntityGroup;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.GroupingUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.activity.detail.StaffActivity;

import java.util.Collections;

/**
 * Created by max on 2018/03/23.
 * Character actors with their respective media
 */

public class CharacterActorsFragment extends FragmentBaseList<EntityGroup, ConnectionContainer<EdgeContainer<MediaEdge>>, MediaPresenter> {

    private QueryContainerBuilder queryContainer;

    public static CharacterActorsFragment newInstance(Bundle args) {
        CharacterActorsFragment fragment = new CharacterActorsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            queryContainer = GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_id, getArguments().getLong(KeyUtil.arg_id));
        }
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        setPresenter(new MediaPresenter(getContext()));
        setViewModel(true);
    }

    @Override
    protected void updateUI() {
        if(mAdapter == null) {
            mAdapter = new GroupActorAdapter(model, getContext());
            ((GroupActorAdapter)mAdapter).setMediaClickListener(new ItemClickListener<EntityGroup>() {
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
                            intent.putExtra(KeyUtil.arg_id, ((MediaBase) data).getId());
                            intent.putExtra(KeyUtil.arg_mediaType, ((MediaBase) data).getType());
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
                                        .setModel((MediaBase) data).build(getActivity());
                                mediaActionUtil.startSeriesAction();
                            } else
                                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        queryContainer.putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.CHARACTER_ACTORS_REQ, getContext());
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
                Intent intent = new Intent(getActivity(), StaffActivity.class);
                intent.putExtra(KeyUtil.arg_id, ((StaffBase)data).getId());
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

    @Override
    public void onChanged(@Nullable ConnectionContainer<EdgeContainer<MediaEdge>> content) {
        EdgeContainer<MediaEdge> edgeContainer;
        if (content != null && (edgeContainer = content.getConnection()) != null) {
            if(!edgeContainer.isEmpty()) {
                if (edgeContainer.hasPageInfo())
                    getPresenter().setPageInfo(edgeContainer.getPageInfo());
                if (!edgeContainer.isEmpty())
                    onPostProcessed(GroupingUtil.groupActorMediaEdge(edgeContainer.getEdges()));
                else
                    onPostProcessed(Collections.emptyList());
            }
        } else
            onPostProcessed(Collections.emptyList());
        if(model == null)
            onPostProcessed(null);
    }
}
