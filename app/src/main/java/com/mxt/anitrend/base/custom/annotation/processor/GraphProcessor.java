package com.mxt.anitrend.base.custom.annotation.processor;

import android.content.Context;
import android.util.Log;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by max on 2018/03/12.
 * GraphQL annotation processor
 */
public class GraphProcessor {

    private final Map<String, String> graphFiles;

    public GraphProcessor(Context context) {
        this.graphFiles = new WeakHashMap<>();
        findAllFiles("graphql", context);
    }

    public String getQuery(Annotation[] annotations) {
        GraphQuery graphQuery = null;

        for (Annotation annotation: annotations)
            if(annotation instanceof GraphQuery) {
                graphQuery = (GraphQuery) annotation;
                break;
            }

        if(graphFiles != null && graphQuery != null) {
            String fileName = String.format("%s.graphql", graphQuery.value());
            if(graphFiles.containsKey(fileName))
                return graphFiles.get(fileName);
            Log.e(this.toString(), String.format("The request query %s could not be found!", graphQuery.value()));
        }
        return null;
    }

    private void findAllFiles(String path, Context context) {
        String[] paths;
        try {
            paths = context.getAssets().list(path);
            if (paths.length > 0) {
                for (String item : paths) {
                    String absolute = path + "/" + item;
                    if(!item.endsWith(".graphql"))
                        findAllFiles(absolute, context);
                    else
                        graphFiles.put(item, getFileContents(context.getAssets().open(absolute)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileContents(InputStream inputStream) {
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
