package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.general.SeriesList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Maxwell on 11/13/2016.
 */

public interface UserListModel {

    @GET("user/{id}/animelist")
    Call<User> getAnimeList(@Path("id") long id);

    @GET("user/{displayname}/animelist")
    Call<User> getAnimeList(@Path("displayname") String displayname);

    @GET("animelist/{anime_id}")
    Call<SeriesList> getAnimeListStatus(@Path("anime_id") long id);

    @GET("mangalist/{manga_id}")
    Call<SeriesList> getMangaListStatus(@Path("manga_id") long id);

    /**
     * Raw outputs the same as the standard animelist output but the without anime relation data.
     */
    @GET("user/{id}/animelist/raw")
    Call<User> getAnimeListRaw(@Path("id") long id);

    /**
     * Raw outputs the same as the standard animelist output but the without anime relation data.
     */
    @GET("user/{displayname}/animelist/raw")
    Call<User> getAnimeListRaw(@Path("displayname") String displayname);

    /**
     * Used for adding a new anime entry
     *
     * @param id: (int) anime_id of list item
     * @param list_status: (String) "watching" || "completed" || "on-hold" || "dropped" || "plan to watch"
     * @param score: (See bottom of page - List score types)
     * @param score_raw: (int) 0-100 (See bottom of page - Raw score)
     * @param episodes_watched: (int)
     * @param rewatched: (int)
     * @param notes: (String)
     * @param advanced_rating_scores: comma separated scores, same order as advanced_rating_names
     * @param custom_lists: comma separated 1 or 0, same order as custom_list_anime
     * @param hidden_default: (int) 0 || 1
     */
    @FormUrlEncoded
    @POST("animelist")
    Call<SeriesList> addAnimeItem(@Field("id") long id,
                                  @Field("list_status") String list_status,
                                  @Field("score") String score,
                                  @Field("score_raw") Integer score_raw,
                                  @Field("episodes_watched") Integer episodes_watched,
                                  @Field("rewatched") Integer rewatched,
                                  @Field("notes") String notes,
                                  @Field("advanced_rating_scores") String advanced_rating_scores,
                                  @Field("custom_lists") Integer custom_lists,
                                  @Field("hidden_default") Integer hidden_default);

    /**
     * Used for editing an anime list item
     *
     * @param id: (int) anime_id of list item
     * @param list_status: (String) "watching" || "completed" || "on-hold" || "dropped" || "plan to watch"
     * @param score: (See bottom of page - List score types)
     * @param score_raw: (int) 0-100 (See bottom of page - Raw score)
     * @param episodes_watched: (int)
     * @param rewatched: (int)
     * @param notes: (String)
     * @param advanced_rating_scores: comma separated scores, same order as advanced_rating_names
     * @param custom_lists: comma separated 1 or 0, same order as custom_list_anime
     * @param hidden_default: (int) 0 || 1
     */
    @FormUrlEncoded
    @PUT("animelist")
    Call<SeriesList> editAnimeItem(@Field("id") long id,
                               @Field("list_status") String list_status,
                               @Field("score") String score,
                               @Field("score_raw") Integer score_raw,
                               @Field("episodes_watched") Integer episodes_watched,
                               @Field("rewatched") Integer rewatched,
                               @Field("notes") String notes,
                               @Field("advanced_rating_scores") String advanced_rating_scores,
                               @Field("custom_lists") Integer custom_lists,
                               @Field("hidden_default") Integer hidden_default);



    @GET("user/{id}/mangalist")
    Call<User> getMangaList(@Path("id") long id);

    @GET("user/{displayname}/mangalist")
    Call<User> getMangaList(@Path("displayname") String displayname);

    /**
     * Raw outputs the same as the standard mangalist output but without the manga relation data.
     */
    @GET("user/{id}/mangalist/raw")
    Call<User> getMangaListRaw(@Path("id") long id);

    /**
     * Raw outputs the same as the standard mangalist output but without the manga relation data.
     */
    @GET("user/{displayname}/mangalist/raw")
    Call<User> getMangaListRaw(@Path("displayname") String displayname);

    /**
     * Used for adding manga list item
     *
     * @param id: (int) manga_id of list item
     * @param list_status: (String) "reading" || "completed" || "on-hold" || "dropped" || "plan to read"
     * @param score: (See bottom of page - List score types)
     * @param score_raw: (int) 0-100 (See bottom of page - Raw score)
     * @param chapters_read: (int)
     * @param volumes_read: (int)
     * @param reread: (int)
     * @param notes: (String)
     * @param advanced_rating_scores: comma separated scores, same order as advanced_rating_names
     * @param custom_lists: comma separated 1 or 0, same order as custom_list_manga
     * @param hidden_default: (int) 0 || 1
     */
    @FormUrlEncoded
    @POST("mangalist")
    Call<SeriesList> addMangaItem(@Field("id") long id,
                              @Field("list_status") String list_status,
                              @Field("score") String score,
                              @Field("score_raw") Integer score_raw,
                              @Field("volumes_read") Integer volumes_read,
                              @Field("chapters_read") Integer chapters_read,
                              @Field("reread") Integer reread,
                              @Field("notes") String notes,
                              @Field("advanced_rating_scores") String advanced_rating_scores,
                              @Field("custom_lists") Integer custom_lists,
                              @Field("hidden_default") Integer hidden_default);

    /**
     * Used for editing manga list item
     *
     * @param id: (int) manga_id of list item
     * @param list_status: (String) "reading" || "completed" || "on-hold" || "dropped" || "plan to read"
     * @param score: (See bottom of page - List score types)
     * @param score_raw: (int) 0-100 (See bottom of page - Raw score)
     * @param chapters_read: (int)
     * @param volumes_read: (int)
     * @param reread: (int)
     * @param notes: (String)
     * @param advanced_rating_scores: comma separated scores, same order as advanced_rating_names
     * @param custom_lists: comma separated 1 or 0, same order as custom_list_manga
     * @param hidden_default: (int) 0 || 1
     */
    @FormUrlEncoded
    @PUT("mangalist")
    Call<SeriesList> editMangaItem(@Field("id") long id,
                               @Field("list_status") String list_status,
                               @Field("score") String score,
                               @Field("score_raw") Integer score_raw,
                               @Field("volumes_read") Integer volumes_read,
                               @Field("chapters_read") Integer chapters_read,
                               @Field("reread") Integer reread,
                               @Field("notes") String notes,
                               @Field("advanced_rating_scores") String advanced_rating_scores,
                               @Field("custom_lists") Integer custom_lists,
                               @Field("hidden_default") Integer hidden_default);


    @DELETE("animelist/{anime_id}")
    Call<ResponseBody> deleteAnime(@Path("anime_id") long id);


    @DELETE("mangalist/{manga_id}")
    Call<ResponseBody> deleteManga(@Path("manga_id") long id);
}
