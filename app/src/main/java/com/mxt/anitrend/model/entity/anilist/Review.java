package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by Max on 10/4/2016.
 */
public class Review implements Parcelable {

    private int id;
    private String date;
    private int rating;
    private int rating_amount;
    private String summary;
    @SerializedName("private")
    private int review_private;
    private int user_rating;
    private String text;
    private int score;
    private SeriesBase anime;
    private SeriesBase manga;
    private UserBase user;

    protected Review(Parcel in) {
        id = in.readInt();
        date = in.readString();
        rating = in.readInt();
        rating_amount = in.readInt();
        summary = in.readString();
        review_private = in.readInt();
        user_rating = in.readInt();
        text = in.readString();
        score = in.readInt();
        anime = in.readParcelable(SeriesBase.class.getClassLoader());
        manga = in.readParcelable(SeriesBase.class.getClassLoader());
        user = in.readParcelable(UserBase.class.getClassLoader());
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

    public int getId() {
        return id;
    }

    public String getDate() {
        return DateUtil.convertDateString(date);
    }

    /**
    * @return Positive user ratings of review
    */
    public int getRating() {
        return rating;
    }

    /**
     * @return All user rating of review
     */
    public int getRating_amount() {
        return rating_amount;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isReview_private() {
        return review_private == 1;
    }

    /**
     * Current user rating of review
     *
     * @return 0 - no rating <br> 1 up or positive rating <br> 2 down or negative rating
     */
    public @KeyUtils.ReviewStatus int getUser_rating() {
        return user_rating;
    }

    public String getText() {
        return text;
    }

    public int getScore() {
        return score;
    }

    public float getDoubleScore() {
        return score * 5 / 100;
    }

    public SeriesBase getAnime() {
        return anime;
    }

    public UserBase getUser() {
        return user;
    }

    public SeriesBase getManga() {
        return manga;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setRating_amount(int rating_amount) {
        this.rating_amount = rating_amount;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setReview_private(int review_private) {
        this.review_private = review_private;
    }

    public void setUser_rating(int user_rating) {
        this.user_rating = user_rating;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setAnime(SeriesBase anime) {
        this.anime = anime;
    }

    public void setManga(SeriesBase manga) {
        this.manga = manga;
    }

    public void setUser(UserBase user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", date='" + date + "\n" +
                ", rating=" + rating +
                ", rating_amount=" + rating_amount +
                ", summary='" + summary + "\n" +
                ", review_private=" + review_private +
                ", user_rating=" + user_rating +
                ", text='" + text + "\n" +
                ", score=" + score +
                ", anime=" + anime +
                ", manga=" + manga +
                ", user=" + user +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(date);
        parcel.writeInt(rating);
        parcel.writeInt(rating_amount);
        parcel.writeString(summary);
        parcel.writeInt(review_private);
        parcel.writeInt(user_rating);
        parcel.writeString(text);
        parcel.writeInt(score);
        parcel.writeParcelable(anime, i);
        parcel.writeParcelable(manga, i);
        parcel.writeParcelable(user, i);
    }
}
