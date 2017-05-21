package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by gigabyte on 2017/02/07.
 */
@Root(name = "rss", strict = false)
public class Rss implements Parcelable {

    @Element(name = "channel")
    private Channel channel;

    public Rss(@Element(name = "channel") Channel channel) {
        this.channel = channel;
    }

    protected Rss(Parcel in) {
        channel = in.readParcelable(Channel.class.getClassLoader());
    }

    public static final Creator<Rss> CREATOR = new Creator<Rss>() {
        @Override
        public Rss createFromParcel(Parcel in) {
            return new Rss(in);
        }

        @Override
        public Rss[] newArray(int size) {
            return new Rss[size];
        }
    };

    public Channel getChannel() {
        return channel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(channel, i);
    }
}
