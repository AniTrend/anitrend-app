package com.mxt.anitrend.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.api.structure.Anime;
import com.mxt.anitrend.api.structure.Manga;
import com.mxt.anitrend.util.DateTimeConverter;
import com.mxt.anitrend.util.MarkDown;

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
    private Anime anime;
    private Manga manga;
    private UserSmall user;

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
        anime = in.readParcelable(Anime.class.getClassLoader());
        manga = in.readParcelable(Manga.class.getClassLoader());
        user = in.readParcelable(UserSmall.class.getClassLoader());
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
        return DateTimeConverter.convertDateString(date);
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

    public Spanned getSummary() {
        return MarkDown.convert(summary);
    }

    public boolean isReview_private() {
        return review_private == 1;
    }

    /**
     * Current user rating of review
     *
     * @return 0 - no rating <br> 1 up or positive rating <br> 2 down or negative rating
     */
    public int getUser_rating() {
        return user_rating;
    }

    public Spanned getText() {
        return MarkDown.convert(text);
    }

    public int getScore() {
        return score;
    }

    public Anime getAnime() {
        return anime;
    }

    public UserSmall getUser() {
        return user;
    }

    public Manga getManga() {
        return manga;
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
