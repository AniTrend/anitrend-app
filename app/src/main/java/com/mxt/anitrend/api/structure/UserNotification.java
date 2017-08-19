package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.api.model.SeriesSmall;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.util.DateTimeConverter;

import java.io.Serializable;

/**
 * Created by Maxwell on 1/9/2017.
 */

public class UserNotification implements Parcelable {

    private int id;
    private int user_id;
    private int invoker_id;
    private int object_id;
    private int object_type;
    private String meta_data;
    @SerializedName("value")
    private Object meta_value;
    private String created_at;
    private UserSmall user;
    private Comment comment;
    private SeriesSmall series;

    protected UserNotification(Parcel in) {
        id = in.readInt();
        user_id = in.readInt();
        invoker_id = in.readInt();
        object_id = in.readInt();
        object_type = in.readInt();
        meta_data = in.readString();
        meta_value = in.readSerializable();
        created_at = in.readString();
        user = in.readParcelable(UserSmall.class.getClassLoader());
        comment = in.readParcelable(Comment.class.getClassLoader());
        series = in.readParcelable(SeriesSmall.class.getClassLoader());
    }

    public static final Creator<UserNotification> CREATOR = new Creator<UserNotification>() {
        @Override
        public UserNotification createFromParcel(Parcel in) {
            return new UserNotification(in);
        }

        @Override
        public UserNotification[] newArray(int size) {
            return new UserNotification[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getInvoker_id() {
        return invoker_id;
    }

    public int getObject_id() {
        return object_id;
    }

    public int getObject_type() {
        return object_type;
    }

    public String getMeta_data() {
        return meta_data;
    }

    public Object getMeta_value() {
        return meta_value;
    }

    public String getCreated_at() {
        //JSP time zone
        return DateTimeConverter.getPrettyDateCustom(created_at);
    }

    public UserSmall getUser() {
        return user;
    }

    public Comment getComment() {
        return comment;
    }

    public SeriesSmall getSeries() {
        return series;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(user_id);
        parcel.writeInt(invoker_id);
        parcel.writeInt(object_id);
        parcel.writeInt(object_type);
        parcel.writeString(meta_data);
        parcel.writeSerializable((Serializable) meta_value);
        parcel.writeString(created_at);
        parcel.writeParcelable(user, i);
        parcel.writeParcelable(comment, i);
        parcel.writeParcelable(series, i);
    }
}
