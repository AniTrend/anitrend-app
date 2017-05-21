package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Maxwell on 11/15/2016.
 */

public class UserLists implements Parcelable {

    private List<ListItem> on_hold;
    private List<ListItem> plan_to_read;
    private List<ListItem> dropped;
    private List<ListItem> completed;
    private List<ListItem> reading;
    private List<ListItem> watching;
    private List<ListItem> plan_to_watch;

    protected UserLists(Parcel in) {
        on_hold = in.createTypedArrayList(ListItem.CREATOR);
        plan_to_read = in.createTypedArrayList(ListItem.CREATOR);
        dropped = in.createTypedArrayList(ListItem.CREATOR);
        completed = in.createTypedArrayList(ListItem.CREATOR);
        reading = in.createTypedArrayList(ListItem.CREATOR);
        watching = in.createTypedArrayList(ListItem.CREATOR);
        plan_to_watch = in.createTypedArrayList(ListItem.CREATOR);
    }

    public static final Creator<UserLists> CREATOR = new Creator<UserLists>() {
        @Override
        public UserLists createFromParcel(Parcel in) {
            return new UserLists(in);
        }

        @Override
        public UserLists[] newArray(int size) {
            return new UserLists[size];
        }
    };

    public List<ListItem> getOn_hold() {
        return on_hold;
    }

    public List<ListItem> getPlan_to_read() {
        return plan_to_read;
    }

    public List<ListItem> getDropped() {
        return dropped;
    }

    public List<ListItem> getCompleted() {
        return completed;
    }

    public List<ListItem> getReading() {
        return reading;
    }

    public List<ListItem> getWatching() {
        return watching;
    }

    public List<ListItem> getPlan_to_watch() {
        return plan_to_watch;
    }

    public int getAnimeCount() {
        return (completed != null? completed.size():0) + (on_hold != null?on_hold.size():0)
                + (dropped!= null? dropped.size():0) + (watching!= null? watching.size():0)
                + (plan_to_watch!= null? plan_to_watch.size():0);
    }

    public int getMangaCount() {
        return (completed != null? completed.size():0) + (on_hold != null?on_hold.size():0)
                + (dropped!= null? dropped.size():0) + (reading!= null? reading.size():0)
                + (plan_to_read!= null? plan_to_read.size():0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(on_hold);
        parcel.writeTypedList(plan_to_read);
        parcel.writeTypedList(dropped);
        parcel.writeTypedList(completed);
        parcel.writeTypedList(reading);
        parcel.writeTypedList(watching);
        parcel.writeTypedList(plan_to_watch);
    }
}
