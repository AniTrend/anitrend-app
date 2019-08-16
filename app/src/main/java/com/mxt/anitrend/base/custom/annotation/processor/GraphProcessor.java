package com.mxt.anitrend.base.custom.annotation.processor;

import android.content.Context;
import android.util.Log;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by max on 2018/03/12.
 * GraphQL annotation processor
 */
public class GraphProcessor {
    private final String TAG = GraphProcessor.class.getSimpleName();
    private static GraphProcessor ourInstance;
    private final static Object lock = new Object();
    private final static String defaultExtension = ".graphql", defaultDirectory = "graphql";

    public static GraphProcessor getInstance(Context context) {
        if(ourInstance == null)
            ourInstance = new GraphProcessor(context);
        return ourInstance;
    }

    private GraphProcessor(Context context) {
        synchronized (lock) {
            Timber.tag(TAG).i("%s: has obtained a synchronized lock on the object", Thread.currentThread().getName());
            if(this.graphFiles == null)
                this.graphFiles = new HashMap<>();
            if(isEmpty()) {
                Timber.tag(TAG).i("%s: is initializing query files", Thread.currentThread().getName());
                initialize(defaultDirectory, context);
                Timber.tag(TAG).i("%s: has completed initializing all files", Thread.currentThread().getName());
                Timber.tag(TAG).i(Thread.currentThread().getName() + ": Total count of graphFiles -> size: " + graphFiles.size());
            } else
                Timber.tag(TAG).i(Thread.currentThread().getName() + ": skipped initialization of graphFiles -> size: " + graphFiles.size());
        }
    }

    private volatile Map<String, String> graphFiles;

    public String getQuery(Annotation[] annotations) {
        GraphQuery graphQuery = null;

        for (Annotation annotation: annotations)
            if(annotation instanceof GraphQuery) {
                graphQuery = (GraphQuery) annotation;
                break;
            }

        if(graphFiles != null && graphQuery != null) {
            String fileName = String.format("%s%s", graphQuery.value(), defaultExtension);
            Timber.tag(TAG).i(fileName);
            if(graphFiles.containsKey(fileName))
                return graphFiles.get(fileName);
            Timber.tag(TAG).e("The request query %s could not be found!", graphQuery.value());
            Timber.tag(TAG).e("Current size of graphFiles -> size: %d", graphFiles.size());
        }
        return null;
    }

    private synchronized boolean isEmpty() {
        return graphFiles.size() < 1;
    }

    private synchronized void initialize(String path, Context context) {
        try {
            String[] paths = context.getAssets().list(path);
            if (paths.length > 0) {
                for (String item : paths) {
                    String absolute = path + "/" + item;
                    if (!item.endsWith(defaultExtension))
                        initialize(absolute, context);
                    else
                        graphFiles.put(item, getFileContents(context.getAssets().open(absolute)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Timber.tag(TAG).e(e);
        }
    }

    private synchronized String getFileContents(InputStream inputStream) {
        StringBuilder queryBuffer = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            for(String line; (line = bufferedReader.readLine()) != null;)
                queryBuffer.append(line);
            inputStreamReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryBuffer.toString();
    }
}
