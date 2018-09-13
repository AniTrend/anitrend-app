package com.mxt.anitrend.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import com.github.rjeschke.txtmark.Processor;

/**
 * Created by max on 2017/03/26.
 * Moved markdown processor to global location
 */
public final class MarkDownUtil {

    private static SpannableStringBuilder fromMD(@NonNull String content) {
        Spanned htmlConverted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlConverted = Html.fromHtml(Processor.process(content),
                    Html.FROM_HTML_MODE_LEGACY);
        else
            htmlConverted = Html.fromHtml(Processor.process(content));
        return (SpannableStringBuilder) htmlConverted;
    }

    public static Spanned convert(@Nullable String input) {
        SpannableStringBuilder result;
        if(TextUtils.isEmpty(input))
            result = fromMD("<b>No content available</b>");
        else
            result = fromMD(RegexUtil.findUserTags(input));

        try {
            if(result.length() > 0)
                while (result.charAt(result.length() - 1) == '\n')
                    result = result.delete(result.length() - 1, result.length());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("convert(input)", e.getMessage());
        }

        return result;
    }

    public static Spanned convert(@Nullable String input, Context context, AppCompatTextView source) {
        SpannableStringBuilder result;
        if(TextUtils.isEmpty(input))
            result = fromMD("<b>No content available</b>");
        else
            result = fromMD(RegexUtil.findUserTags(input));
            // result = fromMD(RegexUtil.findUserTags(input), context, source);

        try {
            if(result.length() > 0)
                while (result.charAt(result.length() - 1) == '\n')
                    result = result.delete(result.length() - 1, result.length());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("convert(input...)", e.getMessage());
        }

        return result;
    }

    static String convertLink(Editable text) {
        return convertLink(text.toString());
    }

    static String convertImage(Editable text) {
        return convertImage(text.toString());
    }

    static String convertYoutube(Editable text) {
        return convertYoutube(text.toString());
    }

    static String convertVideo(Editable text) {
        return convertVideo(text.toString());
    }

    public static String convertLink(String text) {
        return RegexUtil.createLinkStandard(text);
    }

    public static String convertImage(String text) {
        return RegexUtil.createImageStandard(text);
    }

    public static String convertYoutube(String text) {
        return RegexUtil.createYoutubeStandard(text);
    }

    public static String convertVideo(String text) {
        return RegexUtil.createWebMStandard(text);
    }
}
