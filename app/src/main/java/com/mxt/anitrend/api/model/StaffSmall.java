package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class StaffSmall implements Parcelable {

    private int id;
    private String name_first;
    private String name_last;
    private String image_url_lge;
    private String image_url_med;
    private String language;
    private String role;

    protected StaffSmall(Parcel in) {
        id = in.readInt();
        name_first = in.readString();
        name_last = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
        language = in.readString();
        role = in.readString();
    }

    public static final Creator<StaffSmall> CREATOR = new Creator<StaffSmall>() {
        @Override
        public StaffSmall createFromParcel(Parcel in) {
            return new StaffSmall(in);
        }

        @Override
        public StaffSmall[] newArray(int size) {
            return new StaffSmall[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName_first() {
        return name_first;
    }

    public String getName_last() {
        return name_last;
    }

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_med() {
        return image_url_med;
    }

    public String getLanguage() {
        return language;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "StaffSmall{" +
                "id=" + id +
                ", name_first='" + name_first + "\n" +
                ", name_last='" + name_last + "\n" +
                ", image_url_lge='" + image_url_lge + "\n" +
                ", image_url_med='" + image_url_med + "\n" +
                ", language='" + language + "\n" +
                ", role='" + role + "\n" +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name_first);
        parcel.writeString(name_last);
        parcel.writeString(image_url_lge);
        parcel.writeString(image_url_med);
        parcel.writeString(language);
        parcel.writeString(role);
    }
}
