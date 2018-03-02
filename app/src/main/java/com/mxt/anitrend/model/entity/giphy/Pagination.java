package com.mxt.anitrend.model.entity.giphy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2017/12/09.
 * giphy pagination data
 */

public class Pagination implements Parcelable {

    private int total_count;
    private int count;
    private int offset;

    protected Pagination(Parcel in) {
        total_count = in.readInt();
        count = in.readInt();
        offset = in.readInt();
    }

    public static final Creator<Pagination> CREATOR = new Creator<Pagination>() {
        @Override
        public Pagination createFromParcel(Parcel in) {
            return new Pagination(in);
        }

        @Override
        public Pagination[] newArray(int size) {
            return new Pagination[size];
        }
    };

    /**
     * @return total items in the request scope
     */
    public int getTotal_count() {
        return total_count;
    }

    /**
     * @return current pagination limit set in the request query
     * @see com.mxt.anitrend.model.api.retro.base.GiphyModel
     */
    public int getCount() {
        return count;
    }

    /**
     * @return current pagination offset value
     */
    public int getOffset() {
        return offset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(total_count);
        parcel.writeInt(count);
        parcel.writeInt(offset);
    }
}
