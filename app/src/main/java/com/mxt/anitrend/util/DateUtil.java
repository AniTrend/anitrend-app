package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by max on 2017/06/09.
 * Class to provide any calendar functionality
 */

public class DateUtil {

    private static final String seasons[] = {
            KeyUtils.WINTER, KeyUtils.WINTER,
            KeyUtils.SPRING, KeyUtils.SPRING, KeyUtils.SPRING,
            KeyUtils.SUMMER, KeyUtils.SUMMER, KeyUtils.SUMMER,
            KeyUtils.FALL, KeyUtils.FALL, KeyUtils.FALL,
            KeyUtils.WINTER
    };

    /**
     * Gets current season title
     * <br/>
     *
     * @return Season name
     */
    public static String getSeason(){
        int month = Calendar.getInstance().get(Calendar.MONTH);
        return seasons[month];
    }

    public static String getSeriesSeason(FuzzyDate fuzzyDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
        try {
            Date converted = format.parse(String.valueOf(fuzzyDate));
            Calendar calendar = new GregorianCalendar(Locale.getDefault());
            calendar.setTime(converted);
            return String.format(Locale.getDefault(),"%s %d", seasons[calendar.get(Calendar.MONTH)], calendar.get(Calendar.YEAR));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(fuzzyDate);
    }

    /**
     * Gets the current season title for menu
     * <br/>
     *
     * @return Season name
     */
    public static int getMenuSelect(){
        String value = seasons[Calendar.getInstance().get(Calendar.MONTH)];
        return CompatUtil.getListFromArray(KeyUtils.MediaSeasons).indexOf(value);
    }

    /**
     * Gets the current year
     * <br/>
     *
     * @return Year
     */
    public static int getYear(){
        if(Calendar.getInstance().get(Calendar.MONTH) >= 11 && getSeason().equals(KeyUtils.WINTER))
            return Calendar.getInstance().get(Calendar.YEAR)+ 1;
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Converts unix time representation into current readable time
     * <br/>
     *
     * @return A time format of dd MMM yyyy
     */
    public static @Nullable String convertLongDate(long value) {
        try {
            if(value != 0)
                return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(value*1000L));
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Converts unix time representation into current readable time
     * <br/>
     *
     * @return A time format of dd MMM yyyy
     */
    public static @Nullable String convertDate(FuzzyDate fuzzyDate) {
        try {
            if(fuzzyDate != null && fuzzyDate.isValidDate()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date converted = simpleDateFormat.parse(String.valueOf(fuzzyDate));
                return new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(converted);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return "TBA";
    }

    /**
     * Checks if the given data is newer than the current data on the device
     */
    private static boolean isNewerDate(FuzzyDate fuzzyDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
        Date converted = format.parse(String.valueOf(fuzzyDate));
        return converted.getTime() > System.currentTimeMillis();
    }

    /**
     * Returns appropriate title for ends or ended
     * <br/>
     * @param fuzzyDate - fuzzy date
     */
    public static String getEndTitle(FuzzyDate fuzzyDate) {
        if(fuzzyDate == null || !fuzzyDate.isValidDate())
            return "Ends";

        try {
            return isNewerDate(fuzzyDate)? "Ends":"Ended";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Ends";
    }

    /**
     * Returns appropriate title for starts or started
     * <br/>
     * @param fuzzyDate - fuzzy date
     */
    public static String getStartTitle(FuzzyDate fuzzyDate){
        if(fuzzyDate == null || !fuzzyDate.isValidDate())
            return "Starts";

        try {
            return isNewerDate(fuzzyDate)? "Starts":"Started";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Starts";
    }

    /**
     * Formats the epotch time to a pretty data
     * <br/>
     * @return string such as "EP 6 AiringSchedule in 2 hours"
     * @param airingSchedule - the current airingSchedule object of a series
     */
    public static @NonNull String getNextEpDate(@NonNull AiringSchedule airingSchedule){
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String from_now = prettyTime.format(new Date(System.currentTimeMillis()+(airingSchedule.getTimeUntilAiring() * 1000L)));
        return String.format(Locale.getDefault(), "EP %d: %s", airingSchedule.getEpisode(), from_now);
    }

    /**
     * Unix time stamps dates
     * <br/>
     * @param date - a unix timestamp
     */
    public static @NonNull String getPrettyDateUnix(long date) {
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        return prettyTime.format(new Date(date * 1000L));
    }

    /**
     * A loop to create year ranges list
     * from the year 1995 to current year + 1
     */
    public static List<Integer> getYearRanges() {
        final int start = 1951; // default used to be 1995
        List<Integer> years = new ArrayList<>((getYear()+1)-start);
        for (int i = start; i <= getYear()+1; i++)
            years.add(i);
        return years;
    }

    /**
     * Returns the current month
     */
    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    /**
     * Returns the current date
     */
    public static int getDate() {
        return Calendar.getInstance().get(Calendar.DATE);
    }

    /**
     * Checks if the time given has a difference greater than or equal to the target time
     * <br/>
     * @param conversionTarget type of comparison between the epoch time and target
     * @param epochTime time to compare against the current system clock
     * @param target unit to compare against
     */
    public static boolean timeDifferenceSatisfied(@KeyUtils.TimeTargetType int conversionTarget, long epochTime, int target) {
        long currentTime = System.currentTimeMillis();
        TimeUnit defaultSystemUnit = TimeUnit.MILLISECONDS;
        switch (conversionTarget) {
            case KeyUtils.TIME_UNIT_DAYS:
                return defaultSystemUnit.toDays(currentTime - epochTime) >= target;
            case KeyUtils.TIME_UNIT_HOURS:
                return defaultSystemUnit.toHours(currentTime - epochTime) >= target;
            case KeyUtils.TIME_UNIT_MINUTES:
                return defaultSystemUnit.toMinutes(currentTime - epochTime) >= target;
            case KeyUtils.TIME_UNITS_SECONDS:
                return defaultSystemUnit.toSeconds(currentTime - epochTime) >= target;
        }
        return false;
    }
}
