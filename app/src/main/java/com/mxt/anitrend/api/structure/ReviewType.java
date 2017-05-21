package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.mxt.anitrend.api.model.Review;

import java.util.List;

/**
 * Created by max on 2017/05/02.
 */

public class ReviewType implements Parcelable {

    private List<Review> anime;
    private List<Review> manga;

    protected ReviewType(Parcel in) {
        anime = in.createTypedArrayList(Review.CREATOR);
        manga = in.createTypedArrayList(Review.CREATOR);
    }

    public static final Creator<ReviewType> CREATOR = new Creator<ReviewType>() {
        @Override
        public ReviewType createFromParcel(Parcel in) {
            return new ReviewType(in);
        }

        @Override
        public ReviewType[] newArray(int size) {
            return new ReviewType[size];
        }
    };

    public List<Review> getAnime() {
        return anime;
    }

    public List<Review> getManga() {
        return manga;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(anime);
        dest.writeTypedList(manga);
    }

    public void setManga(List<Review> manga) {
        this.manga.addAll(manga);
    }

    public void setAnime(List<Review> anime) {
        this.anime.addAll(anime);
    }
}
