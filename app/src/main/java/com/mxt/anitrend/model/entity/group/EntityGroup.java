package com.mxt.anitrend.model.entity.group;

import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2018/02/18.
 */

public abstract class EntityGroup {

    private @KeyUtils.RecyclerViewType
    int content_type;

    public @KeyUtils.RecyclerViewType
    int getContentType() {
        return content_type;
    }

    public void setContentType(@KeyUtils.RecyclerViewType int type) {
        this.content_type = type;
    }
}
