package com.mxt.anitrend.util.date

import androidx.annotation.IntRange
import com.annimon.stream.Collectors
import com.annimon.stream.IntStream
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by max on 2017/06/09.
 * Class to provide any calendar functionality
 */

object DateUtil {

    private val seasons by lazy {
        arrayOf(
            KeyUtil.WINTER,
            KeyUtil.WINTER,
            KeyUtil.SPRING,
            KeyUtil.SPRING,
            KeyUtil.SPRING,
            KeyUtil.SUMMER,
            KeyUtil.SUMMER,
            KeyUtil.SUMMER,
            KeyUtil.FALL,
            KeyUtil.FALL,
            KeyUtil.FALL,
            KeyUtil.WINTER
        )
    }

    private const val dateOutputFormat = "MMM dd, yyyy"
    private const val dateInputFormat = "yyyy/MM/dd"

    /**
     * Gets current season title
     * <br></br>
     *
     * @return Season name
     */
    val currentSeason: String
        @KeyUtil.MediaSeason get() {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            return seasons[month]
        }

    /**
     * Gets the current season title for menu
     * <br></br>
     *
     * @return Season name
     */
    val menuSelect: Int
        @IntRange(from = 0, to = 4) get() {
            val season = seasons[Calendar.getInstance().get(Calendar.MONTH)]
            return CompatUtil.constructListFrom(*KeyUtil.MediaSeason)
                .indexOf(season)
        }

    /**
     * Returns the current month
     */
    val month: Int
        @IntRange(from = 0, to = 11) get() =
            Calendar.getInstance().get(Calendar.MONTH)

    /**
     * Returns the current date
     */
    val date: Int
        @IntRange(from = 0, to = 31) get() =
            Calendar.getInstance().get(Calendar.DATE)

    /**
     * Returns the current year
     */
    val year: Int
        get() = Calendar.getInstance().get(Calendar.YEAR)

    /**
     * Get the current fuzzy date
     */
    val currentDate: FuzzyDate
        get() = FuzzyDate(
            date, month + 1,
            year
        )

    fun getDateOutputFormat(fuzzyDate: FuzzyDate): String? {
        if (fuzzyDate.day != 0 && fuzzyDate.month != 0 && fuzzyDate.year != 0)
            return "MMM dd, yyyy"

        if (fuzzyDate.month != 0 && fuzzyDate.year != 0)
            return "MMM, yyyy"

        if (fuzzyDate.year != 0)
            return "yyyy"

        return null
    }

