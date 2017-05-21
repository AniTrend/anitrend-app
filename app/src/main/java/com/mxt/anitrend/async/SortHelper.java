package com.mxt.anitrend.async;

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
public class SortHelper<T> extends AsyncTask<Void, Void, List<T>> {

    private Comparator<T> comparator;
    private SortCallback<T> callback;
    private List<T> list;

    public SortHelper(Comparator<T> comparator, SortCallback<T> callback, List<T> list) {
        this.comparator = comparator;
        this.callback = callback;
        this.list = list;
    }

    @Override
    protected List<T> doInBackground(Void... voids) {
        if(list != null) {
            try {
                Collections.sort(list, comparator);
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } return list;
    }

    @Override
    protected void onPostExecute(List<T> sorted) {
        callback.onSortComplete(sorted);
    }

    public interface SortCallback <T> {
        void onSortComplete(List<T> result);
    }
}
