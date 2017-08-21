package com.mxt.anitrend.view.detail.fragment;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.AnimeLinksAdapter;
import com.mxt.anitrend.adapter.recycler.details.SeriesRankingAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.base.custom.async.YoutubeInitializer;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.interfaces.event.InteractionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AnimeLinksFragment extends Fragment implements InteractionListener, android.app.LoaderManager.LoaderCallbacks {

    @BindView(R.id.anime_details_completed) TextView mCompleted;
    @BindView(R.id.anime_details_on_hold) TextView mOnHold;
    @BindView(R.id.anime_details_dropped) TextView mDroped;
    @BindView(R.id.anime_details_planning_to_watch) TextView mPlanningToWatch;
    @BindView(R.id.anime_details_watching) TextView mWatching;
    @BindView(R.id.anime_details_links) RecyclerView mExternalLinks;
    @BindView(R.id.ranking_recycler) RecyclerView mRankingRecycler;

    private Series model;
    private YoutubeFragment youFragment;
    private Unbinder unbinder;
    private YoutubeInitializer initializer;

    private RecyclerViewAdapter mExternalLinksAdapter;
    private RecyclerViewAdapter mRankingAdapter;
    private final static String ARG_KEY = "arg_data";
    private String KEY_LOADING = "PLAYER_LOADING";
    private volatile boolean isLoading;

    private LoaderManager loaderManager;

    public AnimeLinksFragment() {
        // Required empty public constructor
    }

    public static AnimeLinksFragment newInstance() {
        return new AnimeLinksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = getActivity().getLoaderManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_anime_links, container, false);
        unbinder = ButterKnife.bind(this, root);
        if(savedInstanceState != null)
            isLoading = savedInstanceState.getBoolean(KEY_LOADING);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mExternalLinks.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size)));
        mExternalLinks.setNestedScrollingEnabled(false);
        mExternalLinks.setHasFixedSize(true);
        mRankingRecycler.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank)));
        mRankingRecycler.setNestedScrollingEnabled(false);
        mRankingRecycler.setHasFixedSize(true);
    }

    public void updateUI() {
        /*Populate all the views*/
        if(model.getList_stats() != null) {
            mCompleted.setText(model.getList_stats().getCompleted());
            mOnHold.setText(model.getList_stats().getOn_hold());
            mDroped.setText(model.getList_stats().getDropped());
            mPlanningToWatch.setText(model.getList_stats().getPlan_to_watch());
            mWatching.setText(model.getList_stats().getWatching());
        }

        if(model.getExternal_links() != null && model.getExternal_links().size() > 0) {
            mExternalLinksAdapter = new AnimeLinksAdapter(model.getExternal_links(), this);
            mExternalLinks.setAdapter(mExternalLinksAdapter);
        }
        if(model.getRankings() != null && model.getRankings().size() > 0) {
            mRankingAdapter = new SeriesRankingAdapter(model.getRankings(), this);
            mRankingRecycler.setAdapter(mRankingAdapter);
        }
        if(!isLoading && model.getYoutube_id() != null) {
            loaderManager.restartLoader(getResources().getInteger(R.integer.youtube_loader),null, this);
        }
    }

    /**
     * Responds to published events
     *
     * @param param
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventPublished(Series param) {
        if(!isRemoving() && model == null) {
            model = param;
            updateUI();
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_LOADING, isLoading);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDetach() {
        if(youFragment != null) {
            youFragment.onDetach();
            youFragment = null;
        }
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        if(isLoading && initializer != null)
            initializer.cancelLoadInBackground();
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        isLoading = true;
        initializer = new YoutubeInitializer(getContext(), model.getYoutube_id());
        return initializer;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (isVisible() && (!isRemoving() || !isDetached())) {
            try {
                youFragment = (YoutubeFragment) data;
                getChildFragmentManager().beginTransaction().replace(R.id.video_view, youFragment).commit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            isLoading = false;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onItemClick(int index) {
        ExternalLink external = model.getExternal_links().get(index);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(external.getUrl()));
        startActivity(intent);
    }
}
