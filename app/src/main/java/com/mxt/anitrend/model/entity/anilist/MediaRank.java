package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.Locale;

/**
 * Created by Maxwell on 10/24/2016.
 */

public class MediaRank implements Parcelable {

    private int id;
    private int rank;
    private @KeyUtil.MediaRankType String type;
    private @KeyUtil.MediaFormat String format;
    private int year;
    private @KeyUtil.MediaSeason String season;
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
                return String.format(Locale.getDefault(), "<b>#%d %s <small>%s<small/></b> <small>(%s)</small>",
                        rank, context.toUpperCase(), season.toUpperCase(), CompatUtil.INSTANCE.capitalizeWords(format));
            else
                return String.format(Locale.getDefault(), "<b>#%d %s</b> <small>(%s)</small>",
                        rank, context.toUpperCase(), CompatUtil.INSTANCE.capitalizeWords(format));
        if(season != null)
            return String.format(Locale.getDefault(), "<b>#%d %s <small>%s %d</small></b> <small>(%s)</small>",
                    rank, context.toUpperCase(), season.toUpperCase(), year, CompatUtil.INSTANCE.capitalizeWords(format));
        return String.format(Locale.getDefault(), "<b>#%d %s <small>%d</small></b> <small>(%s)</small>",
                rank, context.toUpperCase(), year, CompatUtil.INSTANCE.capitalizeWords(format));
    }

    public String getTypeHtmlPlainTitle() {
        if(year == 0)
            if(season != null)
                return String.format(Locale.getDefault(), "%s <small>%s<small/> <small>(%s)</small>",
                        context.toUpperCase(), season, CompatUtil.INSTANCE.capitalizeWords(format));
            else
                return String.format(Locale.getDefault(), "%s <small>(%s)</small>",
                        context.toUpperCase(), CompatUtil.INSTANCE.capitalizeWords(format));
        if(season != null)
            return String.format(Locale.getDefault(), "%s <small>%s %d</small> <small>(%s)</small>",
                    context.toUpperCase(), season, year, CompatUtil.INSTANCE.capitalizeWords(format));
        return String.format(Locale.getDefault(), "%s <small>%d</small> <small>(%s)</small>",
                context.toUpperCase(), year, CompatUtil.INSTANCE.capitalizeWords(format));
    }

    public int getId() {
        return id;
    }

    public int getRank() {
        return rank;
    }

    public @NonNull @KeyUtil.MediaRankType String getType() {
        return type;
    }

    public @NonNull @KeyUtil.MediaFormat String getFormat() {
        return format;
    }

    public int getYear() {
        return year;
    }

    public @KeyUtil.MediaSeason String getSeason() {
        return season;
    }

    public boolean isAllTime() {
        return allTime;
    }

    public @NonNull String getContext() {
        return context;
    }
}
