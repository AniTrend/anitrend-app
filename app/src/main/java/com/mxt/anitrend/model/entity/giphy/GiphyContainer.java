package com.mxt.anitrend.model.entity.giphy;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by max on 2017/12/09.
 * container for giphy api responses
 */

public class GiphyContainer implements Parcelable {

    private List<Giphy> data;
    private Pagination pagination;
    private Meta meta;

    protected GiphyContainer(Parcel in) {
        data = in.createTypedArrayList(Giphy.CREATOR);
        pagination = in.readParcelable(Pagination.class.getClassLoader());
        meta = in.readParcelable(Meta.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(data);
        dest.writeParcelable(pagination, flags);
        dest.writeParcelable(meta, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GiphyContainer> CREATOR = new Creator<GiphyContainer>() {
        @Override
        public GiphyContainer createFromParcel(Parcel in) {
            return new GiphyContainer(in);
        }

        @Override
        public GiphyContainer[] newArray(int size) {
            return new GiphyContainer[size];
        }
    };

    public List<Giphy> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Meta getMeta() {
        return meta;
    }
}
