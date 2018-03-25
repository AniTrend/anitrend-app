package com.mxt.anitrend.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.Cache;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by max on 2017/09/16.
 * Utility class that contains helpful functions
 */
public class CompatUtil {

    private static final int CACHE_LIMIT = 1024 * 1024 * 250;

    public static void hideKeyboard(FragmentActivity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connectivityManager != null)
            networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public static Cache cacheProvider(Context context) {
        Cache cache = null;
        try {
            cache = new Cache(new File(context.getCacheDir(), "response-cache"), CACHE_LIMIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cache;
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     *
     * @param resource The resource id of the drawable or vector drawable
     *                 @see DrawableRes
     *
     * @param context Any valid application context
     *                @see Context
     *
     * @return Drawable
     * @see Drawable
     */
    public static Drawable getDrawable(Context context, @DrawableRes int resource) {
        return AppCompatResources.getDrawable(context, resource);
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     * and tints the drawable depending on the current selected theme, images loaded
     * from this method apply the {@link Drawable#mutate()} to assure that the state
     * of each drawable is not shared
     *
     * @param resource The resource id of the drawable or vector drawable
     *                 @see DrawableRes
     *
     * @param context Any valid application context
     *                @see Context
     *
     * @return Drawable
     * @see Drawable
     */
    public static Drawable getTintedDrawable(Context context, @DrawableRes int resource) {
        Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, resource)).mutate();
        DrawableCompat.setTint(drawable, getColorFromAttr(context, R.attr.titleColor));
        return drawable;
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     * Also images loaded from this method apply the {@link Drawable#mutate()} to assure
     * that the state of each drawable is not shared
     *
     * @param resource The resource id of the drawable or vector drawable
     *                 @see DrawableRes
     *
     * @param context Any valid application context
     *                @see Context
     *
     * @param tint A specific color to tint the drawable
     *
     * @return Drawable
     * @see Drawable
     */
    public static Drawable getDrawable(Context context, @DrawableRes int resource, @ColorRes int tint) {
        Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, resource)).mutate();
        if(tint != 0)
            DrawableCompat.setTint(drawable, getColor(context, tint));
        return drawable;
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     * Also images loaded from this method apply the {@link Drawable#mutate()} to assure
     * that the state of each drawable is not shared
     *
     * @param resource The resource id of the drawable or vector drawable
     *                 @see DrawableRes
     *
     * @param context Any valid application context
     *                @see Context
     *
     * @param attribute Type of attribute resource
     *
     * @return Drawable
     * @see Drawable
     */
    public static Drawable getDrawableTintAttr(Context context, @DrawableRes int resource, @AttrRes int attribute) {
        Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, resource)).mutate();
        DrawableCompat.setTint(drawable, getColorFromAttr(context, attribute));
        return drawable;
    }

    /**
     * Returns a color from a defined attribute
     *
     * @param context Any valid application context
     *                @see Context
     *
     * @param attribute Type of attribute resource
     *
     * @return Color Integer
     */
    public static @ColorInt int getColorFromAttr(Context context, @AttrRes int attribute) {
        TypedArray colorAttribute = context.obtainStyledAttributes(new int[] {attribute});
        @ColorInt int color = colorAttribute.getColor(0, 0);
        colorAttribute.recycle();
        return color;
    }

    /**
     * Get screen dimensions for the current device configuration
     */
    public static void getScreenDimens(Point deviceDimens, Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if(windowManager != null)
            windowManager.getDefaultDisplay().getSize(deviceDimens);
    }

    /**
     * Starts a shared transition of activities connected by views
     * <br/>
     *
     * @param base The calling activity
     * @param target The view from the calling activity with transition name
     * @param data Intent with bundle and or activity to start
     */
    public static void startSharedTransitionActivity(FragmentActivity base, View target, Intent data) {
        Pair participants = new Pair<>(target, ViewCompat.getTransitionName(target));
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat
                .makeSceneTransitionAnimation(base, participants);
        ActivityCompat.startActivity(base, data, transitionActivityOptions.toBundle());
    }

    /**
     * Starts a shared transition of activities connected by views
     * by making use of the provided transition name
     * <br/>
     *
     * @param base The calling activity
     * @param target The view from the calling activity with transition name
     * @param data Intent with bundle and or activity to start
     */
    public static void startSharedImageTransition(FragmentActivity base, View target, Intent data, @StringRes int transitionName) {
        ViewCompat.setTransitionName(target, base.getString(transitionName));
        ActivityOptionsCompat transition = ActivityOptionsCompat.makeSceneTransitionAnimation(base, target, ViewCompat.getTransitionName(target));
        base.startActivity(data, transition.toBundle());
    }

    /**
     * Starts a reveal animation for a target view from an activity implementation
     *
     * @param activity Typically a fragment activity descendant
     * @param target View which the reveal transition show be anchored to
     * @param finish true to allow the calling activity to be finished
     * @param data Intent data for the target activity to receive
     */
    public static void startRevealAnim(final FragmentActivity activity, View target, final Intent data, final boolean finish) {
        CircularAnim.fullActivity(activity, target)
                .colorOrImageRes(isLightTheme(activity)?R.color.colorPrimaryDark:R.color.colorDarker)
                .go(() -> { activity.startActivity(data);  if(finish) activity.finish(); });
    }

    /**
     * Starts a reveal animation for a target view from an activity without
     * closing the calling activity
     *
     * @param activity Typically a fragment activity descendant
     * @param target View which the reveal transition show be anchored to
     * @param data Intent data for the target activity to receive
     */
    public static void startRevealAnim(FragmentActivity activity, View target, Intent data) {
        startRevealAnim(activity, target, data, false);
    }

    public static boolean isLightTheme(@StyleRes int theme) {
        return theme == R.style.AppThemeLight;
    }

    public static boolean isLightTheme(Context context) {
        return new ApplicationPref(context).getTheme() == R.style.AppThemeLight;
    }

    public static int dipToPx(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int pxToDip(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int spToPx(float spValue) {
        float scaledDensity = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return Math.round(spValue * scaledDensity);
    }

    /**
     * Return true if the smallest width in DP of the device is equal or greater than the given
     * value.
     */
    public static boolean isScreenSw(int swDp) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float widthDp = displayMetrics.widthPixels / displayMetrics.density;
        float heightDp = displayMetrics.heightPixels / displayMetrics.density;
        float screenSw = Math.min(widthDp, heightDp);
        return screenSw >= swDp;
    }

    /**
     * Return true if the width in DP of the device is equal or greater than the given value
     */
    public static boolean isScreenW(int widthDp) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels / displayMetrics.density;
        return screenWidth >= widthDp;
    }

    public static int getColor(Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    public static LayoutInflater getLayoutInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Credits
     * @author hamakn
     * https://gist.github.com/hamakn/8939eb68a920a6d7a498
     * */
    public static int getStatusBarHeight(Resources resources) {
        int statusBarHeight = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        return statusBarHeight;
    }

    /**
     * Credits
     * @author hamakn
     * https://gist.github.com/hamakn/8939eb68a920a6d7a498
     * */
    public static int getActionBarHeight(FragmentActivity fragmentActivity) {
        int actionBarHeight = 0;
        final TypedArray styledAttributes = fragmentActivity.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize }
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarHeight;
    }


    /**
     * Credits
     * @author hamakn
     * https://gist.github.com/hamakn/8939eb68a920a6d7a498
     * */
    public static int getNavigationBarHeight(Resources resources) {
        int navigationBarHeight = 0;
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0)
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        return navigationBarHeight;
    }

    /**
     * Configure our swipe refresh layout
     */
    public static void configureSwipeRefreshLayout(CustomSwipeRefreshLayout swipeRefreshLayout, FragmentActivity fragmentActivity) {
        swipeRefreshLayout.setDragTriggerDistance(CustomSwipeRefreshLayout.DIRECTION_BOTTOM, (getNavigationBarHeight(fragmentActivity.getResources()) + dipToPx(16f)));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getColorFromAttr(fragmentActivity, R.attr.rootColor));
        swipeRefreshLayout.setColorSchemeColors(getColorFromAttr(fragmentActivity, R.attr.contentColor));
        swipeRefreshLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setPermitRefresh(true);
        swipeRefreshLayout.setPermitLoad(false);
    }

    /**
     * Get List from a given array res
     * @return list of the array
     */
    public static List<String> getStringList(Context context, @ArrayRes int arrayRes) {
        String[] array = context.getResources().getStringArray(arrayRes);
        return getListFromArray(array);
    }

    /**
     * Get List from a given array type
     * @return list of the array
     */
    @SafeVarargs
    public static <T> List<T> getListFromArray(T... array) {
        return Arrays.asList(array);
    }

    /**
     * Gets the index of any type of collection guaranteed that an equals override for the class
     * of type T is implemented.
     *
     * @param collection the child collection item to search
     * @param target the item to search
     * @return 0 if no result was found
     */
    public static <T> int getIndexOf(Collection<T> collection, T target) {
        if(collection != null && target != null) {
            Optional<IntPair<T>> pairOptional = Stream.of(collection)
                    .findIndexed(((index, value) -> value != null && value.equals(target)));
            if (pairOptional.isPresent())
                return pairOptional.get().getFirst();
        }
        return 0;
    }

    /**
     * Gets the index of any type of collection guaranteed that an equals override for the class
     * of type T is implemented.
     *
     * @param collection the child collection item to search
     * @param target the item to search
     * @return 0 if no result was found
     */
    public static <T> int getIndexOf(T[] collection, T target) {
        if(collection != null && target != null) {
            Optional<IntPair<T>> pairOptional = Stream.of(collection)
                    .findIndexed(((index, value) -> value != null && value.equals(target)));
            if (pairOptional.isPresent())
                return pairOptional.get().getFirst();
        }
        return 0;
    }

    /**
     * Gets the index of any type of collection guaranteed that an
     * equals override for the class of type T is implemented.
     * <br/>
     * @see Object#equals(Object)
     *
     * @param collection the child collection item to search
     * @param target the item to search
     * @return Optional result object
     * <br/>
     *
     * @see Optional<T> for information on how to handle return
     * @see IntPair
     */
    public static <T> Optional<IntPair<T>> findIndexOf(Collection<T> collection, T target) {
        if(collection != null && target != null)
            return Stream.of(collection)
                    .findIndexed((index, value) -> value != null &&  value.equals(target));
        return Optional.empty();
    }

    /**
     * Gets the index of any type of collection guaranteed that an
     * equals override for the class of type T is implemented.
     * <br/>
     * @see Object#equals(Object)
     *
     * @param collection the child collection item to search
     * @param target the item to search
     * @return Optional result object
     * <br/>
     *
     * @see Optional<T> for information on how to handle return
     * @see IntPair
     */
    public static <T> Optional<IntPair<T>> findIndexOf(T[] collection, T target) {
        if(collection != null && target != null)
            return Stream.of(collection)
                    .findIndexed((index, value) ->value != null &&  value.equals(target));
        return Optional.empty();
    }


    /**
     * Sorts a given map by the order of the of the keys in the map in descending order
     * @see ComparatorProvider#getKeyComparator() for logic on how comparator handles this
     */
    public static <T> List<Map.Entry<String, T>> getKeyFilteredMap(Map<String, T> map) {
        return Stream.of(map).sorted(ComparatorProvider.getKeyComparator()).toList();
    }

    public static boolean isLowRamDevice(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean isLowRamDevice = false;
        if (activityManager != null)
            isLowRamDevice = ActivityManagerCompat.isLowRamDevice(activityManager);
        return isLowRamDevice;
    }

    /**
     * Capitalize words for text view consumption
     */
    public static String capitalizeWords(String input) {
        if(!TextUtils.isEmpty(input)) {
            StringBuilder result = new StringBuilder(input.length());
            String[] words = input.split(" ");
            int index = 0;
            for (String word: words) {
                index++;
                char starting = Character.toUpperCase(word.charAt(0));
                result.append(starting).append(word.substring(1));
                if(index != word.length() -1)
                    result.append(" ");
            }
            return result.toString();
        }
        return "";
    }

    public static <T extends Collection> boolean isEmpty(T collection) {
        return collection == null || collection.isEmpty();
    }
}