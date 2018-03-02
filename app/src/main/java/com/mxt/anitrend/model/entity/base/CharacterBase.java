package com.mxt.anitrend.model.entity.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mxt.anitrend.model.entity.group.EntityGroup;

import java.util.List;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class CharacterBase extends EntityGroup implements Parcelable {

    private long id;
    private String name_first;
    private String name_last;
    private String image_url_lge;
    private String image_url_med;
    private String role;
    private List<StaffBase> actor;

    protected CharacterBase(Parcel in) {
        id = in.readLong();
        name_first = in.readString();
        name_last = in.readString();
        image_url_lge = in.readString();
        image_url_med = in.readString();
        role = in.readString();
        actor = in.createTypedArrayList(StaffBase.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
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

    public static final Creator<CharacterBase> CREATOR = new Creator<CharacterBase>() {
        @Override
        public CharacterBase createFromParcel(Parcel in) {
            return new CharacterBase(in);
        }

        @Override
        public CharacterBase[] newArray(int size) {
            return new CharacterBase[size];
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

    public List<StaffBase> getActor() {
        return actor;
    }

    public String getFirstActor() {
        if(actor != null && !actor.isEmpty())
            return actor.get(0).getFullName();
        return null;
    }

    public void setId(long id) {
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

    public void setActor(List<StaffBase> actor) {
        this.actor = actor;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CharacterBase)
            return ((CharacterBase)obj).getId() == id;
        return super.equals(obj);
    }
}
