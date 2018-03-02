package com.mxt.anitrend.model.entity.crunchy;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by max on 2/9/2017.
 */
@Root(name ="thumbnail", strict = false)
public class Thumbnail implements Parcelable {

    @Attribute(name = "url")
    private String url;

    @Attribute(name = "width")
    private int width;

    @Attribute(name = "height")
    private int height;

    public Thumbnail(@Attribute(name = "url") String url, @Attribute(name = "width") int width, @Attribute(name = "height") int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    protected Thumbnail(Parcel in) {
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static final Creator<Thumbnail> CREATOR = new Creator<Thumbnail>() {
        @Override
        public Thumbnail createFromParcel(Parcel in) {
            return new Thumbnail(in);
        }

        @Override
        public Thumbnail[] newArray(int size) {
            return new Thumbnail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeInt(width);
        parcel.writeInt(height);
    }
}
