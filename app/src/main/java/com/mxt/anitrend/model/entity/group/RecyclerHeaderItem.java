package com.mxt.anitrend.model.entity.group;

import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2018/02/18.
 */

public class RecyclerHeaderItem extends RecyclerItem {

    private String title;
    private int size;
    private boolean capitalize;

    public RecyclerHeaderItem(String title, int size, boolean capitalize) {
        this.title = title;
        this.size = size;
        this.capitalize = capitalize;
        this.setContentType(KeyUtil.RECYCLER_TYPE_HEADER);
    }

    public RecyclerHeaderItem(String title, int size) {
        this(title, size, true);
    }

    public RecyclerHeaderItem(String title) {
        this(title, 0);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getTitle() {
        return capitalize ? CompatUtil.INSTANCE.capitalizeWords(title) : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof RecyclerHeaderItem)
            return CompatUtil.INSTANCE.equals(((RecyclerHeaderItem) o).title, title);
        return super.equals(o);
    }
}
