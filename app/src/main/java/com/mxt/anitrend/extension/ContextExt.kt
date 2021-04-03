package com.mxt.anitrend.extension

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.mxt.anitrend.R
import timber.log.Timber
import java.io.File

/**
 * Exactly whether a device is low-RAM is ultimately up to the device configuration, but currently
 * it generally means something in the class of a 512MB device with about a 800x480 or less screen.
 * This is mostly intended to be used by apps to determine whether they should
 * turn off certain features that require more RAM.
 *
 * @return true if this is a low-RAM device.
 */
fun Context?.isLowRamDevice() = this?.let {
    val activityManager = it.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return ActivityManagerCompat.isLowRamDevice(activityManager)
} ?: false

/**
 * Start a new activity from context and avoid potential crashes from early API levels
 */
inline fun <reified T> Context?.startNewActivity(params: Bundle? = null) {
    try {
        val intent = Intent(this, T::class.java)
        with (intent) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            params?.also { putExtras(it) }
        }
        this?.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

/**
 * Creates a list of the array resource given
 *
 * @return The string list associated with the resource.
 * @throws Exception if the given ID does not exist.
 */
fun Context.getStringList(@ArrayRes arrayRes : Int): List<String> {
    val array = resources.getStringArray(arrayRes)
    return array.toList()
}

fun View.getLayoutInflater(): LayoutInflater =
    context.getLayoutInflater()

fun Context.getLayoutInflater(): LayoutInflater =
    getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

/**
 * Gets the size of the display, in pixels. Value returned by this method does
 * not necessarily represent the actual raw size (native resolution) of the display.
 *
 * @return A Point object to with the size information.
 * @see Point
 */
fun Context.getScreenDimens(): Point {
    val deviceDimens = Point()
    (getSystemService(Context.WINDOW_SERVICE) as WindowManager).apply {
        defaultDisplay?.getSize(deviceDimens)
    }
    return deviceDimens
}

/**
 * Creates a drawable from the given attribute resource which cannot be nullable type
 *
 * @param drawableAttr attribute resource for drawable
 * @return Drawable for the attribute, or null if not defined.
 * @throws UnsupportedOperationException if the attribute is defined but is
 *         not a color or drawable resource.
 */
fun Context.getDrawableFromAttr(@AttrRes drawableAttr : Int): Drawable? {
    val drawableAttribute = obtainStyledAttributes(intArrayOf(drawableAttr))
    val drawable = drawableAttribute.getDrawable(0)
    drawableAttribute.recycle()
    return drawable
}

/**
 * Creates a color from the given attribute, If the attribute references a color resource holding a complex
 * @link{android.content.res.ColorStateList}, then the default color from the set is returned.
 *
 * @param colorAttr attribute resource for color
 * @return Attribute color value, or defValue if not defined.
 * @throws UnsupportedOperationException if the attribute is defined but is
 *         not a color or drawable resource.
 */
fun Context.getCompatColorAttr(@AttrRes colorAttr : Int, defaultColor : Int = 0): Int {
    val colorAttribute = obtainStyledAttributes(intArrayOf(colorAttr))
    @ColorInt val color = colorAttribute.getColor(0, defaultColor)
    colorAttribute.recycle()
    return color
}

/**
 * Starting in android Marshmallow, the returned
 * color will be styled for the specified Context's theme.
 *
 * @see android.os.Build.VERSION_CODES.M
 * @return A single color value in the form 0xAARRGGBB.
 */
fun Context.getCompatColor(@ColorRes colorRes: Int) =
    ContextCompat.getColor(this, colorRes)

/**
 * Avoids resource not found when using vector drawables in API levels < Lollipop
 *
 * This method supports inflation of {@code <vector>}, {@code <animated-vector>} and
 * {@code <animated-selector>} resources on devices where platform support is not available.
 *
 * @param resource The resource id of the drawable or vector drawable
 *                 @see DrawableRes
 *
 * @return Drawable An object that can be used to draw this resource.
 * @see Drawable
 */
fun Context.getCompatDrawable(@DrawableRes resource : Int) =
    AppCompatResources.getDrawable(this, resource)

/**
 * Avoids resource not found when using vector drawables in API levels < Lollipop
 * Also images loaded from this method apply the {@link Drawable#mutate()} to assure
 * that the state of each drawable is not shared
 *
 * @param resource The resource id of the drawable or vector drawable
 * @param tintColor A specific color to tint the drawable
 * @return Drawable tinted with the tint color
 */
fun Context.getCompatDrawable(@DrawableRes resource : Int, @ColorRes tintColor : Int): Drawable? {
    val drawableResource = AppCompatResources.getDrawable(this, resource)
    if (drawableResource != null) {
        val drawableResult = DrawableCompat.wrap(drawableResource).mutate()
        if (tintColor != 0)
            DrawableCompat.setTint(drawableResult, getCompatColor(tintColor))
        return drawableResource
    }
    return null
}

/**
 * Avoids resource not found when using vector drawables in API levels < Lollipop
 * and tints the drawable depending on the current selected theme, images loaded
 * from this method apply the {@link Drawable#mutate()} to assure that the state
 * of each drawable is not shared
 *
 * @param resource The resource id of the drawable or vector drawable
 * @param colorAttr A specific color to tint the drawable
 * @return Drawable tinted with the tint color
 */
fun Context.getCompatTintedDrawable(@DrawableRes resource : Int, @AttrRes colorAttr : Int = R.attr.titleColor): Drawable? {
    val originalDrawable = getCompatDrawable(resource)
    var drawable : Drawable? = null
    if (originalDrawable != null) {
        drawable = DrawableCompat.wrap(originalDrawable).mutate()
        DrawableCompat.setTint(drawable, getCompatColorAttr(colorAttr))
    }
    return drawable
}

fun Context.logDirectory(): File {
    val external = File(getExternalFilesDir(null) ?: filesDir, "logs")
    if (!external.exists()) external.mkdirs()
    return external
}

fun Context.logFile(): File {
    val log = File(logDirectory(), "${packageName}.log")
    if (!log.exists()) log.mkdirs()
    return log
}