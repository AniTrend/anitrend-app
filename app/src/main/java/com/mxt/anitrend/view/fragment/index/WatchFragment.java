package com.mxt.anitrend.view.fragment.index;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.EpisodeAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentChannelBase;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/11/03.
 */

public class WatchFragment extends FragmentChannelBase {

    public static Fragment newInstance(boolean popular) {
        WatchFragment fragment = new WatchFragment();
        Bundle args = new Bundle();
        args.putBoolean(KeyUtils.arg_popular, popular);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(List<ExternalLink> externalLinks, boolean popular) {
        WatchFragment fragment = new WatchFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(KeyUtils.arg_list_model, (ArrayList<? extends Parcelable>) externalLinks);
        args.putBoolean(KeyUtils.arg_popular, popular);
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
        setPresenter(new BasePresenter(getContext()));
        setViewModel(true);
        mColumnSize = R.integer.single_list_x1;
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null && model != null)
            mAdapter = new EpisodeAdapter(model.getChannel().getEpisode(), getContext());
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
            bundle.putString(KeyUtils.arg_search_query, targetLink);
            bundle.putBoolean(KeyUtils.arg_feed, feed);
            getViewModel().requestData(getRequestMode(feed), getContext());
        }
    }

    private @KeyUtils.RequestMode int getRequestMode(boolean feed) {
        if(feed)
            return isPopular? KeyUtils.EPISODE_POPULAR_REQ:KeyUtils.EPISODE_LATEST_REQ;
        return KeyUtils.EPISODE_FEED_REQ;
    }
}
