package com.mxt.anitrend.api.call;

import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.api.structure.UserActivityReply;
import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.custom.Payload;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Max on 10/4/2016.
 */
public interface UserModel {

    @GET("user")
    Call<User> fetchCurrentUser();

    @GET("user/activity")
    Call<UserActivity> fetchCurrentUserActivity(@Query("page") int page);

    @GET("user/activity")
    Call<List<UserActivity>> fetchCurrentUserActivity(@Query("page") int page, @Query("type") String type);

    @GET("user/{username}/activity")
    Call<List<UserActivity>> fetchUserActivity( @Path("username") String username, @Query("page") int page, @Query("type") String type);

    @GET("user/{id}")
    Call<User> fetchUser(@Path("id") int id);

    @GET("user/{displayname}")
    Call<User> fetchUser(@Path("displayname") String displayname);

    /**
     * Activities of the user including messages etc
     *
     */
    @GET("user/{id}/activity")
    Call<List<UserActivity>> fetchUserActivity(@Path("id") int id, @Query("page") int page, @Query("type") String type);

    @GET("user/activity/{id}")
    Call<UserActivity> fetchUserActivity(@Path("id") int id);

    @GET("user/activity/{id}/reply")
    Call<List<UserActivityReply>> fetchActivityReplies(@Path("id") int id);

    @POST("activity/like")
    Call<ResponseBody> toggleFavourite(@Body Payload.ActionIdBased payload);

    @POST("activity/reply/like")
    Call<ResponseBody> toggleReplyFavourite(@Body Payload.ActionIdBased payload);


    /**
     * Send post request for creating an activity status
     * <br />
     *
     * @param text The text of what you wish to post
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> createActivityStatus(@Field("text") String text);

    /**
     * Send post request for creating an activity message
     * <br />
     *
     * @param text (string) activity text
     * @param messenger_id (int) recipient user id
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> createActivityMessage(@Field("text") String text, @Field("messenger_id") int messenger_id);

    /**
     * Send post request for editing an existing reply on an activity
     * <br />
     *
     * @param id The id of the comment that is being edited
     * @param messenger_id  (int) recipient user id
     * @param user_id int from the post creator
     * @param text Text representing the users reply to the activity
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<UserActivity> activityMessageEdit(@Field("id") int id, @Field("messenger_id") int messenger_id, @Field("text") String text , @Field("user_id") int user_id);

    /**
     * Send post request for creating a reply on a activity
     * <br />
     *
     * @param reply_id int from the UserActivity.getId()
     * @param text Text representing the users reply to the activity
     */
    @FormUrlEncoded
    @POST("user/activity")
    Call<ResponseBody> createActivityReply(@Field("reply_id") int reply_id, @Field("text") String text);

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
    Call<UserActivity> activityEdit(@Field("id") int id, @Field("text") String text , @Field("user_id") int user_id);

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
     * @param payload contains an int id of the activity
     */
    @HTTP(method = "DELETE", path = "user/activity", hasBody = true)
    Call<ResponseBody> activityDelete(@Body Payload.ActionIdBased payload);

    /**
     * Send a delete request to remove an activity reply
     * <br/>
     *
     * @param payload an int of the id of the reply item
     */
    @HTTP(method = "DELETE", path = "user/activity/reply", hasBody = true)
    Call<ResponseBody> activityDeleteReply(@Body Payload.ActionIdBased payload);


    @GET("user/notifications")
    Call<List<UserNotification>> fetchNotifications();

    @GET("user/notifications/count")
    Call<Integer> fetchNotificationCount();

    @GET("user/{id}/following")
    Call<List<UserSmall>> fetchFollowing(@Path("id") int id);

    @GET("user/{id}/followers")
    Call<List<UserSmall>> fetchFollowers(@Path("id") int id);

    @POST("user/follow")
    Call<ResponseBody> followUserToggle(@Body Payload.ActionIdBased payload);

    @GET("user/{id}/favourites")
    Call<Favourite> fetchFavourites(@Path("id") int id);

    @GET("user/{displayname}/favourites")
    Call<Favourite> fetchFavourites(@Path("displayname") String displayname);

    @GET("user/airing")
    Call<List<ListItem>> fetchAiring(@Query("limit") int limit);

    @GET("user/search/{query}")
    Call<List<UserSmall>> searchUser(@Path("query") String query);
}
