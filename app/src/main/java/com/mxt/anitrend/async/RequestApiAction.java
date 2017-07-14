package com.mxt.anitrend.async;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.call.CharacterModel;
import com.mxt.anitrend.api.call.ReviewModel;
import com.mxt.anitrend.api.call.SeriesModel;
import com.mxt.anitrend.api.call.StaffModel;
import com.mxt.anitrend.api.call.StudioModel;
import com.mxt.anitrend.api.call.UserListModel;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.custom.Payload;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Maxwell on 12/16/2016.
 */
public class RequestApiAction {

    /**
     * All requests that are Id based will be handled by this class
     */
    public static class IdActions extends AsyncTask<Void, Void, Call<ResponseBody>> {

        private Context mContext;
        private Callback<ResponseBody> mCallback;
        private KeyUtils.ActionType mActionType;
        private Payload.ActionIdBased actionIdBased;

        public IdActions(Context mContext, Callback<ResponseBody> mCallback, KeyUtils.ActionType mActionType, Payload.ActionIdBased actionIdBased) {
            this.mContext = mContext;
            this.mCallback = mCallback;
            this.mActionType = mActionType;
            this.actionIdBased = actionIdBased;
        }

        @Override
        protected void onPostExecute(Call<ResponseBody> requestBodyCall) {
            if(requestBodyCall != null)
                requestBodyCall.enqueue(mCallback);
            else
                mCallback.onFailure(null, new Throwable("Post prepareTask for posting action received null"));
        }

