package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Created by max on 10/4/2016.
 */
@Entity
public class UserBase implements Parcelable {

    @Id(assignable = true)
    private long id;
    @Index
    private String display_name;
    private String image_url_lge;
    private String image_url_med;

    public UserBase() {

    }

    protected UserBase(Parcel in) {
        id = in.readLong();
        display_name = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(display_name);
        dest.writeString(image_url_lge);
        dest.writeString(image_url_med);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserBase> CREATOR = new Creator<UserBase>() {
        @Override
        public UserBase createFromParcel(Parcel in) {
            return new UserBase(in);
        }

        @Override
        public UserBase[] newArray(int size) {
            return new UserBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_med() {
        return image_url_med;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setImage_url_lge(String image_url_lge) {
        this.image_url_lge = image_url_lge;
    }

    public void setImage_url_med(String image_url_med) {
        this.image_url_med = image_url_med;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserBase)
            return ((UserBase) obj).id == id;
        return super.equals(obj);
    }
}
