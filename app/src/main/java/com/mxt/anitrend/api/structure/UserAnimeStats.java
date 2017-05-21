package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * Created by Maxwell on 1/12/2017.
 */

public class UserAnimeStats implements Parcelable {

    private int completed;
    @SerializedName("on hold")
    private int on_hold;
    private int dropped;
    @SerializedName("plan to watch")
    private int plan_to_watch;
    private int watching;

    protected UserAnimeStats(Parcel in) {
        completed = in.readInt();
        on_hold = in.readInt();
        dropped = in.readInt();
        plan_to_watch = in.readInt();
        watching = in.readInt();
    }

    public static final Creator<UserAnimeStats> CREATOR = new Creator<UserAnimeStats>() {
        @Override
        public UserAnimeStats createFromParcel(Parcel in) {
            return new UserAnimeStats(in);
        }

        @Override
        public UserAnimeStats[] newArray(int size) {
            return new UserAnimeStats[size];
        }
    };

    public int getCompleted() {
        return completed;
    }

    public int getOn_hold() {
        return on_hold;
    }

    public int getDropped() {
        return dropped;
    }

    public int getPlan_to_watch() {
        return plan_to_watch;
    }

    public int getWatching() {
        return watching;
    }

    public String getTotalAnime(){
        int amount = completed + on_hold + dropped + plan_to_watch + watching;
        if(amount > 1000){
            return String.format(Locale.getDefault(), "%.1f K", (float)amount/1000);
        }
        return String.format(Locale.getDefault(),"%d", amount);
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
        parcel.writeInt(watching);
    }
}
