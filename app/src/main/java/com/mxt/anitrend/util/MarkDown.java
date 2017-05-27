package com.mxt.anitrend.util;

import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.github.rjeschke.txtmark.Processor;

/**
 * Created by max on 2017/03/26.
 * Moved markdown processor to global location
 */
public final class MarkDown {

    private static SpannableStringBuilder fromMD(String content) {
        if(content == null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return (SpannableStringBuilder) Html.fromHtml("<b>No content available</b>", Html.FROM_HTML_MODE_COMPACT | Html.FROM_HTML_OPTION_USE_CSS_COLORS | Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST);
            else return (SpannableStringBuilder) Html.fromHtml("<b>No content available</b>");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return (SpannableStringBuilder) Html.fromHtml(Processor.process(content), Html.FROM_HTML_MODE_COMPACT | Html.FROM_HTML_OPTION_USE_CSS_COLORS | Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST);
        else return (SpannableStringBuilder) Html.fromHtml(Processor.process(content));
    }

    public static Spanned convert(String input) {
        SpannableStringBuilder spanned = fromMD(PatternMatcher.findUserTags(input));

        if(input != null && !input.isEmpty())
            while (spanned.charAt(spanned.length() - 1) == '\n')
                spanned = spanned.delete(spanned.length() - 1, spanned.length());

        return spanned;
    }

    public static String convertLink(Editable text) {
        return String.format("[%s](%s)", text, text);
    }

    public static String convertImage(Editable text) {
        return String.format("img220(%s)", text);
    }

    public static String convertYoutube(Editable text) {
        return String.format("youtube(%s)", text);
    }

    public static String convertVideo(Editable text) {
        return String.format("webm(%s)", text);
    }
}
