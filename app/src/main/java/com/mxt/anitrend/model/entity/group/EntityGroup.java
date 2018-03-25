package com.mxt.anitrend.model.entity.group;

import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2018/02/18.
 * EntityGroup for grouping items in a recycler view
 */

public abstract class EntityGroup {

    private @KeyUtils.RecyclerViewType int content_type;

    private String subGroupTitle;

    public @KeyUtils.RecyclerViewType int getContentType() {
        return content_type;
    }

    public void setContentType(@KeyUtils.RecyclerViewType int type) {
        this.content_type = type;
    }

    public String getSubGroupTitle() {
        return subGroupTitle;
    }

    public void setSubGroupTitle(String subGroupTitle) {
        this.subGroupTitle = subGroupTitle;
    }
}
