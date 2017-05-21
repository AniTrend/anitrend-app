package com.mxt.anitrend.api.call;

import com.mxt.anitrend.api.structure.Rss;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Maxwell on 2/10/2017.
 */

public interface EpisodeModel {

    @GET("/{path}")
    Call<Rss> getRSS(@Path("path") String link);

    @GET("crunchyroll/rss/anime/popular?format=xml")
    Call<Rss> getPopularFeed();

    @GET("crunchyroll/rss/anime")
    Call<Rss> getLatestFeed();
}
