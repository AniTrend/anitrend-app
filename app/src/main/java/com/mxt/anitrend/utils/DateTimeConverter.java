package com.mxt.anitrend.utils;

import com.mxt.anitrend.api.structure.Airing;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Maxwell on 10/7/2016.
 */

public final class DateTimeConverter {

    private static final String seasons[] = {
            "winter", "winter",
            "spring", "spring", "spring",
            "summer", "summer", "summer",
            "fall", "fall", "fall",
            "winter"
    };

    /**
     * Gets season name from a series season number
     * <br/>
     *
     * @return Season name
     */
    @Deprecated
    public static String getSeasonName(Integer passing){
        if(passing == null)
            return "TBA";
        try{
            String rep = String.valueOf(passing);
            switch (rep.substring(2)) {
                case "1":
                    return " Winter";
                case "2":
                    return " Spring";
                case "3":
                    return " Summer";
                case "4":
                    return " Fall";
                default:
                    return "TBA";
            }
        } catch (Exception ex){
            return "N/A";
        }
    }

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

    /**
     * Gets the current season title for menu
     * <br/>
     *
     * @return Season name
     */
    public static int getMenuSelect(){
        String value = seasons[Calendar.getInstance().get(Calendar.MONTH)];
        int index = Arrays.asList(new String[]{"winter","spring","summer","fall"}).indexOf(value);
        return index;
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
    public static String convertLongDate(long value) {
        try {
            return value == 0? "N/A" : new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(value*1000L));
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Unknown";
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
        return "TBA";
    }

    /**
     * Converts any date format of yyyy-MM-dd'T'HH:mm:ss+HH:mm
     * <br/>
     *
     * @return A date format of MMM dd yyyy
     */
    public static String convertDateString(String value){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+HH:mm",Locale.getDefault());
        try {
            Date converted = format.parse(value);
            return new SimpleDateFormat("MMM dd yyyy",Locale.getDefault()).format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Unknown Date";
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
     * Formats the epotch time to a pretty data
     * <br/>
     * @return string such as "EP 6 Airing in 2 hours"
     * @param airing - the current airing object of a series
     */
    public static String getNextEpDate(Airing airing){
        if(airing == null)
            return "N/A";
        else {
            PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
            String from_now = prettyTime.format(new Date(System.currentTimeMillis()+(airing.getCountdown()*1000L)));
            return String.format(Locale.getDefault(), "EP %d - %s",airing.getNext_episode(), from_now);
        }
    }

    /**
     * Generates a pretty data "x y from now" where y can be minutes, hours, days or even weeks e.t.c
     * <br/>
     * @return Pretty Date (x hours from now / x hours ago)
     * @param date - a date with the format of yyyy-MM-dd HH:mm:ss
     */
    public static String getPrettyDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        try {
            Date converted = format.parse(date);
            PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
            return prettyTime.format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Unknown Time";
    }

    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm:ss",Locale.getDefault());
        try {
            Date date = format.parse(DateFormat.getDateTimeInstance().format(new Date()));
            return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * For dates that don't include a timezones,
     * the underlying method sets the time with a +7 offset
     * <br/>
     * @param date - a date with the format of yyyy-MM-dd HH:mm:ss
     */
    public static String getPrettyDateCustom(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("Japan"));
        try {
            Date converted = format.parse(date);
            PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
            return prettyTime.format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Unknown Time";
    }

    /**
     * Get the pretty time from a unix time value
     * <br/>
     * @param updated_at unix time (epotch)
     * @return Pretty Date (x hours from now / x hours ago)
     */
    public static String getLastUpdated(long updated_at) {
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        return prettyTime.format(new Date(updated_at*1000L));
    }

    /**
     * A loop to create year ranges list
     * from the year 1995 to current year + 1
     */
    public static List<Integer> getYearRanges() {
        final int start = 1951; // default used to be 1995
        List<Integer> years = new ArrayList<>((getYear()+1)-start);
        for (int i = start; i <= getYear()+1; i++) {
            years.add(i);
        }
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

}
