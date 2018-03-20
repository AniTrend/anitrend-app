package com.mxt.anitrend.view.fragment.detail;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesListAdapter;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.fragment.FragmentUserListBase;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.general.MediaList;
import com.mxt.anitrend.presenter.activity.ProfilePresenter;
import com.mxt.anitrend.util.ComparatorProvider;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.FilterProvider;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.detail.SeriesActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Map;

/**
 * Created by max on 2017/12/18.
 * series list fragment
 */

public class SeriesListFragment extends FragmentUserListBase implements BaseConsumer.onRequestModelChange<MediaList> {

    private String userName;
    private int contentIndex;
    private @KeyUtils.SeriesType int seriesType;

    public static FragmentUserListBase newInstance(Bundle params, int position) {
        Bundle args = new Bundle(params);
        args.putInt(KeyUtils.arg_series_show_type, position);
        FragmentUserListBase fragment = new SeriesListFragment();
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
            userName = getArguments().getString(KeyUtils.arg_user_name);
            seriesType = getArguments().getInt(KeyUtils.arg_series_type);
            contentIndex = getArguments().getInt(KeyUtils.arg_series_show_type);
        }
        mColumnSize = R.integer.grid_list_x2; isFilterable = true;
        setPresenter(new ProfilePresenter(getContext()));
        setViewModel(true);
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    @Override
    protected void updateUI() {
        if(mAdapter == null) {
            mAdapter = new SeriesListAdapter(model, getContext());
            ((SeriesListAdapter)mAdapter).setCurrentUser(userName);
        }
        if(model != null && model.size() > 0)
            if(getPresenter().isCurrentUser(userName))
                getPresenter().getDatabase().saveSeries(model);
        injectAdapter();
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        Bundle params = getViewModel().getParams();
        params.putString(KeyUtils.arg_user_name, userName);
        getViewModel().requestData(seriesType == KeyUtils.ANIME? KeyUtils.USER_ANIME_LIST_REQ : KeyUtils.USER_MANGA_LIST_REQ, getContext());
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater menu inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_share).setVisible(false);
        menu.findItem(R.id.action_genre).setVisible(false);
        menu.findItem(R.id.action_type).setVisible(false);
        menu.findItem(R.id.action_year).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);
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
                Intent intent = new Intent(getActivity(), SeriesActivity.class);
                intent.putExtra(KeyUtils.arg_id, data.getSeries_id());
                intent.putExtra(KeyUtils.arg_series_type, KeyUtils.SeriesTypes[seriesType]);
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
                    seriesActionUtil = new SeriesActionUtil.Builder()
                            .setModel(data).build(getActivity());
                    seriesActionUtil.startSeriesAction();
                } else
                    NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(model != null) {
            model = sortedFilteredList(model);
            mAdapter.onItemsInserted(model);
        }
    }

    /**
     * Called when the model state is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable User content) {
        if(content != null) {
            if(content.getLists() != null) {
                if(isPager) {
                    if (model == null) model = getList(content.getLists());
                    else model.addAll(getList(content.getLists()));
                }
                else
                    model = getList(content.getLists());
                updateUI();
            } else {
                if (isPager)
                    setLimitReached();
                if (model == null || model.size() < 1)
                    showEmpty(getString(R.string.layout_empty_response));
            }
        } else
            showEmpty(getString(R.string.layout_empty_response));
    }

    private List<MediaList> getList(Map<String, List<MediaList>> seriesMap) {
        List<MediaList> mediaList = seriesMap.get(getListType());
        List<MediaList> filtered = FilterProvider.getListItemFilter(getPresenter().isCurrentUser(userName), mediaList);
        if(filtered != null)
            return sortedFilteredList(filtered);
        return mediaList;
    }

    private List<MediaList> sortedFilteredList(List<MediaList> mediaList) {
        User user = getPresenter().getDatabase().getCurrentUser();
        List<MediaList> mediaListFiltered;
        if(seriesType == KeyUtils.ANIME)
            mediaListFiltered = Stream.of(mediaList).sorted(ComparatorProvider.getAnimeComparator(getPresenter().getApplicationPref() , user.getTitle_language())).toList();
        else
            mediaListFiltered = Stream.of(mediaList).sorted(ComparatorProvider.getMangaComparator(getPresenter().getApplicationPref(), user.getTitle_language())).toList();
        return mediaListFiltered;
    }

    private String getListType() {
        return seriesType == KeyUtils.ANIME? KeyUtils.AnimeListKeys[contentIndex] : KeyUtils.MangaListKeys[contentIndex];
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<MediaList> consumer) {
        if(getPresenter().isCurrentUser(userName)) {
            int pairIndex;
            Optional<IntPair<MediaList>> pairOptional = CompatUtil.findIndexOf(model, consumer.getChangeModel());
            if(consumer.getRequestMode() == KeyUtils.ANIME_LIST_EDIT_REQ || consumer.getRequestMode() == KeyUtils.MANGA_LIST_EDIT_REQ) {
                if (pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.set(pairIndex, consumer.getChangeModel());
                    mAdapter.onItemChanged(consumer.getChangeModel(),pairIndex);
                }
            } else if(consumer.getRequestMode() == KeyUtils.ANIME_LIST_DELETE_REQ || consumer.getRequestMode() == KeyUtils.MANGA_LIST_DELETE_REQ) {
                if (pairOptional.isPresent()) {
                    pairIndex = pairOptional.get().getFirst();
                    model.remove(pairIndex);
                    mAdapter.onItemRemoved(pairIndex);
                }
            }
        }
    }
}