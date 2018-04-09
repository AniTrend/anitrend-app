package com.mxt.anitrend.model.entity.group;

import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2018/02/18.
 */

public class EntityHeader extends EntityGroup {

    private String title;
    private int size;

    public EntityHeader(String title, int size) {
        this.title = title;
        this.size = size;
        this.setContentType(KeyUtil.RECYCLER_TYPE_HEADER);
    }

    public EntityHeader(String title) {
        this(title, 0);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getTitle() {
        return CompatUtil.capitalizeWords(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof EntityHeader)
            return CompatUtil.equals(((EntityHeader) o).title, title);
        return super.equals(o);
    }
}
