package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by Maxwell on 10/4/2016.
 */
public class SeriesListStats implements Parcelable {

    private int completed;
    private int on_hold;
    private int dropped;
    private int plan_to_watch;
    private int plan_to_read;
    private int reading;
    private int watching;

    protected SeriesListStats(Parcel in) {
        completed = in.readInt();
        on_hold = in.readInt();
        dropped = in.readInt();
        plan_to_watch = in.readInt();
        plan_to_read = in.readInt();
        reading = in.readInt();
        watching = in.readInt();
    }

    public static final Creator<SeriesListStats> CREATOR = new Creator<SeriesListStats>() {
        @Override
        public SeriesListStats createFromParcel(Parcel in) {
            return new SeriesListStats(in);
        }

        @Override
        public SeriesListStats[] newArray(int size) {
            return new SeriesListStats[size];
        }
    };

    public String getCompleted() {
        return String.format(Locale.getDefault(),"%d People", completed);
    }

    public String getOn_hold() {
        return String.format(Locale.getDefault(),"%d People", on_hold);
    }

    public String getDropped() {
        return String.format(Locale.getDefault(),"%d People",dropped);
    }

    public String getPlan_to_watch() {
        return String.format(Locale.getDefault(),"%d People",plan_to_watch);
    }

    public String getWatching() {
        return String.format(Locale.getDefault(),"%d People",watching);
    }

    public String getPlan_to_read() {
        return String.format(Locale.getDefault(),"%d People",plan_to_read);
    }

    public String getReading() {
        return String.format(Locale.getDefault(),"%d People",reading);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(completed);
        parcel.writeInt(on_hold);
        parcel.writeInt(dropped);
        parcel.writeInt(plan_to_watch);
        parcel.writeInt(plan_to_read);
        parcel.writeInt(reading);
        parcel.writeInt(watching);
    }
}