        @Override
        protected Call<ResponseBody> doInBackground(Void... integers) {
            try {
                switch (mActionType) {
                    case ACTION_FOLLOW_TOGGLE:
                        UserModel follow_toggle = ServiceGenerator.createService(UserModel.class, mContext);
                        return follow_toggle.followUserToggle(actionIdBased);
                    case ANIME_FAVOURITE:
                        SeriesModel anime = ServiceGenerator.createService(SeriesModel.class, mContext);
                        return anime.toggleFavourite(KeyUtils.SeriesTypes[KeyUtils.ANIME], actionIdBased);
                    case MANGA_FAVOURITE:
                        SeriesModel manga = ServiceGenerator.createService(SeriesModel.class, mContext);
                        return manga.toggleFavourite(KeyUtils.SeriesTypes[KeyUtils.MANGA], actionIdBased);
                    case CHARACTER_FAVOURITE:
                        CharacterModel character = ServiceGenerator.createService(CharacterModel.class, mContext);
                        return character.toggleFavourite(actionIdBased);
                    case STAFF_FAVOURITE:
                        StaffModel staff = ServiceGenerator.createService(StaffModel.class, mContext);
                        return staff.toggleFavourite(actionIdBased);
                    case ACTIVITY_CREATE:
                        break;
                    case ACTIVITY_EDIT:
                        break;
                    case ACTIVITY_DELETE:
                        UserModel delete = ServiceGenerator.createService(UserModel.class, mContext);
                        return delete.activityDelete(actionIdBased);
                    case ACTIVITY_REPLY:
                        break;
                    case ACTIVITY_FAVOURITE:
                        UserModel activity = ServiceGenerator.createService(UserModel.class, mContext);
                        return activity.toggleFavourite(actionIdBased);
                    case ACTIVITY_REPLY_FAVOURITE:
                        UserModel reply = ServiceGenerator.createService(UserModel.class, mContext);
                        return reply.toggleReplyFavourite(actionIdBased);
                    case ACTIVITY_REPLY_EDIT:
                        break;
                    case ACTIVITY_REPLY_DELETE:
                        UserModel reply_delete = ServiceGenerator.createService(UserModel.class, mContext);
                        return reply_delete.activityDeleteReply(actionIdBased);
                    case STUDIO_FAVOURITE:
                        StudioModel favour_studio = ServiceGenerator.createService(StudioModel.class, mContext);
                        return favour_studio.toggleFavourite(actionIdBased);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Activity type actions
     */
    @SuppressWarnings("unchecked")
    public static class ActivityActions <T> extends AsyncTask<Void, Void, Call<T>> {

        private Context mContext;
        private Callback<T> mCallback;
        private KeyUtils.ActionType mActionType;
        private Payload.ActivityStruct genericAction;

        public ActivityActions(Context mContext, Callback<T> mCallback, KeyUtils.ActionType mActionType, Payload.ActivityStruct genericAction) {
            this.mContext = mContext;
            this.mCallback = mCallback;
            this.mActionType = mActionType;
            this.genericAction = genericAction;
        }

        @Override
        protected void onPostExecute(Call<T> requestBodyCall) {
            if(requestBodyCall != null)
                requestBodyCall.enqueue(mCallback);
            else
                mCallback.onFailure(null, new Throwable("Post prepareTask for posting action received null"));
        }

        @Override
        protected Call<T> doInBackground(Void... integers) {
            try {
                switch (mActionType) {
                    case ACTIVITY_CREATE:
                        UserModel create = ServiceGenerator.createService(UserModel.class, mContext);
                        return (Call<T>) create.createActivityStatus(genericAction.getText());
                    case ACTIVITY_EDIT:
                        UserModel edit = ServiceGenerator.createService(UserModel.class, mContext);
                        return (Call<T>) edit.activityEdit(genericAction.getId(), genericAction.getText(), genericAction.getUser_id());
                    case ACTIVITY_REPLY:
                        UserModel reply = ServiceGenerator.createService(UserModel.class, mContext);
                        return (Call<T>) reply.createActivityReply(genericAction.getReply_id(), genericAction.getText());
                    case ACTIVITY_REPLY_EDIT:
                        UserModel reply_edit = ServiceGenerator.createService(UserModel.class, mContext);
                        return (Call<T>) reply_edit.activityEditReply(genericAction.getId() , genericAction.getReply_id(), genericAction.getText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static class MessageActions <T> extends AsyncTask<Void, Void, Call<T>> {

        private Context mContext;
        private Callback<T> mCallback;
        private KeyUtils.ActionType mActionType;
        private Payload.ActivityMessage messageAction;

        public MessageActions(Context mContext, Callback<T> mCallback, KeyUtils.ActionType mActionType, Payload.ActivityMessage messageAction) {
            this.mContext = mContext;
            this.mCallback = mCallback;
            this.mActionType = mActionType;
            this.messageAction = messageAction;
        }

        @Override
        protected void onPostExecute(Call<T> requestBodyCall) {
            if(requestBodyCall != null)
                requestBodyCall.enqueue(mCallback);
            else
                mCallback.onFailure(null, new Throwable("Post prepareTask for posting action received null"));
        }

        @Override
        protected Call<T> doInBackground(Void... integers) {
            try {
                switch (mActionType) {
                    case DIRECT_MESSAGE_SEND:
                        UserModel message = ServiceGenerator.createService(UserModel.class, mContext);
                        return (Call<T>) message.createActivityMessage(messageAction.getText(), messageAction.getMessenger_id());
                    case DIRECT_MESSAGE_EDIT:
                        UserModel message_edit = ServiceGenerator.createService(UserModel.class, mContext);
                        return (Call<T>) message_edit.activityMessageEdit(messageAction.getId(), messageAction.getMessenger_id(), messageAction.getText(), messageAction.getUser_id());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * When handling reviews
     */
    public static class ReviewBasedActions extends AsyncTask<Void, Void, Call<ResponseBody>> {

        private Context mContext;
        private Callback<ResponseBody> mCallback;
        private KeyUtils.ActionType mActionType;
        private Payload.ReviewActions reviewActions;

        public ReviewBasedActions(Context mContext, Callback<ResponseBody> mCallback, KeyUtils.ActionType mActionType, Payload.ReviewActions reviewActions) {
            this.mContext = mContext;
            this.mCallback = mCallback;
            this.mActionType = mActionType;
            this.reviewActions = reviewActions;
        }

        @Override
        protected void onPostExecute(Call<ResponseBody> requestBodyCall) {
            if(requestBodyCall != null)
                requestBodyCall.enqueue(mCallback);
            else
                mCallback.onFailure(null, new Throwable("Post prepareTask for posting action received null"));
        }

        @Override
        protected Call<ResponseBody> doInBackground(Void... integers) {
            try {
                switch (mActionType) {
                    case REVIEW_ANIME_RATE:
                        ReviewModel review_anime = ServiceGenerator.createService(ReviewModel.class, mContext);
                        return review_anime.rateAnimeReview(reviewActions.getId(), reviewActions.getRating());
                    case REVIEW_MANGA_RATE:
                        ReviewModel review_manga = ServiceGenerator.createService(ReviewModel.class, mContext);
                        return review_manga.rateMangaReview(reviewActions.getId(), reviewActions.getRating());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Adds an anime list to your list of anime
     */
    public static class AnimeListActions extends AsyncTask<Void, Void, Call<Object>> {

        private Context mContext;
        private Callback<Object> mCallback;
        private KeyUtils.ActionType mActionType;
        private Payload.ListAnimeAction animeAction;

        public AnimeListActions(Context mContext, KeyUtils.ActionType mActionType, Payload.ListAnimeAction animeAction, Callback<Object> mCallback) {
            this.mContext = mContext;
            this.mCallback = mCallback;
            this.mActionType = mActionType;
            this.animeAction = animeAction;
        }

        @Override
        protected void onPostExecute(Call<Object> requestBodyCall) {
            if(requestBodyCall != null)
                requestBodyCall.enqueue(mCallback);
            else
                mCallback.onFailure(null, new Throwable("Post prepareTask for posting action received null"));
        }

        @Override
        protected Call<Object> doInBackground(Void... integers) {
            try {
                UserListModel anime = ServiceGenerator.createService(UserListModel.class, mContext);
                switch (mActionType){
                    case ANIME_LIST_ADD:
                        return anime.addAnimeItem(animeAction.getId(),
                                                  animeAction.getList_status(),
                                                  animeAction.getScore(),
                                                  animeAction.getScore_raw(),
                                                  animeAction.getEpisodes_watched(),
                                                  animeAction.getRewatched(),
                                                  animeAction.getNotes(),
                                                  animeAction.getAdvanced_rating_scores(),
                                                  animeAction.getCustom_lists(),
                                                  animeAction.getHidden_default());
                    case ANIME_LIST_EDIT:
                        return anime.editAnimeItem(animeAction.getId(),
                                                   animeAction.getList_status(),
                                                   animeAction.getScore(),
                                                   animeAction.getScore_raw(),
                                                   animeAction.getEpisodes_watched(),
                                                   animeAction.getRewatched(),
                                                   animeAction.getNotes(),
                                                   animeAction.getAdvanced_rating_scores(),
                                                   animeAction.getCustom_lists(),
                                                   animeAction.getHidden_default());
                    case ANIME_LIST_DELETE:
                        return anime.deleteAnime(animeAction.getId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Currently adds an manga list to your list of manga
     */
    public static class MangaListActions extends AsyncTask<Void, Void, Call<Object>> {

        private Context mContext;
        private Callback<Object> mCallback;
        private KeyUtils.ActionType mActionType;
        private Payload.ListMangaAction mangaAction;

        public MangaListActions(Context mContext, KeyUtils.ActionType mActionType, Payload.ListMangaAction mangaAction, Callback<Object> mCallback) {
            this.mContext = mContext;
            this.mCallback = mCallback;
            this.mActionType = mActionType;
            this.mangaAction = mangaAction;
        }

        @Override
        protected void onPostExecute(Call<Object> requestBodyCall) {
            if(requestBodyCall != null)
                requestBodyCall.enqueue(mCallback);
            else
                mCallback.onFailure(null, new Throwable("Post prepareTask for posting action received null"));
        }

        @Override
        protected Call<Object> doInBackground(Void... integers) {
            try{
                UserListModel manga = ServiceGenerator.createService(UserListModel.class, mContext);
                switch (mActionType){
                    case MANGA_LIST_ADD:
                        return manga.addMangaItem(mangaAction.getId(),
                                                  mangaAction.getList_status(),
                                                  mangaAction.getScore(),
                                                  mangaAction.getScore_raw(),
                                                  mangaAction.getVolumes_read(),
                                                  mangaAction.getChapters_read(),
                                                  mangaAction.getReread(),
                                                  mangaAction.getNotes(),
                                                  mangaAction.getAdvanced_rating_scores(),
                                                  mangaAction.getCustom_lists(),
                                                  mangaAction.getHidden_default());
                    case MANGA_LIST_EDIT:
                        return manga.editMangaItem(mangaAction.getId(),
                                                   mangaAction.getList_status(),
                                                   mangaAction.getScore(),
                                                   mangaAction.getScore_raw(),
                                                   mangaAction.getVolumes_read(),
                                                   mangaAction.getChapters_read(),
                                                   mangaAction.getReread(),
                                                   mangaAction.getNotes(),
                                                   mangaAction.getAdvanced_rating_scores(),
                                                   mangaAction.getCustom_lists(),
                                                   mangaAction.getHidden_default());
                    case MANGA_LIST_DELETE:
                        return manga.deleteManga(mangaAction.getId());
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        }
    }
}