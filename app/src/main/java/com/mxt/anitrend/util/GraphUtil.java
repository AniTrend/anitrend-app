package com.mxt.anitrend.util;

import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.base.BasePresenter;

import java.util.List;

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */

public class GraphUtil {

    /**
     * Builder provider helper method, that provides a default GraphQL Query and Variable Builder
     * @param includePaging if one of the variables should container a page limit
     */
    public static QueryContainerBuilder getDefaultQuery(boolean includePaging) {
        QueryContainerBuilder queryContainer = new QueryContainerBuilder();
        if(includePaging)
            queryContainer.putVariable(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT);
        return queryContainer;
    }

    /**
     * Constructs an array from multiple arguments, usually for soring of items
     */
    public static String[] getMultipleParams(String... params) {
        return params;
    }


    /**
     * Used to check if the newly applied preference key is a should trigger an application refresh
     */
    public static boolean isKeyFilter(String preferenceKey) {
        return !CompatUtil.equals(preferenceKey, ApplicationPref._isLightTheme) &&
               !CompatUtil.equals(preferenceKey, ApplicationPref._updateChannel);
    }

    /**
     * Remove empty json object responses, to resolve undefined content errors
     */
    public static List<FeedList> filterFeedList(BasePresenter presenter, List<FeedList> feedLists) {
        List<FeedList> filteredList = Stream.of(feedLists)
                .filter(f -> f != null && !TextUtils.isEmpty(f.getType()))
                .toList();
        if(presenter.getPageInfo() != null)
            presenter.getPageInfo().setPerPage(filteredList.size());
        return filteredList;
    }
}
