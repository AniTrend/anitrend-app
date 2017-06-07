package com.mxt.anitrend.util;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Maxwell on 12/21/2015.
 */
public final class ImeAction
{
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }
}
