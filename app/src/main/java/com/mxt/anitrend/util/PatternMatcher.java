package com.mxt.anitrend.util;

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
    public static final String NO_THUMBNAIL = "http://placehold.it/450x250?text=No+Preview+Available";


    private static final String VID_THUMB = "https://img.youtube.com/vi/%s/hqdefault.jpg";
    private static final String Youtube = "https://www.youtube.com/watch?v=";
    private static final String PATTERN_YOUTUBE_EXTRACT = "(\\?v=)(.*)";

    private static final Pattern pattern;
    private static final String PATTERN_MEDIA = "(img|webm|youtube).*?(\\([^)]+\\))";
    private static final String PATTERN_DEEP_LINKS = "(user|manga|anime)\\/(.*)";

    private static final String USER_URL_LINK = "__[%s](https://anilist.co/user/%s)__";
    private static final String PATTERN_USER_TAGS = "(@[A-Za-z]\\w+)";


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

    static String findUserTags(String text) {
        if(text == null)
            return text;
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
    public static String findIntentKey(String path) {
        Matcher matcher = Pattern.compile(PATTERN_DEEP_LINKS).matcher(path);
        if(matcher.find()) {
            int mCount = matcher.groupCount();
            if(mCount < 2)
                return null;
            try {
                String last = matcher.group(mCount);
                if(last.contains("/"))
                    return last.split("/")[0];
                else
                    return last;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Builds a full youtube link from either an id or a valid youtube link
     */
    public static String buildYoutube(String id) {
        if(!id.contains("youtube")) {
            if (id.contains("youtu.be"))
                return Youtube + id.replace("https://youtu.be/", "");
            return Youtube + id;
        }
        return id;
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
        return findImages(findMedia(value).replaceAll("")).replaceAll("").replaceAll("~","");
    }

    public static String convertToStandardMarkdown(String value) {
        Matcher matcher = findImages(value);
        String TAG = "![image]";
        while (matcher.find()) {
            String match = matcher.group();
            int group = matcher.groupCount();
            value = value.replace(match, TAG + matcher.group(group));
        }
        return value.replaceAll("~","").replaceAll("!~","").replaceAll("~!","");
    }

    public static String convertToStandardMarkdownEditor(String value) {
        Matcher matcher = findImages(value);
        String TAG = "![image]";
        while (matcher.find()) {
            String match = matcher.group();
            int group = matcher.groupCount();
            value = value.replace(match, TAG + matcher.group(group));
        }
        return value;
    }

    public static String stripBloat(String value) {
        value = value.replaceAll("(_(und|jpn)_[0-9])|(\\(.*?\\))|(_\\(.*?\\)_)","").replaceAll("_"," ").replaceAll("(\\[.*?\\])|(.\\[.*?\\])","");
        if(value.startsWith(" "))
            value = value.replaceFirst(" ","");
        return value;
    }
}
