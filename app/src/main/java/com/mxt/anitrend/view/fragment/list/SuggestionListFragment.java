package com.mxt.anitrend.view.fragment.list;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.mxt.anitrend.R;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/11/04.
 * Suggestions adapter
 */
// TODO: 2018/04/07 Hide genres from filter list
public class SuggestionListFragment extends MediaBrowseFragment {

    public static SuggestionListFragment newInstance(Bundle params) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(true)
                .putVariable(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                .putVariable(KeyUtil.arg_onList, false));
        SuggestionListFragment fragment = new SuggestionListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void makeRequest() {
        ApplicationPref pref = getPresenter().getApplicationPref();
        Bundle bundle = getViewModel().getParams();
        queryContainer.putVariable(KeyUtil.arg_tagsInclude, getPresenter().getTopFavouriteTags(6))
                .putVariable(KeyUtil.arg_genresInclude, getPresenter().getTopFavouriteGenres(4))
                .putVariable(KeyUtil.arg_sort, pref.getMediaSort() + pref.getSortOrder())
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_BROWSE_REQ, getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_genre).setVisible(false);
        menu.findItem(R.id.action_type).setVisible(false);
        menu.findItem(R.id.action_year).setVisible(false);
        menu.findItem(R.id.action_status).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getContext() != null)
            switch (item.getItemId()) {
                case R.id.action_sort:
                    DialogUtil.createSelection(getContext(), R.string.app_filter_sort, CompatUtil.getIndexOf(KeyUtil.MediaSortType,
                            getPresenter().getApplicationPref().getMediaSort()), CompatUtil.capitalizeWords(KeyUtil.MediaSortType),
                            (dialog, which) -> {
                                if(which == DialogAction.POSITIVE)
                                    getPresenter().getApplicationPref().setMediaSort(KeyUtil.MediaSortType[dialog.getSelectedIndex()]);
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
}
