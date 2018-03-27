package com.mxt.anitrend.util;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Maxwell on 2/12/2017.
 * Comparator Provider returns comparators of various types
 */
public final class ComparatorProvider {

    public static <T> Comparator<HashMap.Entry<String, T>> getKeyComparator() {
        return (o1, o2) -> o1.getKey().compareTo(o2.getKey());
    }
}