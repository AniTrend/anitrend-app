package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterStaffAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge;
import com.mxt.anitrend.model.entity.base.CharacterStaffBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.collection.GroupingUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.view.activity.detail.CharacterActivity;

import java.util.Collections;

import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by LuK1337 on 2021/05/05.
 * MediaAnimeRoleFragment
 */

public class MediaAnimeRoleFragment extends FragmentBaseList<RecyclerItem, ConnectionContainer<EdgeContainer<MediaEdge>>, MediaPresenter> {

    private long id;
    private Boolean onList;
    private @KeyUtil.MediaType String mediaType;

    private @KeyUtil.RequestType int requestType;

    public static MediaAnimeRoleFragment newInstance(Bundle params, @KeyUtil.MediaType String mediaType, @KeyUtil.RequestType int requestType) {
        Bundle args = new Bundle(params);
        args.putString(KeyUtil.arg_mediaType, mediaType);
        args.putInt(KeyUtil.arg_request_type, requestType);
        MediaAnimeRoleFragment fragment = new MediaAnimeRoleFragment();
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
        if (getArguments() != null) {
            requestType = getArguments().getInt(KeyUtil.arg_request_type);
            id = getArguments().getLong(KeyUtil.arg_id);
            onList = (Boolean) getArguments().getSerializable(KeyUtil.arg_onList);
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
        }
        mColumnSize = R.integer.grid_giphy_x3;
        isPager = true;
        mAdapter = new GroupCharacterStaffAdapter(getContext());
        setPresenter(new MediaPresenter(getContext()));
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
                .putVariable(KeyUtil.arg_id, id)
                .putVariable(KeyUtil.arg_onList, onList)
                .putVariable(KeyUtil.arg_mediaType, mediaType)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(requestType, getContext());
    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<EdgeContainer<MediaEdge>> content) {
        EdgeContainer<MediaEdge> edgeContainer;
        if (content != null && (edgeContainer = content.getConnection()) != null) {
            if (!edgeContainer.isEmpty()) {
                if (edgeContainer.hasPageInfo())
                    getPresenter().setPageInfo(edgeContainer.getPageInfo());
                if (!edgeContainer.isEmpty())
                    onPostProcessed(GroupingUtil.INSTANCE.groupCharactersByYear(edgeContainer.getEdges(), mAdapter.getData()));
                else
                    onPostProcessed(Collections.emptyList());
            }
        } else
            onPostProcessed(Collections.emptyList());
        if (mAdapter.getItemCount() < 1)
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
                intent.putExtra(KeyUtil.arg_id, ((CharacterStaffBase) data.getSecond()).getCharacter().getId());
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
