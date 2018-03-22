package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

/**
 * Created by max on 2018/03/22.
 * Graph request helper class
 */

public class GraphParameterUtil {

    public static GraphQueryContainer getDefaultQueryContainer(boolean includePaging) {
        GraphQueryContainer queryContainer = new GraphQueryContainer();
        if(includePaging)
            queryContainer.setVariable(KeyUtils.arg_per_page, KeyUtils.PAGING_LIMIT);
        return queryContainer;
    }
}
