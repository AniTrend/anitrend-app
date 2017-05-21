package com.mxt.anitrend.api.structure;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * Created by Maxwell on 1/12/2017.
 */

public class UserMangaStats implements Parcelable {

    private int reading;
    @SerializedName("plan to read")
    private int plan_to_read;
    private int completed;
    private int dropped;
    @SerializedName("on hold")
    private int on_hold;

    protected UserMangaStats(Parcel in) {
        reading = in.readInt();
        plan_to_read = in.readInt();
        completed = in.readInt();
        dropped = in.readInt();
        on_hold = in.readInt();
    }

    public static final Creator<UserMangaStats> CREATOR = new Creator<UserMangaStats>() {
        @Override
        public UserMangaStats createFromParcel(Parcel in) {
            return new UserMangaStats(in);
        }

        @Override
        public UserMangaStats[] newArray(int size) {
            return new UserMangaStats[size];
        }
    };

    public int getReading() {
        return reading;
    }

    public int getPlan_to_read() {
        return plan_to_read;
    }

    public int getComplete() {
        return completed;
    }

    public int getDropped() {
        return dropped;
    }

    public int getOn_hold() {
        return on_hold;
    }

    public String getTotalManga(){
        int amount = completed + on_hold + dropped + plan_to_read + reading;
        if(amount > 1000){
            return String.format(Locale.getDefault(), "%d K", amount/1000);
        }
        return String.format(Locale.getDefault(),"%d", amount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(reading);
        parcel.writeInt(plan_to_read);
        parcel.writeInt(completed);
        parcel.writeInt(dropped);
        parcel.writeInt(on_hold);
    }
}
