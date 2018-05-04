package com.mxt.anitrend.view.fragment.list;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;

/**
 * Created by max on 2017/12/18.
 * media list fragment
 */

public class MediaListFragment extends FragmentBaseList<MediaList, PageContainer<MediaList>, MediaPresenter> implements BaseConsumer.onRequestModelChange<MediaList> {

    private long userId;
    private String userName;
    private QueryContainerBuilder queryContainer;
    private @KeyUtil.MediaType String mediaType;

    public static MediaListFragment newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        MediaListFragment fragment = new MediaListFragment();
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
        if(getArguments() != null) {
            userId = getArguments().getLong(KeyUtil.arg_id);
            userName = getArguments().getString(KeyUtil.arg_userName);
            queryContainer = getArguments().getParcelable(KeyUtil.arg_graph_params);
            mediaType = getArguments().getString(KeyUtil.arg_mediaType);
        }
        mColumnSize = R.integer.grid_list_x2; isFilterable = true; isPager = true;
        hasSubscriber = true;
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
        if (getContext() != null)
            switch (item.getItemId()) {
                case R.id.action_sort:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_sort, CompatUtil.getIndexOf(KeyUtil.MediaListSortType,
                            getPresenter().getApplicationPref().getMediaListSort()), CompatUtil.capitalizeWords(KeyUtil.MediaListSortType),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getApplicationPref().setMediaListSort(KeyUtil.MediaListSortType[dialog.getSelectedIndex()]);
                            });
                    return true;
                case R.id.action_order:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_order, CompatUtil.getIndexOf(KeyUtil.SortOrderType,
                            getPresenter().getApplicationPref().getSortOrder()), CompatUtil.getStringList(getContext(), R.array.order_by_types),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getApplicationPref().saveSortOrder(KeyUtil.SortOrderType[dialog.getSelectedIndex()]);
                            });
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null) {
            mAdapter = new MediaListAdapter(model, getContext());
            ((MediaListAdapter)mAdapter).setCurrentUser(userName);
        }
        if (!CompatUtil.isEmpty(model))
            injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        ApplicationPref pref = getPresenter().getApplicationPref();

        if (userId != 0)
            queryContainer.putVariable(KeyUtil.arg_userId, userId);
        else
            queryContainer.putVariable(KeyUtil.arg_userName, userName);

        queryContainer.putVariable(KeyUtil.arg_mediaType, mediaType)
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(KeyUtil.arg_sort, pref.getMediaListSort() + pref.getSortOrder());

        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_LIST_BROWSE_REQ, getContext());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, MediaList data) {
        switch (target.getId()) {
            case R.id.series_image:
                MediaBase mediaBase = data.getMedia();
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, data.getMediaId());
                intent.putExtra(KeyUtil.arg_mediaType, mediaBase.getType());
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
    public void onItemLongClick(View target, MediaList data) {
        switch (target.getId()) {
            case R.id.series_image:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(data.getMediaId()).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("SwitchIntDef")
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<MediaList> consumer) {
        int pairIndex;
        if(getPresenter().isCurrentUser(userId, userName)) {
            Optional<IntPair<MediaList>> pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
            if (pairOptional.isPresent())
                switch (consumer.getRequestMode()) {
                    case KeyUtil.MUT_SAVE_MEDIA_LIST:
                        pairIndex = pairOptional.get().getFirst();
                        if(queryContainer == null || CompatUtil.equals(queryContainer.getVariable(KeyUtil.arg_status), consumer.getChangeModel().getStatus())) {
                            model.set(pairIndex, consumer.getChangeModel());
                            mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                        } else {
                            model.remove(pairIndex);
                            mAdapter.onItemRemoved(pairIndex);
                        }
                        break;
                    case KeyUtil.MUT_DELETE_MEDIA_LIST:
                        pairIndex = pairOptional.get().getFirst();
                        model.remove(pairIndex);
                        mAdapter.onItemRemoved(pairIndex);
                        break;
                }
        }
    }

    @Override
    public void onChanged(@Nullable PageContainer<MediaList> content) {
        if(content != null) {
            if(content.hasPageInfo())
                getPresenter().setPageInfo(content.getPageInfo());
            if(!content.isEmpty())
                onPostProcessed(content.getPageData());
            else
                onPostProcessed(Collections.emptyList());
        } else
            onPostProcessed(Collections.emptyList());
        if(model == null)
            onPostProcessed(null);
    }
}