package com.mxt.anitrend.model.entity.group;

import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2018/02/18.
 * EntityGroup for grouping items in a recycler view
 */

public abstract class EntityGroup {

    private @KeyUtil.RecyclerViewType int content_type;

    private String subGroupTitle;

    public @KeyUtil.RecyclerViewType int getContentType() {
        return content_type;
    }

    public void setContentType(@KeyUtil.RecyclerViewType int type) {
        this.content_type = type;
    }

    public String getSubGroupTitle() {
        return subGroupTitle;
    }

    public void setSubGroupTitle(String subGroupTitle) {
        this.subGroupTitle = subGroupTitle;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
