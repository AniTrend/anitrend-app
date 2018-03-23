package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.container.request.QueryContainer;

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */

public class GraphUtil {

    public static QueryContainer getDefaultQuery(boolean includePaging) {
        QueryContainer queryContainer = new QueryContainer();
        if(includePaging)
            queryContainer.setVariable(KeyUtils.arg_page_limit, KeyUtils.PAGING_LIMIT);
        return queryContainer;
    }
}
