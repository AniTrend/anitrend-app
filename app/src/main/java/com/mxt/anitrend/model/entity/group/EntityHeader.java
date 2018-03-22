package com.mxt.anitrend.model.entity.group;

import com.mxt.anitrend.util.KeyUtils;

import java.util.Objects;

/**
 * Created by max on 2018/02/18.
 */

public class EntityHeader extends EntityGroup {

    private String title;
    private int size;

    public EntityHeader(String title, int size) {
        this.title = title;
        this.size = size;
        setContentType(KeyUtils.RECYCLER_TYPE_HEADER);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof EntityHeader)
            return Objects.equals(((EntityHeader) o).getTitle(), title);
        return super.equals(o);
    }
}
