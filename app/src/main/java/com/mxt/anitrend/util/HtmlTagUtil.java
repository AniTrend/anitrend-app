package com.mxt.anitrend.util;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

/**
 * created by max
 * Helps us handle custom tags which may not be supported
 */
public class HtmlTagUtil implements Html.TagHandler {

    public static HtmlTagUtil create() {
        return new HtmlTagUtil();
    }

    private HtmlTagUtil() {

    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

    }
}
