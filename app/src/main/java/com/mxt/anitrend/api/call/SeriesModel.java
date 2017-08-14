package com.mxt.anitrend.api.call;


import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.Airing;
import com.mxt.anitrend.api.structure.Genre;
import com.mxt.anitrend.base.custom.Payload;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Maxwell on 10/2/2016.
 */
public interface SeriesModel {

    /**
     * Cache the result of genre list somewhere for later use
     * @return list of genres to use with browse query
    */
    @GET("genre_list")
    Call<List<Genre>> fetchGenres();

    /**
     * Browse a collection of anime types
     *
     * @param series_type the type of series either anime or manga
     */
    @GET("browse/{series_type}")
    Call<List<Series>> fetchBrowsable(@Path("series_type") String series_type);

    /**
     * Gets a filtered result of anime
     *
     * @param year           : 4 digit year e.g. "2014"
     * @param season         : "winter" || "spring" || "summer" || "fall"
     * @param type           : (See types table above)
     * @param status         : (See status types table above)
     * @param genres         : Comma separated genre strings. e.g. "Action,Comedy" Returns series that have ALL the genres.
     * @param genres_exclude : Comma separated genre strings. e.g. "Drama" Excludes series that have ANY of the genres.
     * @param sort           : "id" || "score" || "popularity" || "start_date" || "end_date" Sorts results, default ascending order. Append "-desc" for descending order e.g. "id-desc"
     * @param airing_data    : "airing_data=true" Includes anime airing data in small models
     * @param full_page      : "full_page=true" Returns all available results. Ignores pages. Only available when status="Currently Airing" or season is included
     * @param page           : int
    */
    @GET("browse/{series_type}")
    Call<List<Series>> fetchFileteredBrowsable(@Path("series_type") String series_type,
                                               @Query("year") Integer year,
                                               @Query("season") String season,
                                               @Query("type") String type,
                                               @Query("status") String status,
                                               @Query("genres") String genres,
                                               @Query("genres_exclude") String genres_exclude,
                                               @Query("sort") String sort,
                                               @Query("airing_data") Boolean airing_data,
                                               @Query("full_page") Boolean full_page,
                                               @Query("page") Integer page);

    /**
     * Basic
     * @return  a series model.
     * */
    @GET("{series_type}/{id}")
    Call<Series> fetchSeries(@Path("series_type") String series_type, @Path("id") int id);

    /**
     * Page Item which returns a series model with the following:
     * <p>
     * Up to 9 small model characters (ordered by main role) with Japanese small model actors for anime
     * Up to 9 small model staff
     * Up to 2 small model reviews with their users
     * Relations (small model)
     * Anime/Manga relations (small model)
     * Studios (anime)
     * External links (anime)
     *
     * @return a series model with the following:
     * */
    @GET("{series}/{id}/page")
    Call<Series> fetchSeriesPage(@Path("series") String series_type, @Path("id") int id);

    /**
     * Returns series model with the following:
     * <p>
     * Small model characters (ordered by main role) with small model actors     *
     *
     * @return series model
     */
    @GET("{series}/{id}/characters")
    Call<Series> fetchSeriesCharacters(@Path("series") String series_type, @Path("id") int id);

    /**
     * Returns series model with the following:
     * <p>
     * Small model characters (ordered by main role) with small model actors
     * Small model staff
     *
     * @return series model
     */
    @GET("{series}/{id}/staff")
    Call<Series> fetchSeriesStaff(@Path("series") String series_type, @Path("id") int id);

    /**
     * Returns series model with the following:
     * <p>
     * Small model characters (ordered by main role) with small model actors     *
     *
     * @return series model
     */
    @GET("{series}/{id}/actors")
    Call<Series> fetchSeriesActors(@Path("series") String series_type, @Path("id") int id);

    /**
     * Get airing status of an anime
     *
     * @return Key: Episode number Value: Airing Time
     */
    @GET("anime/{id}/airing")
    Call<List<Airing>> fetchAiringStatus(@Path("id") int id);

    /**
     * Search for a type of series
     *
     * @param series_type anime or manga
     * @param query search for what?
     * @return series models
    */
    @GET("{series}/search/{query}")
    Call<List<Series>> findSeries(@Path("series") String series_type, @Path("query") String query);

    @POST("{series_type}/favourite")
    Call<ResponseBody> toggleFavourite(@Path("series_type") String series_type, @Body Payload.ActionIdBased payload);
}
