package com.mxt.anitrend.model.entity.container.attribute;

public class PageInfo {

    private int total;
    private int perPage;
    private int currentPage;
    private boolean hasNextPage;

    public int getTotal() {
        return total;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }
}
