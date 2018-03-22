package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.mxt.anitrend.model.entity.base.CharacterBase;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class Character extends CharacterBase {

    private String description;
    private boolean isFavourite;

    protected Character(Parcel in) {
        super(in);
        description = in.readString();
        isFavourite = in.readByte() != 0;
    }

    public static final Creator<Character> CREATOR = new Creator<Character>() {
        @Override
        public Character createFromParcel(Parcel in) {
            return new Character(in);
        }

        @Override
        public Character[] newArray(int size) {
            return new Character[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(description);
        parcel.writeByte((byte) (isFavourite ? 1 : 0));
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean isFavourite() {
        return isFavourite;
    }
}
