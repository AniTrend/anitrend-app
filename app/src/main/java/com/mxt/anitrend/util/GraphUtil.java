package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */

public class GraphUtil {

    public static QueryContainerBuilder getDefaultQuery(boolean includePaging) {
        QueryContainerBuilder queryContainer = new QueryContainerBuilder();
        if(includePaging)
            queryContainer.putVariable(KeyUtils.arg_page_limit, KeyUtils.PAGING_LIMIT);
        return queryContainer;
    }
}
