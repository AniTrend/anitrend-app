package com.mxt.anitrend.util;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by max on 2017/04/08.
 * Pattern matcher uses regex to return possible matches for media types
 */
public class PatternMatcher {

    public static final String KEY_IMG = "img";
    public static final String KEY_WEB = "webm";
    public static final String KEY_YOU = "youtube";
    public static final String NO_THUMBNAIL = "http://placehold.it/1280x720?text=No+Preview+Available";

    private static final String VID_THUMB = "https://img.youtube.com/vi/%s/hqdefault.jpg";
    private static final String Youtube = "https://www.youtube.com/watch?v=";
    private static final String YoutubeShort = "https://youtu.be/";
    private static final String PATTERN_YOUTUBE_EXTRACT = "(\\?v=)(.*)";

    private static final Pattern pattern;
    private static final String PATTERN_MEDIA = "(img|webm|youtube).*?(\\([^)]+\\))";
    private static final String PATTERN_DEEP_LINKS = "(user|manga|anime|character|staff)\\/(.*)";

    private static final String USER_URL_LINK = "__[%s](https://anilist.co/user/%s)__";
    private static final String PATTERN_USER_TAGS = "(@[A-Za-z]\\w+)";

    private static final String PATTERN_TRAILING_SPACES = "(^[\\r\\n]+|[\\r\\n]+$)";


    static {
        pattern = Pattern.compile(PATTERN_MEDIA);
    }

    /**
     * finds images and youtube videos
     */
    public static Matcher findMedia(String param) {
        return pattern.matcher(param);
    }

    public static Matcher findImages(String param) {
        return Pattern.compile("(img).*?(\\([^)]+\\))").matcher(param);
    }

    /**
     * Removes trailing white spaces from a given string and returns the
     * mutated string object.
     *
     * Deprecated please use the String.trim() method to
     * achieve the same result:
     * @see String#trim()
     * */
    @Deprecated
    public static String removeTrailingWhiteSpaces(String param) {
        return Pattern.compile(PATTERN_TRAILING_SPACES).matcher(param).replaceAll("");
    }

    static String findUserTags(String text) {
        if(text == null)
            return null;
        Matcher matcher = Pattern.compile(PATTERN_USER_TAGS).matcher(text);
        while (matcher.find()) {
            String match = matcher.group();
            text = text.replace(match, String.format(USER_URL_LINK, match, match.replace("@","")));
        }
        return text;
    }

    /**
     * Returns either an Id of anime listing or user name
     */
    public static @Nullable Matcher findIntentKeys(String path) {
        Matcher deepLinkMatcher = Pattern.compile(PATTERN_DEEP_LINKS).matcher(path);
        if(deepLinkMatcher.find())
            return deepLinkMatcher;
        return null;
    }

    /**
     * Builds a full youtube link from either an id or a valid youtube link
     */
    public static String buildYoutube(String id) {
        if(!id.contains("youtube")) {
            if (id.contains(YoutubeShort))
                return Youtube + id.replace(YoutubeShort, "");
            return Youtube + id;
        }
        return id;
    }

    public static String createYoutubeStandard(String link) {
        if(!link.contains("youtube"))
            if (link.contains(YoutubeShort))
                return String.format("%s(%s)", KEY_YOU, link.replace(YoutubeShort, ""));
        return String.format("%s(%s)", KEY_YOU, link);
    }

    public static String createLinkStandard(String link) {
        return String.format("[%s](%s)", link, link) ;
    }

    public static String createWebMStandard(String link) {
        return String.format("%s(%s)", KEY_WEB, link) ;
    }

    public static String createImageStandard(String link) {
        return String.format("%s(%s)", KEY_IMG, link) ;
    }

    /**
     * Get the thumbnail image of a youtube video
     * </br>
     *
     * @param link full youtube link
     */
    public static String getYoutubeThumb(String link) {
        Matcher matcher = Pattern.compile(PATTERN_YOUTUBE_EXTRACT).matcher(link);
        String temp;

        if(matcher.find())
            temp = matcher.group(matcher.groupCount());
        else
            temp = NO_THUMBNAIL;
        return String.format(VID_THUMB, temp);
    }

    public static String removeTags(String value) {
        return findImages(findMedia(value).replaceAll("")).replaceAll("")
                .replaceAll("!~", "").replaceAll("~!", "")
                .replaceAll("~", "");
    }

    public static String convertToStandardMarkdown(String value) {
        if(value != null) {
            Matcher matcher = findImages(value);
            String TAG = "![image]";
            while (matcher.find()) {
                String match = matcher.group();
                int group = matcher.groupCount();
                value = value.replace(match, TAG + matcher.group(group));
            }
            return value.replaceAll("!~", "").replaceAll("~!", "").replaceAll("~", "");
        } else
            return "N/A";
    }
}
