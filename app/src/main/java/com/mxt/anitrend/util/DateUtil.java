package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.IntStream;
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by max on 2017/06/09.
 * Class to provide any calendar functionality
 */

public class DateUtil {

    private static final String seasons[] = {
            KeyUtil.WINTER, KeyUtil.WINTER,
            KeyUtil.SPRING, KeyUtil.SPRING, KeyUtil.SPRING,
            KeyUtil.SUMMER, KeyUtil.SUMMER, KeyUtil.SUMMER,
            KeyUtil.FALL, KeyUtil.FALL, KeyUtil.FALL,
            KeyUtil.WINTER
    };

    /**
     * Gets current season title
     * <br/>
     *
     * @return Season name
     */
    public static @KeyUtil.MediaSeason String getCurrentSeason(){
        int month = Calendar.getInstance().get(Calendar.MONTH);
        return seasons[month];
    }

    public static String getMediaSeason(FuzzyDate fuzzyDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
        try {
            Date converted = format.parse(String.valueOf(fuzzyDate));
            Calendar calendar = new GregorianCalendar(Locale.getDefault());
            calendar.setTime(converted);

            return String.format(Locale.getDefault(),"%s %d",
                    CompatUtil.capitalizeWords(seasons[calendar.get(Calendar.MONTH)]),
                    calendar.get(Calendar.YEAR));

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
        return CompatUtil.constructListFrom(KeyUtil.MediaSeason).indexOf(value);
    }

    /**
     * Gets the current year + delta, if the season for the year is winter later in the year
     * then the result would be the current year plus the delta
     * <br/>
     *
     * @return current year with a given delta
     */
    public static int getCurrentYear(int delta){
        if(Calendar.getInstance().get(Calendar.MONTH) >= 11 && getCurrentSeason().equals(KeyUtil.WINTER))
            return Calendar.getInstance().get(Calendar.YEAR) + delta;
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Converts unix time representation into current readable time
     * <br/>
     *
     * @return A time format of dd MMM yyyy
     */
    public static @Nullable String convertDate(long value) {
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
     * Creates a range of years from the given begin year to the end delta
     * @param start Starting year
     * @param endDelta End difference plus or minus the current year
     */
    public static List<Integer> getYearRanges(int start, int endDelta) {
        return IntStream.rangeClosed(start, getCurrentYear(endDelta))
                .boxed().collect(Collectors.toList());
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
    public static boolean timeDifferenceSatisfied(@KeyUtil.TimeTargetType int conversionTarget, long epochTime, int target) {
        long currentTime = System.currentTimeMillis();
        TimeUnit defaultSystemUnit = TimeUnit.MILLISECONDS;
        switch (conversionTarget) {
            case KeyUtil.TIME_UNIT_DAYS:
                return defaultSystemUnit.toDays(currentTime - epochTime) >= target;
            case KeyUtil.TIME_UNIT_HOURS:
                return defaultSystemUnit.toHours(currentTime - epochTime) >= target;
            case KeyUtil.TIME_UNIT_MINUTES:
                return defaultSystemUnit.toMinutes(currentTime - epochTime) >= target;
            case KeyUtil.TIME_UNITS_SECONDS:
                return defaultSystemUnit.toSeconds(currentTime - epochTime) >= target;
        }
        return false;
    }
}
