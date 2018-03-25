package com.mxt.anitrend.view.fragment.favourite;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;
import java.util.Objects;

/**
 * Created by max on 2018/03/25.
 * MediaFavouriteFragment
 */

public class MediaFavouriteFragment extends FragmentBaseList<MediaBase, ConnectionContainer<Favourite>, BasePresenter> {

    private long userId;
    private @KeyUtils.MediaType String mediaType;

    public static MediaFavouriteFragment newInstance(Bundle params, @KeyUtils.MediaType String mediaType) {
        Bundle args = new Bundle(params);
        args.putString(KeyUtils.arg_mediaType, mediaType);
        MediaFavouriteFragment fragment = new MediaFavouriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            userId = getArguments().getLong(KeyUtils.arg_id);
            mediaType = getArguments().getString(KeyUtils.arg_mediaType);
        }
        setPresenter(new BasePresenter(getContext()));
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        setViewModel(true);
    }

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
                .putVariable(KeyUtils.arg_id, userId)
                .putVariable(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        getViewModel().requestData(Objects.equals(mediaType, KeyUtils.ANIME) ?
                KeyUtils.USER_ANIME_FAVOURITES_REQ : KeyUtils.USER_MANGA_FAVOURITES_REQ, getContext());
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

    @Override
    public void onChanged(@Nullable ConnectionContainer<Favourite> content) {
        if(content != null) {
            if(!content.isEmpty()) {
                PageContainer<MediaBase> pageContainer = Objects.equals(mediaType, KeyUtils.ANIME) ?
                        content.getConnection().getAnime() : content.getConnection().getManga();
                if(pageContainer.hasPageInfo())
                    pageInfo = pageContainer.getPageInfo();
                onPostProcessed(pageContainer.getPageData());
            }
            else
                onPostProcessed(Collections.emptyList());
        }
        if(model == null)
            onPostProcessed(null);
    }
}
