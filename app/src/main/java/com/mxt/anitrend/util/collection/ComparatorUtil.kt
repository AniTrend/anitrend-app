package com.mxt.anitrend.util.collection

import java.util.Comparator

/**
 * Created by Maxwell on 2/12/2017.
 * Comparator Provider returns comparators of various types
 */
object ComparatorUtil {
    @JvmStatic
    fun <T> getKeyComparator(): Comparator<Map.Entry<String, T>> = Comparator { o1, o2 -> o1.key.compareTo(o2.key) }
}
