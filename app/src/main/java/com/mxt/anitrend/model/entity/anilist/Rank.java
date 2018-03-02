package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

import java.util.Locale;

/**
 * Created by Maxwell on 10/24/2016.
 */

public class Rank implements Parcelable {

    private int id;
    private int rank;
    private int type;
    private String type_string;
    private String ranking_type;
    private String format;
    private int year;
    private String season;

    protected Rank(Parcel in) {
        id = in.readInt();
        rank = in.readInt();
        type = in.readInt();
        type_string = in.readString();
        ranking_type = in.readString();
        format = in.readString();
        year = in.readInt();
        season = in.readString();
    }

    public static final Creator<Rank> CREATOR = new Creator<Rank>() {
        @Override
        public Rank createFromParcel(Parcel in) {
            return new Rank(in);
        }

        @Override
        public Rank[] newArray(int size) {
            return new Rank[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getRank() {
        return rank;
    }

    public int getType() {
        return type;
    }

    public String getTypeHtml() {
        if(year == 0)
            if(season != null)
                return String.format(Locale.getDefault(), "<b>#%d %s <small>%s<small/></b> <small>(%s)</small>", rank, type_string.toUpperCase(), season.toUpperCase(), format);
            else
                return String.format(Locale.getDefault(), "<b>#%d %s</b> <small>(%s)</small>", rank, type_string.toUpperCase(), format);
        if(season != null)
            return String.format(Locale.getDefault(), "<b>#%d %s <small>%s %d</small></b> <small>(%s)</small>", rank, type_string.toUpperCase(), season.toUpperCase(), year, format);
        return String.format(Locale.getDefault(), "<b>#%d %s <small>%d</small></b> <small>(%s)</small>", rank, type_string.toUpperCase(), year, format);
    }

    public String getTypeHtmlPlainTitle() {
        if(year == 0)
            if(season != null)
                return String.format(Locale.getDefault(), "%s <small>%s<small/> <small>(%s)</small>", type_string.toUpperCase(), season.toUpperCase(), format);
            else
                return String.format(Locale.getDefault(), "%s <small>(%s)</small>", type_string.toUpperCase(), format);
        if(season != null)
            return String.format(Locale.getDefault(), "%s <small>%s %d</small> <small>(%s)</small>", type_string.toUpperCase(), season.toUpperCase(), year, format);
        return String.format(Locale.getDefault(), "%s <small>%d</small> <small>(%s)</small>", type_string.toUpperCase(), year, format);
    }

    public String getRanking_type() {
        return ranking_type;
    }

    public String getFormat() {
        return format;
    }

    public int getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(rank);
        parcel.writeInt(type);
        parcel.writeString(type_string);
        parcel.writeString(ranking_type);
        parcel.writeString(format);
        parcel.writeInt(year);
        parcel.writeString(season);
    }
}
