package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class ExternalLink implements Parcelable {

    private int id;
    private String url;
    private String site;

    public ExternalLink(String url, String site) {
        this.url = url;
        this.site = site;
    }

    protected ExternalLink(Parcel in) {
        id = in.readInt();
        url = in.readString();
        site = in.readString();
    }

    public static final Creator<ExternalLink> CREATOR = new Creator<ExternalLink>() {
        @Override
        public ExternalLink createFromParcel(Parcel in) {
            return new ExternalLink(in);
        }

        @Override
        public ExternalLink[] newArray(int size) {
            return new ExternalLink[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getSite() {
        return site;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(url);
        parcel.writeString(site);
    }
}
