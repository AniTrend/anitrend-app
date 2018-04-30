package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

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
     * Used to check if the newly applied preference key is a filter type that must refresh the application
     * TODO: 2018/03/25  Add preference keys that should trigger a refresh, such as sorting and ordering
     */
    public static boolean isKeyFilter(String preferenceKey) {
        return true;
    }
}
