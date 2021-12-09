package com.mxt.anitrend.view.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.RecommendationBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.util.media.MediaActionUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.ArrayList;
import java.util.Collections;

import io.github.wax911.library.model.request.QueryContainerBuilder;

public class MediaRecommendationsFragment extends FragmentBaseList<RecyclerItem, ConnectionContainer<PageContainer<RecommendationBase>>, MediaPresenter> {

    private @KeyUtil.MediaType String mediaType;
    private long mediaId;

    public static MediaRecommendationsFragment newInstance(Bundle args) {
        MediaRecommendationsFragment fragment = new MediaRecommendationsFragment();
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
            mediaId = getArguments().getLong(KeyUtil.arg_id);
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
        } mColumnSize = R.integer.grid_giphy_x3;
        mAdapter = new GroupSeriesAdapter(getContext());
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
                .putVariable(KeyUtil.arg_id, mediaId)
                .putVariable(KeyUtil.arg_type, mediaType);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_RECOMMENDATION_REQ, getContext());
    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<PageContainer<RecommendationBase>> content) {
        if(content != null) {
            if(!content.isEmpty()) {
                if (content.getConnection().hasPageInfo())
                    getPresenter().setPageInfo(content.getConnection().getPageInfo());
                ArrayList<RecyclerItem> entityMap = new ArrayList<>();
                for (RecommendationBase recommendation : content.getConnection().getPageData()) {
                    if (recommendation.getMediaRecommendation() != null)
                        entityMap.add(recommendation.getMediaRecommendation());
                }
                onPostProcessed(entityMap);
            }
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
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, ((MediaBase) data.getSecond()).getId());
                intent.putExtra(KeyUtil.arg_mediaType, ((MediaBase) data.getSecond()).getType());
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
        switch (target.getId()) {
            case R.id.container:
                if(getPresenter().getSettings().isAuthenticated()) {
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(((MediaBase) data.getSecond()).getId()).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.INSTANCE.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