    fun getMediaSeason(fuzzyDate: FuzzyDate): String {
        val format = SimpleDateFormat(dateInputFormat, Locale.getDefault())
        try {
            val converted = format.parse(fuzzyDate.toString())
            val calendar = GregorianCalendar(Locale.getDefault())
            if (converted != null)
                calendar.time = converted
            return String.format(Locale.getDefault(), "%s %d",
                CompatUtil.capitalizeWords(
                    seasons[calendar.get(
                        Calendar.MONTH
                    )]
                ),
                    calendar.get(Calendar.YEAR))

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return fuzzyDate.toString()
    }

    /**
     * Gets the current year + delta, if the season for the year is winter later in the year
     * then the result would be the current year plus the delta
     * <br></br>
     *
     * @return current year with a given delta
     */
    fun getCurrentYear(delta: Int = 0): Int {
        return if (month >= 11 && currentSeason == KeyUtil.WINTER)
            year + delta
        else year
    }

    /**
     * Converts unix time representation into current readable time
     * <br></br>
     *
     * @return A time format of [DateUtil.dateOutputFormat]
     */
    fun convertDate(value: Long): String? {
        try {
            if (value != 0L)
                return SimpleDateFormat(dateOutputFormat, Locale.getDefault()).format(Date(value * 1000L))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    /**
     * Converts unix time representation into current readable time
     * <br></br>
     *
     * @return A time format of [DateUtil.dateOutputFormat]
     */
    fun convertDate(fuzzyDate: FuzzyDate?): String? {
        try {
            if (fuzzyDate != null && fuzzyDate.isValidDate) {
                val simpleDateFormat = SimpleDateFormat(dateInputFormat, Locale.getDefault())
                val converted = simpleDateFormat.parse(fuzzyDate.toString())
                if (converted != null)
                    return SimpleDateFormat(getDateOutputFormat(fuzzyDate), Locale.getDefault()).format(converted)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return "TBA"
    }

    /**
     * Checks if the given data is newer than the current data on the device
     */
    @Throws(ParseException::class)
    private fun isNewerDate(fuzzyDate: FuzzyDate): Boolean {
        val format = SimpleDateFormat(dateInputFormat, Locale.getDefault())
        val converted = format.parse(fuzzyDate.toString())
        return (converted?.time ?: 0) > System.currentTimeMillis()
    }

    /**
     * Returns appropriate title for ends or ended
     * <br></br>
     * @param fuzzyDate - fuzzy date
     */
    fun getEndTitle(fuzzyDate: FuzzyDate?): String {
        if (fuzzyDate == null || !fuzzyDate.isValidDate)
            return "Ends"

        try {
            return if (isNewerDate(fuzzyDate)) "Ends" else "Ended"
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return "Ends"
    }

    /**
     * Returns appropriate title for starts or started
     * <br></br>
     * @param fuzzyDate - fuzzy date
     */
    fun getStartTitle(fuzzyDate: FuzzyDate?): String {
        if (fuzzyDate == null || !fuzzyDate.isValidDate)
            return "Starts"

        try {
            return if (isNewerDate(fuzzyDate)) "Starts" else "Started"
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return "Starts"
    }

    /**
     * Formats the epotch time to a pretty data
     * <br></br>
     * @return string such as "EP 6 AiringSchedule in 2 hours"
     * @param airingSchedule - the current airingSchedule object of a series
     */
    fun getNextEpDate(airingSchedule: AiringSchedule): String {
        val prettyTime = PrettyTime(Locale.getDefault())
        val fromNow = prettyTime.format(Date(
                System.currentTimeMillis() + airingSchedule.timeUntilAiring * 1000L)
        )
        return String.format(Locale.getDefault(), "EP %d: %s", airingSchedule.episode, fromNow)
    }

    /**
     * Unix time stamps dates
     * <br></br>
     * @param date - a unix timestamp
     */
    fun getPrettyDateUnix(date: Long): String {
        val prettyTime = PrettyTime(Locale.getDefault())
        return prettyTime.format(Date(date * 1000L))
    }

    /**
     * Creates a range of years from the given begin year to the end delta
     * @param start Starting year
     * @param endDelta End difference plus or minus the current year
     */
    fun getYearRanges(start: Int, endDelta: Int): List<Int> {
        return IntStream.rangeClosed(start,
            getCurrentYear(endDelta)
        ).boxed().collect(Collectors.toList())
            .orEmpty()
    }

    /**
     * Checks if the time given has a difference greater than or equal to the target time
     * <br></br>
     * @param conversionTarget type of comparison between the epoch time and target
     * @param epochTime time to compare against the current system clock
     * @param target unit to compare against
     */
    fun timeDifferenceSatisfied(@KeyUtil.TimeTargetType conversionTarget: Int, epochTime: Long, target: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        val defaultSystemUnit = TimeUnit.MILLISECONDS
        when (conversionTarget) {
            KeyUtil.TIME_UNIT_DAYS ->
                return defaultSystemUnit.toDays(currentTime - epochTime) >= target
            KeyUtil.TIME_UNIT_HOURS ->
                return defaultSystemUnit.toHours(currentTime - epochTime) >= target
            KeyUtil.TIME_UNIT_MINUTES ->
                return defaultSystemUnit.toMinutes(currentTime - epochTime) >= target
            KeyUtil.TIME_UNITS_SECONDS ->
                return defaultSystemUnit.toSeconds(currentTime - epochTime) >= target
        }
        return false
    }
}
