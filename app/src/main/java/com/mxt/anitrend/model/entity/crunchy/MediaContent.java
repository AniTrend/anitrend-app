package com.mxt.anitrend.model.entity.crunchy;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by max on 2/9/2017.
 * <media:content url="https://www.crunchyroll.com/syndication/video?type=media&amp;id=727595" type="video/mp4" medium="video" duration="1421"/>
 */
@Root(name ="content", strict = false)
public class MediaContent implements Parcelable {

    @Attribute(name = "url", required = false)
    private String url;
    @Attribute(name = "type")
    private String type;
    @Attribute(name = "medium", required = false)
    private String medium;
    @Attribute(name = "duration", required = false)
    private String duration;

    public MediaContent(@Attribute(name = "url", required = false) String url,
                        @Attribute(name = "type") String type,
                        @Attribute(name = "medium") String medium,
                        @Attribute(name = "duration", required = false) String duration) {
        this.url = url;
        this.type = type;
        this.medium = medium;
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public String getMedium() {
        return medium;
    }

    public String getDuration() {
        return duration;
    }

    protected MediaContent(Parcel in) {
        url = in.readString();
        type = in.readString();
        medium = in.readString();
        duration = in.readString();
    }

    public static final Creator<MediaContent> CREATOR = new Creator<MediaContent>() {
        @Override
        public MediaContent createFromParcel(Parcel in) {
            return new MediaContent(in);
        }

        @Override
        public MediaContent[] newArray(int size) {
            return new MediaContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(type);
        parcel.writeString(medium);
        parcel.writeString(duration);
    }
}
