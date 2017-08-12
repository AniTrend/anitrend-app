package com.mxt.anitrend.api.call;

import com.mxt.anitrend.api.hub.Playlist;
import com.mxt.anitrend.api.hub.Result;
import com.mxt.anitrend.api.hub.Video;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by max on 2017/05/14.
 */
public interface Hub {

    String filter = "{\"videoStatus\":\"final\"," +
            "\"audioLang\":\"jpn\", " +
            "\"addedBy\":\"UeQuqJ90j3igXB6A\"" +
            "}";

    /*Pass in the param of the video item*/
    String playlist_filter = "{\"videos\":{\"$exists\":true}," +
            "\"videos\":\"%s\"}";

    String format = "json";

    String arg_order = "arg_order";
    String arg_limit = "arg_limit";
    String arg_page = "arg_page";
    String arg_expand = "arg_expand";
    String arg_search = "arg_search";
    String arg_filter = "arg_filter";
    String arg_format = "arg_format";
    String arg_id = "arg_id";

    @GET("videos")
    Call<Result<Video>> getVideos(@Query("order") String order,
                                  @Query("limit") int limit,
                                  @Query("page") int page,
                                  @Query("expand") String expand,
                                  @Query("search") String search,
                                  @Query("filter") String filter);

    @GET("videos")
    Call<Result<Video>> getRssFeed(@Query("order") String order,
                                   @Query("limit") int limit,
                                   @Query("page") int page,
                                   @Query("expand") String expand,
                                   @Query("search") String search,
                                   @Query("filter") String filter,
                                   @Query("format") String format);

    @GET("playlists")
    Call<Result<Playlist>> getPlaylist(@Query("order") String order,
                                       @Query("limit") int limit,
                                       @Query("page") int page,
                                       @Query("expand") String expand,
                                       @Query("search") String search,
                                       @Query("filter") String filter);

    @GET("videos/{id}")
    Call<Video> getEpisode(@Path("id") String id);
}
