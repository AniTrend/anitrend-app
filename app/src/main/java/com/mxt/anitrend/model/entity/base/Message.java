package com.mxt.anitrend.model.entity.base;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.BuildConfig;

/**
 * Created by max on 2017/11/01.
 */

public final class Message implements Parcelable {

    public Uri uri;

    public Message(Uri uri) {
        this.uri = uri;
    }

    protected Message(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
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
