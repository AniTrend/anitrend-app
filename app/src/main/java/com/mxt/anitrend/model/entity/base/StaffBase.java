package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mxt.anitrend.model.entity.group.EntityGroup;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class StaffBase extends EntityGroup implements Parcelable {

    private long id;
    private String name_first;
    private String name_last;
    private String image_url_lge;
    private String image_url_med;
    private String language;
    private String role;

    protected StaffBase(Parcel in) {
        id = in.readLong();
        name_first = in.readString();
        name_last = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
        language = in.readString();
        role = in.readString();
    }

    public static final Creator<StaffBase> CREATOR = new Creator<StaffBase>() {
        @Override
        public StaffBase createFromParcel(Parcel in) {
            return new StaffBase(in);
        }

        @Override
        public StaffBase[] newArray(int size) {
            return new StaffBase[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getName_first() {
        return name_first;
    }

    public String getFullName() {
        if(TextUtils.isEmpty(name_last))
            return name_first;
        return String.format("%s %s", name_first, name_last);
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
        return getFullName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StaffBase)
            return ((StaffBase) obj).getId() == id;
        return super.equals(obj);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name_first);
        parcel.writeString(name_last);
        parcel.writeString(image_url_lge);
        parcel.writeString(image_url_med);
        parcel.writeString(language);
        parcel.writeString(role);
    }
}
