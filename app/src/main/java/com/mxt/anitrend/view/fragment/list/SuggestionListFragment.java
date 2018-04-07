package com.mxt.anitrend.view.fragment.list;

import android.os.Bundle;

import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/11/04.
 * Suggestions adapter
 * // TODO: 2018/04/07 Hide genres from filter list
 */

public class SuggestionListFragment extends MediaBrowseFragment {

    public static SuggestionListFragment newInstance(Bundle bundle) {
        return (SuggestionListFragment) newInstance(bundle, GraphUtil.getDefaultQuery(true)
                .putVariable(KeyUtil.arg_mediaType, KeyUtil.ANIME).putVariable(KeyUtil.arg_onList, false),
                false);
    }

    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        queryContainer.putVariable(KeyUtil.arg_genres, getPresenter().getTopFavouriteGenres())
                .putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_BROWSE_REQ, getContext());
    }
}
