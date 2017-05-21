package com.mxt.anitrend.view.detail.fragment;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.AnimeLinksAdapter;
import com.mxt.anitrend.adapter.recycler.details.SeriesRankingAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.async.YoutubeInitializer;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.event.InteractionListener;
import com.thefinestartist.finestwebview.FinestWebView;

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
    private FinestWebView.Builder WebView;
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

    public static AnimeLinksFragment newInstance(Series result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        AnimeLinksFragment fragment = new AnimeLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            model = getArguments().getParcelable(ARG_KEY);
        }
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
        createWebView();
        UpdateUI();
    }

    /** Creates a builder for the web view */
    private void createWebView() {
        WebView = new FinestWebView.Builder(getContext())
                .theme(R.style.FinestWebViewTheme)
                .toolbarScrollFlags(0)
                .statusBarColorRes(R.color.blackPrimaryDark_WebView)
                .toolbarColorRes(R.color.blackPrimary_WebView)
                .titleColorRes(R.color.finestWhite)
                .urlColorRes(R.color.blackPrimaryLight_WebView)
                .iconDefaultColorRes(R.color.finestWhite)
                .progressBarColorRes(R.color.finestWhite)
                .swipeRefreshColorRes(R.color.blackPrimaryDark_WebView)
                .showSwipeRefreshLayout(true)
                .menuSelector(R.drawable.selector_light_theme)
                .menuTextGravity(Gravity.CENTER_VERTICAL | Gravity.START)
                .menuTextPaddingRightRes(R.dimen.defaultMenuTextPaddingLeft)
                .dividerHeight(2)
                .gradientDivider(true)
                .webViewJavaScriptEnabled(true)
                .webViewUseWideViewPort(false)
                .webViewSupportZoom(true)
                .webViewBuiltInZoomControls(true)
                .webViewJavaScriptCanOpenWindowsAutomatically(false)
                //.setCustomAnimations(R.anim.slide_up, R.anim.hold, R.anim.hold, R.anim.slide_down)
                //.setCustomAnimations(R.anim.slide_left_in, R.anim.hold, R.anim.hold, R.anim.slide_right_out)
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_medium, R.anim.fade_in_medium, R.anim.fade_out_fast)
                .disableIconMenu(false);
    }

    public void UpdateUI() {
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
        WebView.titleDefault(external.getSite()).show(external.getUrl());
    }
}
