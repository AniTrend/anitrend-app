package com.mxt.anitrend.base.custom.annotation.processor;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;
import com.mxt.anitrend.base.custom.annotation.GraphQuery;

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
            Log.w(this.toString(), String.format("The request query %s could not be found!", graphQuery.value()));
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
