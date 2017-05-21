package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Maxwell on 2/9/2017.
 */
@Root(name = "item", strict = false)
public class Episode implements Parcelable {

    @Element(name = "title")
    private String title;
    @Element(name = "link")
    private String link;
    @Element(name = "description")
    private String description;
    @Element(name = "publisher", required = false)
    private String publisher;
    @Element(name = "content")
    private MediaContent content;
    @ElementList(name = "thumbnail", inline = true, required = false)
    private List<Thumbnail> thumbnail;

    public Episode(@Element(name = "title")String title, @Element(name = "link")String link, @Element(name = "description")String description, @Element(name = "publisher", required = false)String publisher, @Element(name = "content")MediaContent content, @ElementList(name = "thumbnail", inline = true, required = false)List<Thumbnail> thumbnail) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.publisher = publisher;
        this.content = content;
        this.thumbnail = thumbnail;
    }

    protected Episode(Parcel in) {
        title = in.readString();
        link = in.readString();
        description = in.readString();
        publisher = in.readString();
        content = in.readParcelable(MediaContent.class.getClassLoader());
        thumbnail = in.createTypedArrayList(Thumbnail.CREATOR);
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        description = description.replaceAll("(<img[^>]*>)","").replaceFirst("(<br[^>]*>)","");
        if(description.isEmpty())
            return title+" has no summary information at the moment.";
        return description;
    }

    public String getPublisher() {
        return publisher;
    }

    public MediaContent getContent() {
        return content;
    }

    public List<Thumbnail> getThumbnail() {
        return thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(description);
        parcel.writeString(publisher);
        parcel.writeParcelable(content, i);
        parcel.writeTypedList(thumbnail);
    }
}
