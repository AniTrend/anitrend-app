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

/**
 * Created by max on 2018/03/12.
 * GraphQL annotation processor
 */
public class GraphProcessor {

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
            Log.d("GraphProcessor", Thread.currentThread().getName() + ": has obtained a synchronized lock on the object");
            if(this.graphFiles == null)
                this.graphFiles = new HashMap<>();
            if(isEmpty()) {
                Log.d("GraphProcessor", Thread.currentThread().getName() + ": is initializing query files");
                initialize(defaultDirectory, context);
                Log.d("GraphProcessor", Thread.currentThread().getName() + ": has completed initializing all files");
                Log.d("GraphProcessor", Thread.currentThread().getName() + ": Total count of graphFiles -> size: "+ graphFiles.size());
            } else
                Log.d("GraphProcessor", Thread.currentThread().getName() + ": skipped initialization of graphFiles -> size: "+ graphFiles.size());
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
            if(graphFiles.containsKey(fileName))
                return graphFiles.get(fileName);
            Log.e(this.toString(), String.format("The request query %s could not be found!", graphQuery.value()));
            Log.e(this.toString(), String.format("Current size of graphFiles -> size: %d", graphFiles.size()));
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
