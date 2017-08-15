package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class CharacterSmall implements Parcelable {

    private int id;
    private String name_first;
    private String name_last;
    private String image_url_lge;
    private String image_url_med;
    private String role;
    private List<StaffSmall> actor;

    public CharacterSmall() {
    }

    protected CharacterSmall(Parcel in) {
        id = in.readInt();
        name_first = in.readString();
        name_last = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
        role = in.readString();
        actor = in.createTypedArrayList(StaffSmall.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name_first);
        dest.writeString(name_last);
        dest.writeString(image_url_lge);
        dest.writeString(image_url_med);
        dest.writeString(role);
        dest.writeTypedList(actor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CharacterSmall> CREATOR = new Creator<CharacterSmall>() {
        @Override
        public CharacterSmall createFromParcel(Parcel in) {
            return new CharacterSmall(in);
        }

        @Override
        public CharacterSmall[] newArray(int size) {
            return new CharacterSmall[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName_first() {
        return name_first;
    }

    public String getName_last() {
        return name_last == null? "":name_last;
    }

    public String getImage_url_lge() {
        return image_url_lge;
    }

    public String getImage_url_med() {
        return image_url_med;
    }

    public String getRole() {
        return role;
    }

    public List<StaffSmall> getActor() {
        return actor;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName_first(String name_first) {
        this.name_first = name_first;
    }

    public void setName_last(String name_last) {
        this.name_last = name_last;
    }

    public void setImage_url_lge(String image_url_lge) {
        this.image_url_lge = image_url_lge;
    }

    public void setImage_url_med(String image_url_med) {
        this.image_url_med = image_url_med;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setActor(List<StaffSmall> actor) {
        this.actor = actor;
    }
}
