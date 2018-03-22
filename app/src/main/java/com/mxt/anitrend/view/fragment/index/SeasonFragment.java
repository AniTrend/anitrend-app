package com.mxt.anitrend.view.fragment.index;

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
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;

import java.util.List;

/**
 * Created by max on 2017/10/25.
 */

public class SeasonFragment extends FragmentBaseList<Media, List<Media>, BasePresenter> {

    private @KeyUtils.SeasonType int seasonTitle;

    public static FragmentBaseList newInstance(@KeyUtils.SeasonType int seasonTitle) {
        Bundle args = new Bundle();
        args.putInt(KeyUtils.arg_request_type, seasonTitle);
        FragmentBaseList fragment = new SeasonFragment();
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
        if(getArguments() != null && getArguments().containsKey(KeyUtils.arg_request_type))
            seasonTitle = getArguments().getInt(KeyUtils.arg_request_type, KeyUtils.WINTER);
        setPresenter(new BasePresenter(getContext()));
        isPager = true; isFilterable = true;
        mColumnSize = R.integer.single_list_x1;
        setViewModel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_status_manga).setVisible(false);
        menu.findItem(R.id.action_type_manga).setVisible(false);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null)
            mAdapter = new SeriesAnimeAdapter(model, getContext());
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        bundle.putString(KeyUtils.arg_season_title, KeyUtils.SeasonTitles[seasonTitle]);
        bundle.putInt(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().requestData(KeyUtils.BROWSE_ANIME_REQ, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Media data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), SeriesActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getId());
                intent.putExtra(KeyUtils.arg_media_type, data.getSeries_type());
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
    public void onItemLongClick(View target, Media data) {
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
