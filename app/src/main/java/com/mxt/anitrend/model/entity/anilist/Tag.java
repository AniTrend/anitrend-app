package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Maxwell on 10/4/2016.
 "id": 172,
 "name": "Superhero",
 "description": "A hero with super powers.",
 "spoiler": false,
 "adult": false,
 "demographic": false,
 "denied": 0,
 "category": "Theme-Fantasy",
 "votes": 60,
 "series_spoiler": false
 */
@Entity
public class Tag implements Parcelable {

    @Id(assignable = true)
    private long id;
    private String name;
    private String description;
    private boolean spoiler;
    private boolean adult;
    private boolean demographic;
    private int denied;
    private String category;
    private int votes;
    private boolean series_spoiler;

    public Tag() {

    }

    protected Tag(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        spoiler = in.readByte() != 0;
        adult = in.readByte() != 0;
        demographic = in.readByte() != 0;
        denied = in.readInt();
        category = in.readString();
        votes = in.readInt();
        series_spoiler = in.readByte() != 0;
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public void setDemographic(boolean demographic) {
        this.demographic = demographic;
    }

    public void setDenied(int denied) {
        this.denied = denied;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setSeries_spoiler(boolean series_spoiler) {
        this.series_spoiler = series_spoiler;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    public boolean isAdult() {
        return adult;
    }

    public boolean isDemographic() {
        return demographic;
    }

    public int getDenied() {
        return denied;
    }

    public String getCategory() {
        return category;
    }

    public int getVotes() {
        return votes;
    }

    public boolean isSeries_spoiler() {
        return series_spoiler;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeByte((byte) (spoiler ? 1 : 0));
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeByte((byte) (demographic ? 1 : 0));
        parcel.writeInt(denied);
        parcel.writeString(category);
        parcel.writeInt(votes);
        parcel.writeByte((byte) (series_spoiler ? 1 : 0));
    }
}
