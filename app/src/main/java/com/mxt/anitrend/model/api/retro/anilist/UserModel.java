package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.anilist.UserActivity;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.general.MediaList;
import com.mxt.anitrend.model.entity.general.UserActivityReply;
import com.mxt.anitrend.model.entity.general.Notification;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Max on 10/4/2016.
 */
public interface UserModel {

    @GET("user")
    Call<User> getCurrentUser();

    @GET("user/activity")
    Call<UserActivity> getCurrentUserActivity(@Query("page") int page);

    @GET("user/activity")
    Call<List<UserActivity>> getCurrentUserActivity(@Query("page") int page, @Query("type") String type);

    @GET("user/{username}/activity")
    Call<List<UserActivity>> getUserActivity(@Path("username") String username, @Query("page") int page, @Query("type") String type);

    @GET("user/{id}")
    Call<User> getUser(@Path("id") long id);

    @GET("user/{username}")
    Call<User> getUser(@Path("username") String username);

    /**
     * Activities of the user including messages etc
     *
     */
    @GET("user/{id}/activity")
    Call<List<UserActivity>> getUserActivity(@Path("id") int id, @Query("page") int page, @Query("type") String type);

    @GET("user/activity/{id}")
    Call<UserActivity> getUserActivity(@Path("id") int id);

    @GET("user/activity/{id}/reply")
    Call<List<UserActivityReply>> getActivityReplies(@Path("id") int id);

    @FormUrlEncoded
    @POST("activity/like")
    Call<ResponseBody> toggleFavourite(@Field("id") int id);

    @FormUrlEncoded
    @POST("activity/reply/like")
    Call<ResponseBody> toggleReplyFavourite(@Field("id") int id);


    /**
     * Send post request for creating an activity status
     * <br />
     *
     * @param text The text of what you wish to post
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> activityStatus(@Field("text") String text);

    /**
     * Send post request for creating an activity message
     * <br />
     *
     * @param text (string) activity text
     * @param messenger_id recipient user id
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<UserActivity> activityMessage(@Field("text") String text, @Field("messenger_id") long messenger_id);

    /**
     * Send post request for editing an existing reply on an activity
     * <br />
     *
     * @param id The id of the comment that is being edited
     * @param messenger_id  recipient user id
     * @param user_id from the post creator
     * @param text Text representing the users reply to the activity
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<UserActivity> activityMessageEdit(@Field("id") int id, @Field("messenger_id") long messenger_id, @Field("text") String text, @Field("user_id") long user_id);

    /**
     * Send post request for creating a reply on a activity
     * <br />
     *
     * @param reply_id int from the UserActivity.getId()
     * @param text Text representing the users reply to the activity
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> activityReply(@Field("reply_id") int reply_id, @Field("text") String text);

    /**
     * Send post request for editing an existing reply on an activity
     * <br />
     *
     * @param id The id of the comment that is being edited
     * @param user_id int from the post creator
     * @param text Text representing the users reply to the activity
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> activityEdit(@Field("id") int id, @Field("text") String text, @Field("user_id") long user_id);

    /**
     * Send post request for editing an existing reply on an activity
     * <br />
     *
     * @param id The id of the comment that is being edited
     * @param reply_id int from the UserActivity.getId()
     * @param text Text representing the users reply to the activity
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> activityEditReply(@Field("id") int id, @Field("reply_id") int reply_id, @Field("text") String text);

    /**
     * Send a delete request to remove an activity
     * <br/>
     *
     * @param id contains an int id of the activity
     */
    @DELETE("user/activity")
    Call<ResponseBody> activityDelete(@Query("id") int id);

    /**
     * Send a delete request to remove an activity reply
     * <br/>
     *
     * @param id an int of the id of the reply item
     */
    @DELETE("user/activity/reply")
    Call<ResponseBody> activityDeleteReply(@Query("id") int id);


    @GET("user/notifications")
    Call<List<Notification>> getNotifications();

    @GET("user/notifications/count")
    Call<Integer> getNotificationCount();

    @GET("user/{id}/following")
    Call<List<UserBase>> getFollowing(@Path("id") long id);

    @GET("user/{id}/followers")
    Call<List<UserBase>> getFollowers(@Path("id") long id);

    @FormUrlEncoded
    @POST("user/follow")
    Call<ResponseBody> toggleFollow(@Field("id") long id);

    @GET("user/{id}/favourites")
    Call<Favourite> getFavourites(@Path("id") long id);

    @GET("user/{displayname}/favourites")
    Call<Favourite> getFavourites(@Path("displayname") String displayname);

    @GET("user/airing")
    Call<List<MediaList>> getAiring(@Query("limit") int limit);

    @GET("user/search/{query}")
    Call<List<UserBase>> searchUser(@Path("query") String query, @Query("page") int page);
}
