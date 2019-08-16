package com.mxt.anitrend.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.annotation.*
import androidx.core.app.ActivityManagerCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.appcompat.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.annimon.stream.IntPair
import com.annimon.stream.Optional
import com.annimon.stream.Stream
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity
import okhttp3.Cache
import java.io.File
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Created by max on 2017/09/16.
 * Utility class that contains helpful functions
 */
object CompatUtil {

    private const val CACHE_LIMIT = 1024 * 1024 * 250

    fun hideKeyboard(activity: FragmentActivity?) {
            val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager?.hideSoftInputFromWindow(activity?.window?.decorView?.windowToken, 0)
    }

    @Suppress("DEPRECATION")
    fun isOnline(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo: NetworkInfo? = connectivityManager?.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }

    fun cacheProvider(context: Context): Cache? {
        var cache: Cache? = null
        try {
            cache = Cache(File(context.cacheDir, "response-cache"), CACHE_LIMIT.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cache
    }

    fun imagePreview(activity: FragmentActivity?, view: View, imageUri: String?, errorMessage: Int) {
        if (!imageUri.isNullOrBlank()) {
            val intent = Intent(activity, ImagePreviewActivity::class.java)
            intent.putExtra(KeyUtil.arg_model, imageUri)
            startSharedImageTransition(activity, view, intent, R.string.transition_image_preview)
        } else {
            NotifyUtil.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     *
     * @param resource The resource id of the drawable or vector drawable
     * @see DrawableRes
     *
     *
     * @param context Any valid application context
     * @see Context
     *
     *
     * @return Drawable
     * @see Drawable
     */
    fun getDrawable(context: Context, @DrawableRes resource: Int): Drawable? {
        return AppCompatResources.getDrawable(context, resource)
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     * and tints the drawable depending on the current selected theme, images loaded
     * from this method apply the [Drawable.mutate] to assure that the state
     * of each drawable is not shared
     *
     * @param resource The resource id of the drawable or vector drawable
     * @see DrawableRes
     *
     *
     * @param context Any valid application context
     * @see Context
     *
     *
     * @return Drawable
     * @see Drawable
     */
    fun getTintedDrawable(context: Context, @DrawableRes resource: Int): Drawable {
        val drawable = DrawableCompat.wrap(Objects.requireNonNull<Drawable>(AppCompatResources.getDrawable(context, resource))).mutate()
        DrawableCompat.setTint(drawable, getColorFromAttr(context, R.attr.titleColor))
        return drawable
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     * Also images loaded from this method apply the [Drawable.mutate] to assure
     * that the state of each drawable is not shared
     *
     * @param resource The resource id of the drawable or vector drawable
     * @see DrawableRes
     *
     *
     * @param context Any valid application context
     * @see Context
     *
     *
     * @param tint A specific color to tint the drawable
     *
     * @return Drawable
     * @see Drawable
     */
    fun getDrawable(context: Context, @DrawableRes resource: Int, @ColorRes tint: Int): Drawable {
        val drawable = DrawableCompat.wrap(Objects.requireNonNull<Drawable>(AppCompatResources.getDrawable(context, resource))).mutate()
        if (tint != 0)
            DrawableCompat.setTint(drawable, getColor(context, tint))
        return drawable
    }

    /**
     * Avoids resource not found when using vector drawables in API levels < Lollipop
     * Also images loaded from this method apply the [Drawable.mutate] to assure
     * that the state of each drawable is not shared
     *
     * @param resource The resource id of the drawable or vector drawable
     * @see DrawableRes
     *
     *
     * @param context Any valid application context
     * @see Context
     *
     *
     * @param attribute Type of attribute resource
     *
     * @return Drawable
     * @see Drawable
     */
    fun getDrawableTintAttr(context: Context, @DrawableRes resource: Int, @AttrRes attribute: Int): Drawable {
        val drawable = DrawableCompat.wrap(Objects.requireNonNull<Drawable>(AppCompatResources.getDrawable(context, resource))).mutate()
        DrawableCompat.setTint(drawable, getColorFromAttr(context, attribute))
        return drawable
    }

    /**
     * Returns a color from a defined attribute
     *
     * @param context Any valid application context
     * @see Context
     *
     *
     * @param attribute Type of attribute resource
     *
     * @return Color Integer
     */
    @ColorInt
    fun getColorFromAttr(context: Context, @AttrRes attribute: Int): Int {
        val colorAttribute = context.obtainStyledAttributes(intArrayOf(attribute))
        @ColorInt val color = colorAttribute.getColor(0, 0)
        colorAttribute.recycle()
        return color
    }

    /**
     * Get screen dimensions for the current device configuration
     */
    fun getScreenDimens(deviceDimens: Point, context: Context?) {
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        windowManager?.defaultDisplay?.getSize(deviceDimens)
    }

    /**
     * Starts a shared transition of activities connected by views
     * <br></br>
     *
     * @param base The calling activity
     * @param target The view from the calling activity with transition name
     * @param data Intent with bundle and or activity to start
     */
    @Deprecated("")
    fun startSharedTransitionActivity(base: FragmentActivity?, target: View, data: Intent) {
        /*Pair participants = new Pair<>(target, ViewCompat.getTransitionName(target));
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat
                .makeSceneTransitionAnimation(base, participants);
        ActivityCompat.startActivity(base, data, transitionActivityOptions.toBundle());*/
        base?.startActivity(data)
    }

    /**
     * Starts a shared transition of activities connected by views
     * by making use of the provided transition name
     * <br></br>
     *
     * @param base The calling activity
     * @param target The view from the calling activity with transition name
     * @param data Intent with bundle and or activity to start
     */
    fun startSharedImageTransition(base: FragmentActivity?, target: View, data: Intent, @StringRes transitionName: Int) {
        ViewCompat.setTransitionName(target, Objects.requireNonNull<FragmentActivity>(base).getString(transitionName))
        val transition = ActivityOptionsCompat.makeSceneTransitionAnimation(base!!, target, ViewCompat.getTransitionName(target)!!)
        base.startActivity(data, transition.toBundle())
    }

    /**
     * Starts a reveal animation for a target view from an activity implementation
     *
     * @param activity Typically a fragment activity descendant
     * @param target View which the reveal transition show be anchored to
     * @param finish true to allow the calling activity to be finished
     * @param data Intent data for the target activity to receive
     */
    @Deprecated("Please use standard startActivity calls", level = DeprecationLevel.WARNING)
    @JvmOverloads
    fun startRevealAnim(activity: FragmentActivity?, target: View, data: Intent, finish: Boolean = false) {
        activity?.startActivity(data)
        if (finish)
            activity?.finish()
    }

    fun isLightTheme(@StyleRes theme: Int): Boolean {
        return theme == R.style.AppThemeLight
    }

    fun isLightTheme(context: Context): Boolean {
        return Settings(context).theme == R.style.AppThemeLight
    }

    fun dipToPx(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun pxToDip(pxValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun spToPx(spValue: Float): Int {
        val scaledDensity = Resources.getSystem().displayMetrics.scaledDensity
        return (spValue * scaledDensity).roundToInt()
    }

    /**
     * Return true if the smallest width in DP of the device is equal or greater than the given
     * value.
     */
    fun isScreenSw(swDp: Int): Boolean {
        val displayMetrics = Resources.getSystem().displayMetrics
        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        val heightDp = displayMetrics.heightPixels / displayMetrics.density
        val screenSw = min(widthDp, heightDp)
        return screenSw >= swDp
    }

    /**
     * Return true if the width in DP of the device is equal or greater than the given value
     */
    fun isScreenW(widthDp: Int): Boolean {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        return screenWidth >= widthDp
    }

    fun getColor(context: Context, @ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    fun getLayoutInflater(context: Context): LayoutInflater {
        return context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    /**
     * Credits
     * @author hamakn
     * https://gist.github.com/hamakn/8939eb68a920a6d7a498
     */
    fun getStatusBarHeight(resources: Resources): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        return statusBarHeight
    }

    /**
     * Credits
     * @author hamakn
     * https://gist.github.com/hamakn/8939eb68a920a6d7a498
     */
    fun getActionBarHeight(fragmentActivity: FragmentActivity?): Int? {
        val styledAttributes = fragmentActivity?.theme?.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize)
        )
        styledAttributes?.recycle()
        return styledAttributes?.getDimension(0, 0f)?.toInt()
    }


    /**
     * Credits
     * @author hamakn
     * https://gist.github.com/hamakn/8939eb68a920a6d7a498
     */
    private fun getNavigationBarHeight(resources: Resources): Int {
        var navigationBarHeight = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0)
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
        return navigationBarHeight
    }

    /**
     * Configure our swipe refresh layout
     */
    fun configureSwipeRefreshLayout(swipeRefreshLayout: CustomSwipeRefreshLayout, fragmentActivity: FragmentActivity?) {
        fragmentActivity?.also {
            swipeRefreshLayout.setDragTriggerDistance(CustomSwipeRefreshLayout.DIRECTION_BOTTOM, getNavigationBarHeight(it.resources) + dipToPx(16f))
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getColorFromAttr(it, R.attr.rootColor))
            swipeRefreshLayout.setColorSchemeColors(getColorFromAttr(it, R.attr.contentColor))
            swipeRefreshLayout.visibility = View.GONE
            swipeRefreshLayout.setPermitRefresh(true)
            swipeRefreshLayout.setPermitLoad(false)
        }
    }

    /**
     * Get List from a given array res
     * @return list of the array
     */
    fun getStringList(context: Context?, @ArrayRes arrayRes: Int): List<String> {
        val array = context?.resources?.getStringArray(arrayRes)
        if (array != null)
            return constructListFrom(*array)
        return Collections.emptyList()
    }

    /**
     * Get List from a given array type
     * @return list of the array
     */
    @SafeVarargs
    fun <T> constructListFrom(vararg array: T): List<T> =
            Arrays.asList(*array)


    /**
     * Gets the index of any type of collection guaranteed that an equals override for the class
     * of type T is implemented.
     *
     * @param collection the child collection item to search
     * @param target the item to search
     * @return 0 if no result was found
     */
    fun <T> getIndexOf(collection: Collection<T>?, target: T?): Int {
        if (collection != null && target != null) {
            val pairOptional = Stream.of(collection)
                    .findIndexed { _, value -> value != null && value == target }
            if (pairOptional.isPresent)
                return pairOptional.get().first
        }
        return 0
    }

    /**
     * Gets the index of any type of collection guaranteed that an equals override for the class
     * of type T is implemented.
     *
     * @param collection the child collection item to search
     * @param target the item to search
     * @return 0 if no result was found
     */
    fun <T> getIndexOf(collection: Array<T>?, target: T?): Int {
        if (collection != null && target != null) {
            val pairOptional = Stream.of(*collection)
                    .findIndexed { _, value -> value != null && value == target }
            if (pairOptional.isPresent)
                return pairOptional.get().first
        }
        return 0
    }

    /**
     * Gets the index of any type of collection guaranteed that an
     * equals override for the class of type T is implemented.
     * <br></br>
     * @see Object.equals
     * @param collection the child collection item to search
     * @param target the item to search
     * @return Optional result object
     * <br></br>
     *
     * @see Optional<T> for information on how to handle return
     *
     * @see IntPair
    </T> */
    fun <T> findIndexOf(collection: Collection<T>, target: T?): Optional<IntPair<T>> {
        return if (!isEmpty(collection) && target != null) Stream.of(collection)
                .findIndexed { _, value -> value != null && value == target } else Optional.empty()
    }

    /**
     * Gets the index of any type of collection guaranteed that an
     * equals override for the class of type T is implemented.
     * <br></br>
     * @see Object.equals
     * @param collection the child collection item to search
     * @param target the item to search
     * @return Optional result object
     * <br></br>
     *
     * @see Optional<T> for information on how to handle return
     *
     * @see IntPair
    </T> */
    fun <T> findIndexOf(collection: Array<T>?, target: T?): Optional<IntPair<T>> {
        return if (collection != null && target != null) Stream.of(*collection)
                .findIndexed { _, value -> value != null && value == target }
        else Optional.empty()
    }


    /**
     * Sorts a given map by the order of the of the keys in the map in descending order
     * @see ComparatorUtil.getKeyComparator
     */
    fun <T> getKeyFilteredMap(map: Map<String, T>): List<Map.Entry<String, T>> {
        return Stream.of(map).sorted(ComparatorUtil.getKeyComparator()).toList()
    }

    fun isLowRamDevice(context: Context?): Boolean {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManagerCompat.isLowRamDevice(activityManager)
    }

    /**
     * Capitalize words for text view consumption
     */
    fun capitalizeWords(input: String?): String {
        if (!input.isNullOrBlank()) {
            val exceptions = constructListFrom(KeyUtil.TV, KeyUtil.ONA, KeyUtil.OVA)
            val result = StringBuilder(input.length)
            val words = input.split("_|\\s".toRegex())
                    .dropLastWhile {
                        it.isEmpty()
                    }.toTypedArray()
            for ((index, word) in words.withIndex()) {
                if (word.isNotEmpty()) {
                    if (exceptions.contains(word))
                        result.append(word)
                    else {
                        val starting = Character.toUpperCase(word[0])
                        result.append(starting).append(word.substring(1).toLowerCase())
                    }
                }
                if (index != word.length - 1)
                    result.append(" ")
            }
            return result.toString()
        }
        return ""
    }

    /**
     * Get a list from a given array of strings
     * @return list of capitalized strings
     */
    fun capitalizeWords(strings: Array<String>): List<String> {
        return strings.map { capitalizeWords(it)  }
    }

    fun <T : Collection<*>> isEmpty(collection: T?): Boolean {
        return collection == null || collection.isEmpty()
    }

    fun <T : Collection<*>> sizeOf(collection: T?): Int {
        return if (isEmpty(collection)) 0 else collection!!.size
    }

    fun equals(a: Any?, b: Any): Boolean {
        return a != null && a == b
    }
}