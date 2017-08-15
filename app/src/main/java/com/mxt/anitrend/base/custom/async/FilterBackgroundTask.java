package com.mxt.anitrend.base.custom.async;

import android.os.AsyncTask;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import java.util.Comparator;
import java.util.List;

/**
 * Created by max on 2017/05/14.
 */

public class FilterBackgroundTask <T> extends AsyncTask<Predicate<T>, Void, List<T>> {

    private List<T> model;
    private Comparator<T> comparator;

    public FilterBackgroundTask(List<T> model) {
        this.model = model;
    }

    public FilterBackgroundTask(List<T> model, Comparator<T> comparator) {
        this.model = model;
        this.comparator = comparator;
    }

    @Override
    @SafeVarargs
    protected final List<T> doInBackground(Predicate<T>... params) {
        if(!isCancelled()) {
            if(comparator == null)
                return Stream.of(model)
                        .filter(params[0])
                        .toList();
            else
                return Stream.of(model)
                        .filter(params[0])
                        .sorted(comparator)
                        .toList();
        }
        return null;
    }
}
