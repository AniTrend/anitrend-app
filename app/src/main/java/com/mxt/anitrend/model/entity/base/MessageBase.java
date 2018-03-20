package com.mxt.anitrend.model.entity.base;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.BuildConfig;

/**
 * Created by max on 2017/11/01.
 */

public final class MessageBase implements Parcelable {

    public Uri uri;

    public MessageBase(Uri uri) {
        this.uri = uri;
    }

    protected MessageBase(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<MessageBase> CREATOR = new Creator<MessageBase>() {
        @Override
        public MessageBase createFromParcel(Parcel in) {
            return new MessageBase(in);
        }

        @Override
        public MessageBase[] newArray(int size) {
            return new MessageBase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(uri, i);
    }

    public boolean isValid() {
        return (uri != null && uri.toString().startsWith(BuildConfig.REDIRECT_URI));
    }

    public String getQueryParam(String query) {
        return uri.getQueryParameter(query);
    }
}
