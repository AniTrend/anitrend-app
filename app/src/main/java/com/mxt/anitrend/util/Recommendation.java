package com.mxt.anitrend.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by max on 2017-05-06.
 */

public class Recommendation {

    private static String genres;

    public static String getTopFavourites (HashMap<String ,Integer> favourites, ApiPreferences prefs) {
        final int maximum = 3;
        int count = 0;

        if(genres != null)
            return genres;

        if(favourites.size() > 0) {
            List<Map.Entry<String, Integer>> list = new LinkedList<>(favourites.entrySet());

            // Sorting the list based on values
            Collections.sort(list, ComparatorProvider.getGenresComparator(prefs));

            for (Map.Entry<String, Integer> entry : list)
            {
                if(count == maximum)
                    break;
                if(genres == null)
                    genres = entry.getKey();
                else
                    genres += ","+entry.getKey();
                count++;
            }
        }
        return genres;
    }
}
