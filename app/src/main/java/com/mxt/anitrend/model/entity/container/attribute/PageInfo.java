package com.mxt.anitrend.model.entity.container.attribute;

import android.os.Parcel;
import android.os.Parcelable;

public class PageInfo implements Parcelable {

    private int total;
    private int perPage;
    private int currentPage;
    private boolean hasNextPage;

    protected PageInfo(Parcel in) {
        total = in.readInt();
        perPage = in.readInt();
        currentPage = in.readInt();
        hasNextPage = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(total);
        dest.writeInt(perPage);
        dest.writeInt(currentPage);
        dest.writeByte((byte) (hasNextPage ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PageInfo> CREATOR = new Creator<PageInfo>() {
        @Override
        public PageInfo createFromParcel(Parcel in) {
            return new PageInfo(in);
        }

        @Override
        public PageInfo[] newArray(int size) {
            return new PageInfo[size];
        }
    };

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
