package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

/**
 * Created by Maxwell on 12/30/2016.
 */

public class LibraryRep implements Parcelable {

    private String name;
    private String description;
    private String link;

    protected LibraryRep(Parcel in) {
        name = in.readString();
        description = in.readString();
        link = in.readString();
    }

    public static final Creator<LibraryRep> CREATOR = new Creator<LibraryRep>() {
        @Override
        public LibraryRep createFromParcel(Parcel in) {
            return new LibraryRep(in);
        }

        @Override
        public LibraryRep[] newArray(int size) {
            return new LibraryRep[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Spanned getDescription() {
        return Html.fromHtml(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(link);
    }
}
