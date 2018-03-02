package com.mxt.anitrend.model.api.retro.crunchy;

import com.mxt.anitrend.model.entity.crunchy.Rss;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by max on 2017/10/22.
 */

public interface EpisodeModel {

    @GET("/{path}")
    Call<Rss> getRSS(@Path("path") String link);

    @GET("crunchyroll/rss/anime/popular?format=xml")
    Call<Rss> getPopularFeed();

    @GET("crunchyroll/rss/anime")
    Call<Rss> getLatestFeed();
}
