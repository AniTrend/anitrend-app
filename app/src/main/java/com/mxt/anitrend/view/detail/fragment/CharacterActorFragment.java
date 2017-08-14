package com.mxt.anitrend.view.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.SeriesStaffAdapter;
import com.mxt.anitrend.api.model.CharacterSmall;
import com.mxt.anitrend.api.model.StaffSmall;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.interfaces.event.InteractionListener;
import com.mxt.anitrend.view.detail.activity.StaffActivity;
import com.mxt.anitrend.viewmodel.fragment.DefaultFragment;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/08/12.
 */

public class CharacterActorFragment extends DefaultFragment<CharacterSmall> implements SwipeRefreshLayout.OnRefreshListener, InteractionListener {

    @BindView(R.id.generic_pull_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_recycler)
    StatefulRecyclerView recyclerView;
    @BindView(R.id.generic_progress_state)
    ProgressLayout progressLayout;

    private RecyclerViewAdapter<StaffSmall> mAdapter;
    private GridLayoutManager mLayoutManager;

    public static CharacterActorFragment newInstance(CharacterSmall result) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_KEY, result);
        CharacterActorFragment fragment = new CharacterActorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_base_view, container, false);
        unbinder = ButterKnife.bind(this, root);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false); //set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank));
        recyclerView.setLayoutManager(mLayoutManager);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void updateUI() {
        if (model != null) {
            mAdapter = new SeriesStaffAdapter(model.getActor(), getContext(), this);
            recyclerView.setAdapter(mAdapter);
        } else {
            progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), getString(R.string.layout_empty_response));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(int index) {
        if(index != -1) {
            Intent intent = new Intent(getActivity(), StaffActivity.class);
            intent.putExtra(StaffActivity.STAFF_INTENT_KEY, model.getActor().get(index));
            startActivity(intent);
        }
    }
}
