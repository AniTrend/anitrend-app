package com.mxt.anitrend.view.fragment.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.MediaAdapter;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import java.util.Collections;

/**
 * Created by max on 2018/03/25.
 * StudioMediaFragment
 */

public class StudioMediaFragment extends FragmentBaseList<MediaBase, ConnectionContainer<PageContainer<MediaBase>>, MediaPresenter> {

    private long id;

    public static StudioMediaFragment newInstance(Bundle args) {
        StudioMediaFragment fragment = new StudioMediaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            id = getArguments().getLong(KeyUtil.arg_id);
        mColumnSize = R.integer.grid_giphy_x3; isPager = true;
        isFilterable = true;
        mAdapter = new MediaAdapter(getContext(), true);
        setPresenter(new MediaPresenter(getContext()));
        setViewModel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_genre).setVisible(false);
        menu.findItem(R.id.action_tag).setVisible(false);
        menu.findItem(R.id.action_type).setVisible(false);
        menu.findItem(R.id.action_year).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(getContext() != null)
            switch (item.getItemId()) {
                case R.id.action_sort:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_sort, CompatUtil.INSTANCE.getIndexOf(KeyUtil.MediaSortType,
                            getPresenter().getApplicationPref().getMediaSort()), CompatUtil.INSTANCE.capitalizeWords(KeyUtil.MediaSortType),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getApplicationPref().setMediaSort(KeyUtil.MediaSortType[dialog.getSelectedIndex()]);
                            });
                    return true;
                case R.id.action_order:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_order, CompatUtil.INSTANCE.getIndexOf(KeyUtil.SortOrderType,
                            getPresenter().getApplicationPref().getSortOrder()), CompatUtil.INSTANCE.getStringList(getContext(), R.array.order_by_types),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getApplicationPref().saveSortOrder(KeyUtil.SortOrderType[dialog.getSelectedIndex()]);
                            });
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateUI() {
        setSwipeRefreshLayoutEnabled(false);
        injectAdapter();
    }

    @Override
    public void makeRequest() {
        ApplicationPref pref = getPresenter().getApplicationPref();
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_id, id)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtil.arg_sort, pref.getMediaSort() + pref.getSortOrder());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.STUDIO_MEDIA_REQ, getContext());
    }

    @Override
    public void onChanged(@Nullable ConnectionContainer<PageContainer<MediaBase>> content) {
        PageContainer<MediaBase> pageContainer;
        if (content != null && (pageContainer = content.getConnection()) != null) {
            if(!pageContainer.isEmpty()) {
                if (pageContainer.hasPageInfo())
                    getPresenter().setPageInfo(pageContainer.getPageInfo());
                if (!pageContainer.isEmpty())
                    onPostProcessed(pageContainer.getPageData());
                else
                    onPostProcessed(Collections.emptyList());
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
    public void onItemClick(View target, IntPair<MediaBase> data) {
        switch (target.getId()) {
            case R.id.container:
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, data.getSecond().getId());
                intent.putExtra(KeyUtil.arg_mediaType, data.getSecond().getType());
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
    public void onItemLongClick(View target, IntPair<MediaBase> data) {
        switch (target.getId()) {
            case R.id.container:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(data.getSecond().getId()).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
