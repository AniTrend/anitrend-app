package com.mxt.anitrend.model.entity.giphy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 2017/12/09.
 * giphy request meta data
 */

public class Meta implements Parcelable {

    private int status;
    private String msg;
    private String response_id;

    protected Meta(Parcel in) {
        status = in.readInt();
        msg = in.readString();
        response_id = in.readString();
    }

    public static final Creator<Meta> CREATOR = new Creator<Meta>() {
        @Override
        public Meta createFromParcel(Parcel in) {
            return new Meta(in);
        }

        @Override
        public Meta[] newArray(int size) {
            return new Meta[size];
        }
    };

    /**
     * @return http status code for the request;
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return status message for giphy request
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @return response id
     */
    public String getResponse_id() {
        return response_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(status);
        parcel.writeString(msg);
        parcel.writeString(response_id);
    }
}
