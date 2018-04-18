package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;

import com.mxt.anitrend.model.entity.base.CharacterBase;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class MediaCharacter extends CharacterBase {

    private String description;

    protected MediaCharacter(Parcel in) {
        super(in);
        description = in.readString();
    }

    public static final Creator<MediaCharacter> CREATOR = new Creator<MediaCharacter>() {
        @Override
        public MediaCharacter createFromParcel(Parcel in) {
            return new MediaCharacter(in);
        }

        @Override
        public MediaCharacter[] newArray(int size) {
            return new MediaCharacter[size];
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
    }

    public String getDescription() {
        return description;
    }
}
