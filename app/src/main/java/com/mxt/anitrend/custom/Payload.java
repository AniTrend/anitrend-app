package com.mxt.anitrend.custom;

import java.io.Serializable;

/**
 * Created by Maxwell on 11/12/2016.
 * Payload types
 */

public final class Payload {

    /**
     * Use this to create/edit an activity message
     * */
    public static class ActivityMessage {

        private int id;
        private String text;
        private int messenger_id;
        private int user_id;

        public String getText() {
            return text;
        }

        public int getMessenger_id() {
            return messenger_id;
        }

        public int getId() {
            return id;
        }

        public int getUser_id() {
            return user_id;
        }


        /**
         * @param id (int) activity id
         * @param text (string) activity text
         * @param messenger_id (int) recipient user id
         * @param user_id (int) current user id
         */
        public ActivityMessage(int id, int messenger_id, String text, int user_id) {
            this.id = id;
            this.text = text;
            this.messenger_id = messenger_id;
            this.user_id = user_id;
        }

        /**
         * @param text (string) activity text
         * @param messenger_id (int) recipient user id
         */
        public ActivityMessage(String text, int messenger_id) {
            this.text = text;
            this.messenger_id = messenger_id;
        }
    }

    /**
     * Use this to post, edit & reply to activities
     * */
    public static class ActivityStruct {

        private int id;
        private String text;
        private int reply_id;
        private int user_id;

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public int getReply_id() {
            return reply_id;
        }

        public int getUser_id() {
            return user_id;
        }

        /**
         *
         * @param id (int) id of the current activity
         * @param text (string) activity text
         * @param user_id (int) the id of the current user
         * @param reply_id (int) activity id of the current reply
         */
        public ActivityStruct(int id, String text, int user_id, int reply_id) {
            this.id = id;
            this.text = text;
            this.user_id = user_id;
            this.reply_id = reply_id;
        }

        /**
         * @param id (int) id of the current activity
         * @param text (string) activity text
         * @param reply_id (int) activity id of the current reply
         */
        public ActivityStruct(int id, String text, int reply_id) {
            this.id = id;
            this.text = text;
            this.reply_id = reply_id;
        }

        public ActivityStruct(String text) {
            this.text = text;
        }
    }

    /**
     * Use this for all requests with a payload of ID
     * */
    public static class ActionIdBased {

        private final int id;

        public int getId() {
            return id;
        }

        public ActionIdBased(int id) {
            this.id = id;
        }
    }

    /**
     * Use this for rating reviews
     */
    public static class ReviewActions {
        private int id;
        private int rating;

        public int getId() {
            return id;
        }

        public int getRating() {
            return rating;
        }


        /**
         * @param id (int) id of review to rate
         * @param rating (int) 0 no rating, 1 up/positive rating, 2 down/negative rating
         */
        public ReviewActions(int id, int rating) {
            this.id = id;
            this.rating = rating;
        }
    }



    /**
     * id: (int) anime_id of list item
     * list_status: (String) "watching" || "completed" || "on-hold" || "dropped" || "plan to watch"
     * score: (See bottom of page - List score types)
     * score_raw: (int) 0-100 (See bottom of page - Raw score)
     * episodes_watched: (int)
     * rewatched: (int)
     * notes: (String)
     * advanced_rating_scores: comma separated scores, same order as advanced_rating_names
     * custom_lists: comma separated 1 or 0, same order as custom_list_anime
     * hidden_default: (int) 0 || 1
     */
    public static class ListAnimeAction implements Serializable {

        private int id;
        private String list_status;
        private String score;
        private Integer score_raw;
        private Integer episodes_watched;
        private Integer rewatched;
        private String notes;
        private String advanced_rating_scores;
        private Integer custom_lists;
        private Integer hidden_default;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getList_status() {
            return list_status;
        }

        public void setList_status(String list_status) {
            this.list_status = list_status;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public Integer getScore_raw() {
            return score_raw;
        }

        public void setScore_raw(Integer score_raw) {
            this.score_raw = score_raw;
        }

        public Integer getEpisodes_watched() {
            return episodes_watched;
        }

        public void setEpisodes_watched(Integer episodes_watched) {
            this.episodes_watched = episodes_watched;
        }

        public Integer getRewatched() {
            return rewatched;
        }

        public void setRewatched(Integer rewatched) {
            this.rewatched = rewatched;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getAdvanced_rating_scores() {
            return advanced_rating_scores;
        }

        public void setAdvanced_rating_scores(String advanced_rating_scores) {
            this.advanced_rating_scores = advanced_rating_scores;
        }

        public Integer getCustom_lists() {
            return custom_lists;
        }

        public void setCustom_lists(Integer custom_lists) {
            this.custom_lists = custom_lists;
        }

        public Integer getHidden_default() {
            return hidden_default;
        }

        public void setHidden_default(Integer hidden_default) {
            this.hidden_default = hidden_default;
        }
    }

    /**
     * id: (int) manga_id of list item
     * list_status: (String) "reading" || "completed" || "on-hold" || "dropped" || "plan to read"
     * score: (See bottom of page - List score types)
     * score_raw: (int) 0-100 (See bottom of page - Raw score)
     * volumes_read: (int)
     * chapters_read: (int)
     * reread: (int)
     * notes: (String)
     * advanced_rating_scores: comma separated scores, same order as advanced_rating_names
     * custom_lists: comma separated 1 or 0, same order as custom_list_manga
     * hidden_default: (int) 0 || 1
     */
    public static class ListMangaAction {

        private int id;
        private String list_status;
        private String score;
        private Integer score_raw;
        private Integer volumes_read;
        private Integer chapters_read;
        private Integer reread;
        private String notes;
        private String advanced_rating_scores;
        private Integer custom_lists;
        private int hidden_default;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getList_status() {
            return list_status;
        }

        public void setList_status(String list_status) {
            this.list_status = list_status;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public Integer getScore_raw() {
            return score_raw;
        }

        public void setScore_raw(Integer score_raw) {
            this.score_raw = score_raw;
        }

        public Integer getVolumes_read() {
            return volumes_read;
        }

        public void setVolumes_read(Integer volumes_read) {
            this.volumes_read = volumes_read;
        }

        public Integer getChapters_read() {
            return chapters_read;
        }

        public void setChapters_read(Integer chapters_read) {
            this.chapters_read = chapters_read;
        }

        public Integer getReread() {
            return reread;
        }

        public void setReread(Integer reread) {
            this.reread = reread;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getAdvanced_rating_scores() {
            return advanced_rating_scores;
        }

        public void setAdvanced_rating_scores(String advanced_rating_scores) {
            this.advanced_rating_scores = advanced_rating_scores;
        }

        public Integer getCustom_lists() {
            return custom_lists;
        }

        public void setCustom_lists(Integer custom_lists) {
            this.custom_lists = custom_lists;
        }

        public int getHidden_default() {
            return hidden_default;
        }

        public void setHidden_default(int hidden_default) {
            this.hidden_default = hidden_default;
        }
    }
}
