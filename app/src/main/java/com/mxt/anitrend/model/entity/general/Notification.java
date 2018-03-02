package com.mxt.anitrend.model.entity.general;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.base.UserBase;

import java.io.Serializable;

/**
 * Created by Maxwell on 1/9/2017.
 */

public class Notification extends NotificationBase implements Parcelable {

    @SerializedName("value")
    private Object meta_value;
    private UserBase user;
    private Comment comment;
    private SeriesBase series;


    protected Notification(Parcel in) {
        super(in);
        meta_value = in.readSerializable();
        user = in.readParcelable(UserBase.class.getClassLoader());
        comment = in.readParcelable(Comment.class.getClassLoader());
        series = in.readParcelable(SeriesBase.class.getClassLoader());
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public UserBase getUser() {
        return user;
    }

    public Comment getComment() {
        return comment;
    }

    public SeriesBase getSeries() {
        return series;
    }

    public Object getMeta_value() {
        return meta_value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeSerializable((Serializable) meta_value);
        parcel.writeParcelable(user, i);
        parcel.writeParcelable(comment, i);
        parcel.writeParcelable(series, i);
    }
}
