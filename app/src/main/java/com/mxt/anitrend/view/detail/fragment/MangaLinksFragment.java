package com.mxt.anitrend.view.detail.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.SeriesRankingAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.custom.event.InteractionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Maxwell on 12/4/2016.
 */

public class MangaLinksFragment extends Fragment implements InteractionListener {

    @BindView(R.id.anime_details_completed) TextView mCompleted;
    @BindView(R.id.anime_details_on_hold) TextView mOnHold;
    @BindView(R.id.anime_details_dropped) TextView mDroped;
    @BindView(R.id.anime_details_planning_to_watch) TextView mPlanningToRead;
    @BindView(R.id.anime_details_watching) TextView mReading;
    @BindView(R.id.ranking_recycler) RecyclerView mRankingRecycler;

    @BindView(R.id.ad_extra)
    NativeExpressAdView expressAdView;

    private RecyclerView.Adapter mRankingAdapter;

    private Series model;
    private Unbinder unbinder;

    private final static String ARG_KEY = "arg_data";

    public MangaLinksFragment() {
        // Required empty public constructor
    }

    public static MangaLinksFragment newInstance(Series result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        MangaLinksFragment fragment = new MangaLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            model = getArguments().getParcelable(ARG_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_manga_links, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank));
        mRankingRecycler.setLayoutManager(layoutManager);
        mRankingRecycler.setNestedScrollingEnabled(false);
        mRankingRecycler.setHasFixedSize(true);
        UpdateUI();
    }

    public void UpdateUI(){
        /*Populate all the views*/
        if(model.getList_stats() != null) {
            mCompleted.setText(model.getList_stats().getCompleted());
            mOnHold.setText(model.getList_stats().getOn_hold());
            mDroped.setText(model.getList_stats().getDropped());
            mPlanningToRead.setText(model.getList_stats().getPlan_to_read());
            mReading.setText(model.getList_stats().getReading());
        }
        if(model.getRankings() != null && model.getRankings().size() > 0) {
            mRankingAdapter = new SeriesRankingAdapter(model.getRankings(), this);
            mRankingRecycler.setAdapter(mRankingAdapter);
        }
        expressAdView.loadAd((new AdRequest.Builder().build()));
    }

    @Override
    public void onItemClick(int index) {
        Toast.makeText(getContext(), "Implementation Pending!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(expressAdView != null)
            expressAdView.destroy();
        unbinder.unbind();
    }

}
