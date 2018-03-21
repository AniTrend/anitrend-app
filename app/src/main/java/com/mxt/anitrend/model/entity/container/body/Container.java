package com.mxt.anitrend.model.entity.container.body;

import com.mxt.anitrend.model.entity.container.attribute.PageInfo;

abstract class Container {

    private PageInfo pageInfo;

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public boolean hasPageInfo() {
        return pageInfo != null;
    }

    public abstract boolean isEmpty();
}
