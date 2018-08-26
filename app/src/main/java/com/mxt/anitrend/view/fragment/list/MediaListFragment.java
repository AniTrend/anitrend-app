package com.mxt.anitrend.view.fragment.list;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.MediaListAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.MediaListCollection;
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.MediaListCollectionBase;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaActionUtil;
import com.mxt.anitrend.util.MediaListUtil;
import com.mxt.anitrend.util.MediaUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.MediaActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

/**
 * Created by max on 2017/12/18.
 * media list fragment
 */

public class MediaListFragment extends FragmentBaseList<MediaList, PageContainer<MediaListCollection>, MediaPresenter> implements BaseConsumer.onRequestModelChange<MediaList> {

    protected long userId;
    protected String userName;
    protected @KeyUtil.MediaType String mediaType;
    protected MediaListOptions mediaListOptions;

    protected MediaListCollectionBase mediaListCollectionBase;
    protected QueryContainerBuilder queryContainer;

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
        mColumnSize = R.integer.grid_list_x2; isFilterable = true; isPager = false;
        hasSubscriber = true;
        mAdapter = new MediaListAdapter(getContext());
        ((MediaListAdapter)mAdapter).setCurrentUser(userName);
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
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        mediaListOptions = getPresenter().getDatabase().getCurrentUser().getMediaListOptions();
        if (userId != 0)
            queryContainer.putVariable(KeyUtil.arg_userId, userId);
        else
            queryContainer.putVariable(KeyUtil.arg_userName, userName);

        queryContainer.putVariable(KeyUtil.arg_mediaType, mediaType)
                .putVariable(KeyUtil.arg_forceSingleCompletedList, true)
                .putVariable(KeyUtil.arg_scoreFormat, mediaListOptions.getScoreFormat());

        // since anilist doesn't support sorting by title we set a temporary sorting key
        if(!MediaListUtil.isTitleSort(getPresenter().getApplicationPref().getMediaListSort()))
            queryContainer.putVariable(KeyUtil.arg_sort, getPresenter().getApplicationPref().getMediaListSort() +
                            getPresenter().getApplicationPref().getSortOrder());
        else
            queryContainer.putVariable(KeyUtil.arg_sort, KeyUtil.MEDIA_ID + getPresenter().getApplicationPref().getSortOrder());

        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_LIST_COLLECTION_REQ, getContext());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(getPresenter() != null && isFilterable && GraphUtil.isKeyFilter(key)) {
            @KeyUtil.MediaListSort String mediaListSort = getPresenter().getApplicationPref().getMediaListSort();
            if(CompatUtil.equals(key, ApplicationPref._mediaListSort) && MediaListUtil.isTitleSort(mediaListSort)) {
                swipeRefreshLayout.setRefreshing(true);
                sortMediaListByTitle(mAdapter.getData());
            }
            else
                super.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

    @SuppressLint("SwitchIntDef")
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<MediaList> consumer) {
        if(consumer.getRequestMode() == KeyUtil.MUT_SAVE_MEDIA_LIST || consumer.getRequestMode() == KeyUtil.MUT_DELETE_MEDIA_LIST) {
            int pairIndex;
            if (getPresenter().isCurrentUser(userId, userName)) {
                Optional<IntPair<MediaList>> pairOptional = CompatUtil.findIndexOf(mAdapter.getData(), consumer.getChangeModel());
                if (pairOptional.isPresent()) {
                    switch (consumer.getRequestMode()) {
                        case KeyUtil.MUT_SAVE_MEDIA_LIST:
                            pairIndex = pairOptional.get().getFirst();
                            if (mediaListCollectionBase == null || CompatUtil.equals(mediaListCollectionBase.getStatus(), consumer.getChangeModel().getStatus()))
                                mAdapter.onItemChanged(consumer.getChangeModel(), pairIndex);
                            else
                                mAdapter.onItemRemoved(pairIndex);
                            break;
                        case KeyUtil.MUT_DELETE_MEDIA_LIST:
                            pairIndex = pairOptional.get().getFirst();
                            mAdapter.onItemRemoved(pairIndex);
                            break;
                    }
                } else if (mediaListCollectionBase == null || CompatUtil.equals(mediaListCollectionBase.getStatus(), consumer.getChangeModel().getStatus()))
                    onRefresh();
            }
        }
    }

    @Override
    public void onChanged(@Nullable PageContainer<MediaListCollection> content) {
        if(content != null) {
            if(content.hasPageInfo())
                getPresenter().setPageInfo(content.getPageInfo());
            if(!content.isEmpty()) {
                Optional<MediaListCollection> mediaOptional = Stream.of(content.getPageData()).findFirst();
                if(mediaOptional.isPresent()) {
                    MediaListCollection mediaListCollection = mediaOptional.get();
                    if(MediaListUtil.isTitleSort(getPresenter().getApplicationPref().getMediaListSort()))
                        sortMediaListByTitle(mediaListCollection.getEntries());
                    else
                        onPostProcessed(mediaListCollection.getEntries());
                    mediaListCollectionBase = mediaListCollection;
                } else
                    onPostProcessed(Collections.emptyList());
            }
            else
                onPostProcessed(Collections.emptyList());
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
    public void onItemClick(View target, IntPair<MediaList> data) {
        switch (target.getId()) {
            case R.id.container:
            case R.id.series_image:
                MediaBase mediaBase = data.getSecond().getMedia();
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                intent.putExtra(KeyUtil.arg_id, data.getSecond().getMediaId());
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
    public void onItemLongClick(View target, IntPair<MediaList> data) {
        switch (target.getId()) {
            case R.id.container:
            case R.id.series_image:
                if(getPresenter().getApplicationPref().isAuthenticated()) {
                    mediaActionUtil = new MediaActionUtil.Builder()
                            .setId(data.getSecond().getMediaId()).build(getActivity());
                    mediaActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    protected void sortMediaListByTitle(@NonNull List<MediaList> mediaLists) {
        @KeyUtil.SortOrderType String sortOrder = getPresenter().getApplicationPref().getSortOrder();
        mAdapter.onItemsInserted(Stream.of(mediaLists)
                .sorted((first, second) -> {
                    String firstTitle = MediaUtil.getMediaTitle(first.getMedia());
                    String secondTitle = MediaUtil.getMediaTitle(second.getMedia());
                    return CompatUtil.equals(sortOrder, KeyUtil.ASC) ?
                            firstTitle.compareTo(secondTitle) : secondTitle.compareTo(firstTitle);
                }).toList());
        updateUI();
    }
}