package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;


@Entity
public class MediaTag implements Parcelable {

    @Id(assignable = true)
    private long id;
    private String name;
    private String description;
    private String category;
    private int rank;
    private boolean isGeneralSpoiler;
    private boolean isMediaSpoiler;
    private boolean isAdult;

    public MediaTag() {

    }

    protected MediaTag(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        category = in.readString();
        rank = in.readInt();
        isGeneralSpoiler = in.readByte() != 0;
        isMediaSpoiler = in.readByte() != 0;
        isAdult = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeInt(rank);
        dest.writeByte((byte) (isGeneralSpoiler ? 1 : 0));
        dest.writeByte((byte) (isMediaSpoiler ? 1 : 0));
        dest.writeByte((byte) (isAdult ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaTag> CREATOR = new Creator<MediaTag>() {
        @Override
        public MediaTag createFromParcel(Parcel in) {
            return new MediaTag(in);
        }

        @Override
        public MediaTag[] newArray(int size) {
            return new MediaTag[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getRank() {
        return rank;
    }

    public boolean isGeneralSpoiler() {
        return isGeneralSpoiler;
    }

    public boolean isMediaSpoiler() {
        return isMediaSpoiler;
    }

    public boolean isAdult() {
        return isAdult;
    }
}
