package com.mxt.anitrend.base.custom.async;

import android.os.AsyncTask;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Maxwell on 2/12/2017.
 * Async Task that will use generic Comparator
 * To return the desired result. The Comparator
 * Is provided by @class - ComparatorProvider
 */
public class SortHelperTask<T> extends AsyncTask<List<T>, Void, Void> {

    private Comparator<T> comparator;

    public SortHelperTask(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override @SafeVarargs
    protected final Void doInBackground(List<T>... lists) {
        if(!isCancelled() && lists != null && lists.length > 0)
            try {
                Collections.sort(lists[0], comparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }

    @Override
    protected void onCancelled() {
        comparator = null;
        super.onCancelled();
    }
}
