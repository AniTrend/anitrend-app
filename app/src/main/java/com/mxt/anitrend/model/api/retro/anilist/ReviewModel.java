package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.general.FeedReview;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Max on 10/4/2016.
 */
public interface ReviewModel {

    @GET("anime/review/{review_id}")
    Call<Review> getAnimeReview(@Path("review_id") int id);

    @GET("manga/review/{review_id}")
    Call<Review> getMangaReview(@Path("review_id") int id);

    @GET("anime/{anime_id}/reviews")
    Call<List<Review>> getAnimeReviews(@Path("anime_id") long id);

    @GET("manga/{manga_id}/reviews")
    Call<List<Review>> getMangaReviews(@Path("manga_id") long id);

    /**
     * View all the users reviews
     */
    @GET("user/{id}/reviews")
    Call<List<Review>> getMyReviews(@Path("id") int id);

    /**
     * View all the users reviews
     */
    @GET("user/{displayname}/reviews")
    Call<List<Review>> getMyReviews(@Path("displayname") String displayname);

    /**
     * View review types
     */
    @GET("reviews/{review_type}")
    Call<FeedReview> getSeriesReviews(@Path("review_type") String type, @Query("page") int page);

    /**
     * @param id     : (int) id of review to rate
     * @param rating : (int) 0 no rating, 1 up/positive rating, 2 down/negative rating
     */
    @FormUrlEncoded
    @POST("anime/review/rate")
    Call<Review> rateAnimeReview(@Field("id") int id, @Field("rating") int rating);

    /**
     * @param id     : (int) id of review to rate
     * @param rating : (int) 0 no rating, 1 up/positive rating, 2 down/negative rating
     */
    @FormUrlEncoded
    @POST("manga/review/rate")
    Call<Review> rateMangaReview(@Field("id") int id, @Field("rating") int rating);


    /**
     * Add a new series review
     *
     * <br>
     * @param id : (int) anime_id of review anime. (Change to manga_id for manga)
     * @param text     : (string) Review text (min 2000 characters)
     * @param summary  : (string) Review summary (min 20, max 120 characters)
     * @param hidden  : (int) 0 or 1 boolean
     * @param score    : (int) 0-100 review score
     */
    @FormUrlEncoded
    @POST("anime/review")
    Call<ResponseBody> addAnimeReview(@Field("anime_id") int id, @Field("text") String text, @Field("summary") String summary, @Field("private") int hidden, @Field("score") int score);

    /**
     * Add a new series review
     *
     * <br>
     * @param id : (int) anime_id of review anime. (Change to manga_id for manga)
     * @param text     : (string) Review text (min 2000 characters)
     * @param summary  : (string) Review summary (min 20, max 120 characters)
     * @param hidden  : (int) 0 or 1 boolean
     * @param score    : (int) 0-100 review score
     */
    @FormUrlEncoded
    @POST("anime/review")
    Call<ResponseBody> addMangaReview(@Field("manga_id") int id, @Field("text") String text, @Field("summary") String summary, @Field("private") int hidden, @Field("score") int score);


    /**
     * Edit series review
     *
     * <br>
     * @param id : (int) anime_id of review anime. (Change to manga_id for manga)
     * @param text     : (string) Review text (min 2000 characters)
     * @param summary  : (string) Review summary (min 20, max 120 characters)
     * @param hidden  : (int) 0 or 1 boolean
     * @param score    : (int) 0-100 review score
     */
    @FormUrlEncoded
    @PUT("anime/review")
    Call<ResponseBody> editAnimeReview(@Field("anime_id") int id, @Field("text") String text, @Field("summary") String summary, @Field("private") int hidden, @Field("score") int score);

    /**
     * Edit series review
     *
     * <br>
     * @param id : (int) anime_id of review anime. (Change to manga_id for manga)
     * @param text     : (string) Review text (min 2000 characters)
     * @param summary  : (string) Review summary (min 20, max 120 characters)
     * @param hidden  : (int) 0 or 1 boolean
     * @param score    : (int) 0-100 review score
     */
    @FormUrlEncoded
    @PUT("anime/review")
    Call<ResponseBody> editMangaReview(@Field("manga_id") int id, @Field("text") String text, @Field("summary") String summary, @Field("private") int hidden, @Field("score") int score);


    /**
     * Delete series review
     *
     * <br>
     * @param id : (int) anime_id of review anime. (Change to manga_id for manga)
     * @param text     : (string) Review text (min 2000 characters)
     * @param summary  : (string) Review summary (min 20, max 120 characters)
     * @param hidden  : (int) 0 or 1 boolean
     * @param score    : (int) 0-100 review score
     */
    @FormUrlEncoded
    @DELETE("anime/review")
    Call<ResponseBody> deleteAnimeReview(@Field("anime_id") int id, @Field("text") String text, @Field("summary") String summary, @Field("private") int hidden, @Field("score") int score);

    /**
     * Delete new series review
     *
     * <br>
     * @param id : (int) anime_id of review anime. (Change to manga_id for manga)
     * @param text     : (string) Review text (min 2000 characters)
     * @param summary  : (string) Review summary (min 20, max 120 characters)
     * @param hidden  : (int) 0 or 1 boolean
     * @param score    : (int) 0-100 review score
     */
    @FormUrlEncoded
    @DELETE("anime/review")
    Call<ResponseBody> deleteMangaReview(@Field("manga_id") int id, @Field("text") String text, @Field("summary") String summary, @Field("private") int hidden, @Field("score") int score);

}
