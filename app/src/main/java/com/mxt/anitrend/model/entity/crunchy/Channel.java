package com.mxt.anitrend.model.entity.crunchy;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.util.List;

/**
 * Created by max on 2/9/2017.
 */
@Root(name = "channel", strict = false)
public class Channel implements Parcelable {

    @Element(name = "title", required = false)
    private String title;

    @Text(required = false)
    @Path("link")
    private String link;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "copyright", required = false)
    private String copyright;

    @ElementList(name = "episode", inline = true, required = false)
    private List<Episode> episode;

    public Channel(@Element(name = "title", required = false) String title,
                   @Path("link") @Text(required=false) String link,
                   @Element(name = "description", required = false)String description,
                   @Element(name = "copyright", required = false) String copyright,
                   @ElementList(name = "episode", inline = true) List<Episode> episode) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.copyright = copyright;
        this.episode = episode;
    }

    protected Channel(Parcel in) {
        title = in.readString();
        link = in.readString();
        description = in.readString();
        copyright = in.readString();
        episode = in.createTypedArrayList(Episode.CREATOR);
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getCopyright() {
        return copyright;
    }

    public List<Episode> getEpisode() {
        return episode;
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
        parcel.writeString(copyright);
        parcel.writeTypedList(episode);
    }
}
