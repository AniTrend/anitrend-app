package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mxt.anitrend.model.entity.general.Airing;

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
            "Winter", "Winter",
            "Spring", "Spring", "Spring",
            "Summer", "Summer", "Summer",
            "Fall", "Fall", "Fall",
            "Winter"
    };

    /**
     * Gets current season title
     * <br/>
     *
     * @return Season name
     */
    public static String getSeason(){
        int month = Calendar.getInstance().get(Calendar.MONTH);
        return seasons[month].toLowerCase();
    }

    public static String getSeriesSeason(long date){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd",Locale.getDefault());
        try {
            Date converted = format.parse(String.valueOf(date));
            Calendar calendar = new GregorianCalendar(Locale.getDefault());
            calendar.setTime(converted);
            return String.format(Locale.getDefault(),"%s %d", seasons[calendar.get(Calendar.MONTH)], calendar.get(Calendar.YEAR));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(date);
    }

    /**
     * Gets the current season title for menu
     * <br/>
     *
     * @return Season name
     */
    public static int getMenuSelect(){
        String value = seasons[Calendar.getInstance().get(Calendar.MONTH)].toLowerCase();
        return CompatUtil.getListFromArray(new String[]{"winter","spring","summer","fall"}).indexOf(value);
    }

    /**
     * Gets the current year
     * <br/>
     *
     * @return Year
     */
    public static int getYear(){
        if(Calendar.getInstance().get(Calendar.MONTH) >= 11 && getSeason().equals("winter"))
            return Calendar.getInstance().get(Calendar.YEAR)+ 1;
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Converts unix time representation into current readable time
     * <br/>
     *
     * @return A time format of dd MMM yyyy
     */
    @Deprecated
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
     * Converts date types of 20170218 from the API endpoint
     * <br/>
     *
     * @return A date format of MMM dd yyyy
     */
    public static String convertDate(long date){
        if(date == 0)
            return "TBA";
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd",Locale.getDefault());
        try {
            Date converted = format.parse(String.valueOf(date));
            return new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(date);
    }

    /**
     * Converts any date format of yyyy-MM-dd'T'HH:mm:ss+HH:mm
     * <br/>
     *
     * @return A date format of MMM dd yyyy
     */
    public static @Nullable String convertDateString(String value){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+HH:mm",Locale.getDefault());
        try {
            Date converted = format.parse(value);
            return new SimpleDateFormat("MMM dd yyyy",Locale.getDefault()).format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**Only for fuzzy dates*/
    private static boolean isNewerDate(long date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",Locale.getDefault());
        Date strDate = sdf.parse(convertDate(date));
        return strDate.getTime() > System.currentTimeMillis();
    }

    /**
     * Returns appropriate title for ends or ended
     * <br/>
     * @param time - unix time representations
     */
    public static String getEndTitle(long time) {
        if(time == 0)
            return "Ends";

        try {
            return isNewerDate(time)? "Ends":"Ended";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Ends";
    }

    /**
     * Formats the epotch time to a pretty data
     * <br/>
     * @return string such as "EP 6 Airing in 2 hours"
     * @param airing - the current airing object of a series
     */
    public static String getNextEpDate(@NonNull Airing airing){
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String from_now = prettyTime.format(new Date(System.currentTimeMillis()+(airing.getCountdown()*1000L)));
        return String.format(Locale.getDefault(), "EP %d: %s",airing.getNext_episode(), from_now);
    }

    /**
     * Returns appropriate title for starts or started
     * <br/>
     * @param time - unix time representations
     */
    public static String getStartTitle(long time){
        if(time == 0)
            return "Starts";

        try {
            return isNewerDate(time)? "Starts":"Started";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Starts";
    }

    /**
     * For dates that don't include a timezones,
     * the underlying method sets the time with a +7 offset
     * <br/>
     * @param date - a date with the format of yyyy-MM-dd HH:mm:ss
     */
    public static @Nullable String getPrettyDateCustom(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("Japan"));
        try {
            Date converted = format.parse(date);
            PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
            return prettyTime.format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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

    public static Integer[] getStringYearRanges() {
        final int start = 1951; // default used to be 1995
        Integer[] years = new Integer[(getYear()+2)-start];
        for (int i = start; i <= getYear()+1; i++) {
            years[(i - start)] = i;
        }
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
