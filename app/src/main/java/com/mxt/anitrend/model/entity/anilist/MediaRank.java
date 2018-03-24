package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mxt.anitrend.util.KeyUtils;

import java.util.Locale;

/**
 * Created by Maxwell on 10/24/2016.
 */

public class MediaRank implements Parcelable {

    private int id;
    private int rank;
    private @KeyUtils.MediaRankType String type;
    private @KeyUtils.MediaFormat String format;
    private int year;
    private @KeyUtils.MediaSeason String season;
    private boolean allTime;
    private String context;

    protected MediaRank(Parcel in) {
        id = in.readInt();
        rank = in.readInt();
        type = in.readString();
        format = in.readString();
        year = in.readInt();
        season = in.readString();
        allTime = in.readByte() != 0;
        context = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(rank);
        dest.writeString(type);
        dest.writeString(format);
        dest.writeInt(year);
        dest.writeString(season);
        dest.writeByte((byte) (allTime ? 1 : 0));
        dest.writeString(context);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaRank> CREATOR = new Creator<MediaRank>() {
        @Override
        public MediaRank createFromParcel(Parcel in) {
            return new MediaRank(in);
        }

        @Override
        public MediaRank[] newArray(int size) {
            return new MediaRank[size];
        }
    };

    public String getTypeHtml() {
        if(year == 0)
            if(season != null)
                return String.format(Locale.getDefault(), "<b>#%d %s <small>%s<small/></b> <small>(%s)</small>", rank, context.toUpperCase(), season.toUpperCase(), format);
            else
                return String.format(Locale.getDefault(), "<b>#%d %s</b> <small>(%s)</small>", rank, context.toUpperCase(), format);
        if(season != null)
            return String.format(Locale.getDefault(), "<b>#%d %s <small>%s %d</small></b> <small>(%s)</small>", rank, context.toUpperCase(), season.toUpperCase(), year, format);
        return String.format(Locale.getDefault(), "<b>#%d %s <small>%d</small></b> <small>(%s)</small>", rank, context.toUpperCase(), year, format);
    }

    public String getTypeHtmlPlainTitle() {
        if(year == 0)
            if(season != null)
                return String.format(Locale.getDefault(), "%s <small>%s<small/> <small>(%s)</small>", context.toUpperCase(), season, format);
            else
                return String.format(Locale.getDefault(), "%s <small>(%s)</small>", context.toUpperCase(), format);
        if(season != null)
            return String.format(Locale.getDefault(), "%s <small>%s %d</small> <small>(%s)</small>", context.toUpperCase(), season, year, format);
        return String.format(Locale.getDefault(), "%s <small>%d</small> <small>(%s)</small>", context.toUpperCase(), year, format);
    }

    public int getId() {
        return id;
    }

    public int getRank() {
        return rank;
    }

    public @NonNull @KeyUtils.MediaRankType String getType() {
        return type;
    }

    public @NonNull @KeyUtils.MediaFormat String getFormat() {
        return format;
    }

    public int getYear() {
        return year;
    }

    public @KeyUtils.MediaSeason String getSeason() {
        return season;
    }

    public boolean isAllTime() {
        return allTime;
    }

    public @NonNull String getContext() {
        return context;
    }
}
