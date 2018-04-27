package com.mxt.anitrend.util;

import android.text.Editable;
import android.text.Html;
import org.xml.sax.XMLReader;

/**
 * Helps us handle custom tags which may not be supported
 */
public class HtmlTagHandler implements Html.TagHandler {

    public static HtmlTagHandler create() {
        return new HtmlTagHandler();
    }

    private HtmlTagHandler() {

    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

    }
}
