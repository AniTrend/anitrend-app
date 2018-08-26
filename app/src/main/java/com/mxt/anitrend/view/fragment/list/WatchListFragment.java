package com.mxt.anitrend.view.fragment.list;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.EpisodeAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentChannelBase;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.EpisodeUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/03.
 * WatchListFragment for anime types
 */

public class WatchListFragment extends FragmentChannelBase implements RetroCallback<ConnectionContainer<List<ExternalLink>>> {

    private long mediaId;
    private @KeyUtil.MediaType String mediaType;

    public static FragmentChannelBase newInstance(Bundle params, boolean popular) {
        Bundle args = new Bundle(params);
        FragmentChannelBase fragment = new WatchListFragment();
        args.putBoolean(KeyUtil.arg_popular, popular);
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentChannelBase newInstance(List<ExternalLink> externalLinks, boolean popular) {
        FragmentChannelBase fragment = new WatchListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(KeyUtil.arg_list_model, (ArrayList<? extends Parcelable>) externalLinks);
        args.putBoolean(KeyUtil.arg_popular, popular);
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
            mediaId = getArguments().getLong(KeyUtil.arg_id);
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
        }
        mAdapter = new EpisodeAdapter(getContext());
        mAdapter.setClickListener(clickListener);
        setPresenter(new WidgetPresenter<>(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        if(externalLinks != null) {
            boolean feed = targetLink != null && targetLink.startsWith(BuildConfig.FEEDS_LINK);
            Bundle bundle = getViewModel().getParams();
            bundle.putString(KeyUtil.arg_search, targetLink);
            bundle.putBoolean(KeyUtil.arg_feed, feed);
            getViewModel().requestData(getRequestMode(feed), getContext());
        } else {
            QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false)
                    .putVariable(KeyUtil.arg_id, mediaId)
                    .putVariable(KeyUtil.arg_type, mediaType);
            getPresenter().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
            getPresenter().requestData(KeyUtil.MEDIA_EPISODES_REQ, getContext(), this);
        }
    }

    private @KeyUtil.RequestType int getRequestMode(boolean feed) {
        if(feed)
            return isPopular? KeyUtil.EPISODE_POPULAR_REQ: KeyUtil.EPISODE_LATEST_REQ;
        return KeyUtil.EPISODE_FEED_REQ;
    }

    @Override
    public void onResponse(@NonNull Call<ConnectionContainer<List<ExternalLink>>> call, @NonNull Response<ConnectionContainer<List<ExternalLink>>> response) {
        if(isAlive() && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            ConnectionContainer<List<ExternalLink>> connectionContainer;
            if(response.isSuccessful() && (connectionContainer = response.body()) != null) {
                if(!connectionContainer.isEmpty()) {
                    externalLinks = connectionContainer.getConnection();
                    if(mAdapter.getItemCount() < 1 && externalLinks != null)
                        targetLink = EpisodeUtil.episodeSupport(externalLinks);
                    if (targetLink == null)
                        showEmpty(getString(R.string.waring_missing_episode_links));
                    else
                        makeRequest();
                }
            } else
                Log.e(TAG, ErrorUtil.getError(response));
        }
    }

    @Override
    public void onFailure(@NonNull Call<ConnectionContainer<List<ExternalLink>>> call, @NonNull Throwable throwable) {
        if(isAlive() && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            throwable.printStackTrace();
            Log.e(TAG, throwable.getMessage());
        }
    }
}
