package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by Max on 10/4/2016.
 */
public class Review implements Parcelable {

    private long id;
    private String summary;
    private String mediaType;
    private String body;
    private int rating;
    private int ratingAmount;
    private String userRating;
    private int score;
    @SerializedName("private")
    private boolean isPrivate;
    private long createdAt;
    private UserBase user;
    private MediaBase media;

    protected Review(Parcel in) {
        id = in.readLong();
        summary = in.readString();
        mediaType = in.readString();
        body = in.readString();
        rating = in.readInt();
        ratingAmount = in.readInt();
        userRating = in.readString();
        score = in.readInt();
        isPrivate = in.readByte() != 0;
        createdAt = in.readLong();
        user = in.readParcelable(UserBase.class.getClassLoader());
        media = in.readParcelable(MediaBase.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(summary);
        dest.writeString(mediaType);
        dest.writeString(body);
        dest.writeInt(rating);
        dest.writeInt(ratingAmount);
        dest.writeString(userRating);
        dest.writeInt(score);
        dest.writeByte((byte) (isPrivate ? 1 : 0));
        dest.writeLong(createdAt);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(media, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getBody() {
        return body;
    }

    public int getRating() {
        return rating;
    }

    public int getRatingAmount() {
        return ratingAmount;
    }

    public String getUserRating() {
        return userRating;
    }

    public int getScore() {
        return score;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UserBase getUser() {
        return user;
    }

    public MediaBase getMedia() {
        return media;
    }
}
