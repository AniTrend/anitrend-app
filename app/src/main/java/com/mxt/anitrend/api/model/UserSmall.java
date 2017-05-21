package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Max on 10/4/2016.
 */
public class UserSmall implements Parcelable {

    private int id;
    private String display_name;
    private String image_url_lge;
    private String image_url_med;

    protected UserSmall(Parcel in) {
        id = in.readInt();
        display_name = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
    }

    public static final Creator<UserSmall> CREATOR = new Creator<UserSmall>() {
        @Override
        public UserSmall createFromParcel(Parcel in) {
            return new UserSmall(in);
        }

        @Override
        public UserSmall[] newArray(int size) {
            return new UserSmall[size];
        }
    };

    public int getId() {
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

    @Override
    public String toString() {
        return "UserSmall{" +
                "id=" + id +
                ", display_name='" + display_name + "\n" +
                ", image_url_lge='" + image_url_lge + "\n" +
                ", image_url_med='" + image_url_med + "\n" +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(display_name);
        parcel.writeString(image_url_lge);
        parcel.writeString(image_url_med);
    }


    /**
     * Reserved for equals purposes
     */
    public UserSmall(int id, String display_name, String image_url_med, String image_url_lge) {
        this.id = id;
        this.display_name = display_name;
        this.image_url_med = image_url_med;
        this.image_url_lge = image_url_lge;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserSmall){
            UserSmall temp = (UserSmall) obj;
            return temp.id == id;
        }
        return false;
    }
}
