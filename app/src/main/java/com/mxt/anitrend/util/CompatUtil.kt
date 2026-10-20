package com.mxt.anitrend.util

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import com.mxt.anitrend.util.collection.ComparatorUtil
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity
import okhttp3.Cache
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by max on 2017/09/16.
 * Utility class that contains helpful functions
 */
object CompatUtil {

    private const val CACHE_LIMIT = 1024 * 1024 * 250

    fun isOnline(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun cacheProvider(context: Context): Cache? {
        var cache: Cache? = null
        try {
            cache = Cache(File(context.cacheDir, "response-cache"), CACHE_LIMIT.toLong())
        } catch (e: Exception) {
            Timber.e(e)
        }

        return cache
    }

    fun imagePreview(view: View, imageUri: String) {
        if (imageUri.isNotBlank()) {
            val context = view.context
            val intent = ImagePreviewActivity.newIntent(context, imageUri)
            context.startActivity(intent)
        }
    }

    fun isLightTheme(settings: Settings): Boolean {
        @KeyUtil.ApplicationTheme val theme: String = settings.theme
        return theme == KeyUtil.THEME_LIGHT
    }

    fun dipToPx(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * Get List from a given array type
     * @return list of the array
     */
    @SafeVarargs
    fun <T> constructListFrom(vararg array: T): List<T> = listOf(*array)

    /**
     * Sorts a given map by the order of the of the keys in the map in descending order
     * @see ComparatorUtil.getKeyComparator
     */
    fun <T> getKeyFilteredMap(map: Map<String, T>): List<Map.Entry<String, T>> = map.asSequence().sortedWith(ComparatorUtil.getKeyComparator()).toList()

    fun isLowRamDevice(context: Context?): Boolean {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return activityManager?.isLowRamDevice == true
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
                    if (exceptions.contains(word)) {
                        result.append(word)
                    } else {
                        val starting = Character.toUpperCase(word[0])
                        result.append(starting).append(
                            word.substring(1)
                                .lowercase(Locale.getDefault()),
                        )
                    }
                }
                if (index != word.length - 1) {
                    result.append(" ")
                }
            }
            return result.toString()
        }
        return ""
    }

    /**
     * Get a list from a given array of strings
     * @return list of capitalized strings
     */
    fun capitalizeWords(strings: Array<String>): List<String> = strings.map { capitalizeWords(it) }

    @Deprecated(message = "Use .isNullOrEmpty on the object directly", level = DeprecationLevel.WARNING)
    fun <T : Collection<*>> isEmpty(collection: T?): Boolean = collection.isNullOrEmpty()

    fun equals(a: Any?, b: Any): Boolean = a != null && a == b
}
