package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.presenter.fragment.SeriesPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;

import java.util.List;

/**
 * Created by max on 2018/02/03.
 * Browser fragment, creates it's own request
 */

public class BrowseFragment extends FragmentBaseList<Series, List<Series>, SeriesPresenter> {

    public static BrowseFragment newInstance(Bundle args) {
        BrowseFragment fragment = new BrowseFragment();
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
        isPager = true; isFilterable = true;
        mColumnSize = R.integer.single_list_x1;
        setPresenter(new SeriesPresenter(getContext()));
        setViewModel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_share).setVisible(false);
        menu.findItem(R.id.action_genre).setVisible(false);
        menu.findItem(R.id.action_type).setVisible(false);
        menu.findItem(R.id.action_year).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);

    }

    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new SeriesAnimeAdapter(model, getContext());
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().setParams(getArguments());
        bundle.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().requestData(KeyUtils.BROWSE_FILTER_REQ, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Series data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), SeriesActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getId());
                intent.putExtra(KeyUtils.arg_series_type, data.getSeries_type());
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
    public void onItemLongClick(View target, Series data) {
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
}
