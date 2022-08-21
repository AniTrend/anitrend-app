package com.mxt.anitrend.util.markdown

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by max on 2017/04/08.
 * Pattern matcher uses regex to return possible matches for media types
 */
object RegexUtil {

    const val KEY_IMG = "img"
    const val KEY_WEB = "webm"
    const val KEY_YOU = "youtube"
    const val NO_THUMBNAIL = "http://placehold.it/1280x720?text=No+Preview+Available"

    private const val VID_THUMB = "https://img.youtube.com/vi/%s/hqdefault.jpg"
    private const val Youtube = "https://www.youtube.com/watch?v="
    private const val YoutubeShort = "https://youtu.be/"
    private const val PATTERN_YOUTUBE_EXTRACT = "(\\?v=)(.*)"

    private val pattern: Pattern
    private const val PATTERN_MEDIA = "(img|webm|youtube).*?(\\([^)]+\\))"
    private const val PATTERN_DEEP_LINKS = "(activity|user|manga|anime|character|actor|staff|studio)\\/(.*)"

    private const val USER_URL_LINK = "__[%s](https://anilist.co/user/%s)__"
    private const val PATTERN_USER_TAGS = "(@[A-Za-z]\\w+)"

    private const val PATTERN_TRAILING_SPACES = "(^[\\r\\n]+|[\\r\\n]+$)"


    init {
        pattern = Pattern.compile(PATTERN_MEDIA, Pattern.CASE_INSENSITIVE)
    }

    /**
     * finds images and youtube videos
     */
    fun findMedia(param: String): Matcher {
        return pattern.matcher(param)
    }

    private fun findImages(param: String): Matcher {
        return Pattern.compile("(img|Img|IMG).*?(\\([^)]+\\))", Pattern.CASE_INSENSITIVE).matcher(param)
    }

    /**
     * Removes trailing white spaces from a given string and returns the
     * mutated string object.
     *
     * Deprecated please use the String.trim() method to
     * achieve the same result:
     * @see String.trim
     */
    @Deprecated("")
    fun removeTrailingWhiteSpaces(param: String): String? {
        return if (param.isBlank()) null else Pattern.compile(PATTERN_TRAILING_SPACES).matcher(param).replaceAll("")
    }

    fun findUserTags(text: String?): String {
        var newText = text
        if (newText.isNullOrBlank())
            return "<b>No content available</b>"
        val matcher = Pattern.compile(PATTERN_USER_TAGS).matcher(newText)
        while (matcher.find()) {
            val match = matcher.group()
            val replacement = String.format(
                USER_URL_LINK, match,
                    match.replace("@", "")
            )
            if (newText?.contains(replacement, ignoreCase = false) == true)
                continue
            newText = newText?.replace(match, replacement)
        }
        return newText ?: "<b>No content available</b>"
    }

    /**
     * Returns either an Id of anime listing or user name
     */
    fun findIntentKeys(path: String?): Matcher? {
        if (path != null) {
            val deepLinkMatcher = Pattern.compile(PATTERN_DEEP_LINKS).matcher(path)
            return if (deepLinkMatcher.find()) deepLinkMatcher else null
        }
        return null
    }

    /**
     * Builds a full youtube link from either an id or a valid youtube link
     */
    fun buildYoutube(id: String): String {
        return if (!id.contains("youtube")) {
            if (id.contains(YoutubeShort)) Youtube + id.replace(
                YoutubeShort, "") else Youtube + id
        } else id
    }

    fun createYoutubeStandard(link: String): String {
        if (!link.contains("youtube"))
            if (link.contains(YoutubeShort))
                return String.format("%s(%s)",
                    KEY_YOU, link.replace(YoutubeShort, ""))
        return String.format("%s(%s)", KEY_YOU, link)
    }

    fun createLinkStandard(link: String): String {
        return String.format("[%s](%s)", link, link)
    }

    fun createWebMStandard(link: String): String {
        return String.format("%s(%s)", KEY_WEB, link)
    }

    fun createImageStandard(link: String): String {
        return String.format("%s250(%s)", KEY_IMG, link)
    }

    /**
     * Get the thumbnail image of a youtube video
     *
     *
     * @param link full youtube link
     */
    fun getYoutubeThumb(link: String): String {
        val matcher = Pattern.compile(PATTERN_YOUTUBE_EXTRACT).matcher(link)
        val temp: String?

        if (matcher.find())
            temp = matcher.group(matcher.groupCount())
        else
            return NO_THUMBNAIL
        return String.format(VID_THUMB, temp)
    }

    fun removeTags(value: String?): String? {
        return when (value.isNullOrBlank()) {
            true -> null
            else -> findImages(
                findMedia(value)
                    .replaceAll("")
            )
                    .replaceAll("")
                    .replace("!~","")
                    .replace("~!","")
                    .replace("~","")
        }
    }

    fun convertToStandardMarkdown(value: String?): String {
        var substitute = "<b>No content available</b>"
        return value?.let {
            if (!it.isBlank()) {
                substitute = value/*
                        .replace("!~","")
                        .replace("~!","")
                        .replace("~","")*/

                val matcher = findMedia(it)

                while (matcher.find()) {
                    val gc = matcher.groupCount()
                    val match = matcher.group() // the full match e.g. img%(http://git.raw.sample.jpg)
                    val tag = matcher.group(gc - 1) // returns the first match group tag of the regex match img|IMG|Img
                    val media = matcher.group(gc) // contains the second match group e.g. (http://git.raw.sample.jpg) brackets included
                    val mediaWithoutBrackets = media?.removeSurrounding("(", ")")
                    if (tag != null) {
                        substitute = when (tag.lowercase(Locale.getDefault())) {
                            KEY_IMG ->
                                substitute.replace(match, "![image]$media")
                            KEY_WEB ->
                                substitute.replace(match, "<a href=\"$mediaWithoutBrackets\"><img src=\"$NO_THUMBNAIL\"/></a>")
                            KEY_YOU ->
                                substitute.replace(match, "<a href=\"$mediaWithoutBrackets\"><img src=\"${VID_THUMB.format(mediaWithoutBrackets)}\"/></a>")
                            else -> substitute
                        }
                    }

                }
            }
            substitute
        } ?: substitute
    }
}
