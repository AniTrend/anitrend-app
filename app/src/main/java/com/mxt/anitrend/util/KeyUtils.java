package com.mxt.anitrend.util;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

/**
 * Created by max on 2017/09/16.
 * Key values to be used throughout the application
 */

public interface KeyUtils {

    /** Default Values */
    float AspectRatio = 1.37f;
    float WideAspectRatio = 0.95f;
    float PEEK_HEIGHT = 200f;
    int AIRING_LIMIT = 22, PAGING_LIMIT = 24, GLIDE_REQUEST_TIMEOUT = 10000;

    /** Notification Channels */
    String CHANNEL_ID = "anitrend_app";
    String CHANNEL_TITLE = "AniTrend Notifications";

    /** Base Application Args */
    String arg_id = "arg_id";
    String arg_page = "arg_page";
    String arg_text = "arg_text";
    String arg_feed = "arg_feed";
    String arg_title = "arg_title";
    String arg_model = "arg_model";
    String arg_rating = "arg_rating";
    String arg_popular = "arg_popular";
    String arg_user_id = "arg_user_id";
    String arg_redirect = "arg_redirect";
    String arg_user_name = "arg_user_name";
    String arg_activity_id = "arg_user_id";
    String arg_user_model = "arg_user_model";
    String arg_list_model = "arg_list_model";
    String arg_branch_name = "arg_branch_name";
    String arg_review_type = "arg_review_type";
    String arg_page_offset = "arg_page_offset";
    String arg_request_type = "arg_request_type";
    String arg_search_query = "arg_search_query";
    String arg_recipient_id = "arg_recipient_id";
    String arg_activity_tag = "arg_activity_tag";
    String arg_shortcut_used = "arg_shortcut_used";
    String arg_deep_link_type = "arg_deep_link_type";
    String arg_graph_params = "arg_graph_params";

    String arg_positive_text = "arg_positive_text";
    String arg_negative_text = "arg_negative_text";

    /** Application State Keys */
    String key_recycler_state = "key_recycler_state";
    String key_model_state = "key_model_state";
    String key_pagination = "key_pagination";
    String key_columns = "key_columns";
    String key_page_limit = "key_page_limit";
    String key_navigation_selected = "key_navigation_selected";
    String key_navigation_title = "key_navigation_title";
    String key_bundle_param = "key_bundle_param";
    String key_analytics_error = "key_analytics_error";

    /** Browse Keys */
    String arg_series_tag = "arg_series_tag";
    String arg_series_year = "arg_series_year";
    String arg_series_type = "arg_series_type";
    String arg_season_title = "arg_season_title";
    String arg_series_season = "arg_series_season";
    String arg_series_status = "arg_series_status";
    String arg_series_genres = "arg_series_genres";
    String arg_series_sort_by = "arg_series_sort_by";
    String arg_series_order_by = "arg_series_order_by";
    String arg_series_show_type = "arg_series_show_type";
    String arg_series_airing_data = "arg_series_airing_data";
    String arg_series_tag_exclude = "arg_series_tag_exclude";
    String arg_series_genres_exclude = "arg_series_genres_exclude";

    /** Sort Types */
    String key_sort_title = "title_romaji";
    String key_sort_score = "score";
    String key_sort_popularity = "popularity";
    String key_sort_start_date = "start_date";
    String key_sort_end_date = "end_date";

    /** Media List Keys */
    String arg_list_status = "arg_list_status";
    String arg_list_score = "arg_list_score";
    String arg_list_score_raw = "arg_list_score_raw";
    String arg_list_notes = "arg_list_notes";
    String arg_list_advanced_rating = "arg_list_advanced_rating";
    String arg_list_custom_list = "arg_list_custom_list";
    String arg_list_hidden = "arg_list_hidden";

    String arg_list_watched = "arg_list_watched";
    String arg_list_re_watched = "arg_list_re_watched";

    String arg_list_read = "arg_list_read";
    String arg_list_re_read = "arg_list_re_read";
    String arg_list_volumes = "arg_list_volumes";

    /** keys to use for series list fragment */
    String[] AnimeListKeys = {"watching","plan_to_watch","completed","on_hold","dropped"};
    String[] MangaListKeys = {"reading","plan_to_read","completed","on_hold","dropped"};


    /** Alerter Durations */
    long DURATION_SHORT = 2000L, DURATION_MEDIUM = 3500L, DURATION_LONG = 6500L;

    @IntDef({DURATION_SHORT, DURATION_MEDIUM, DURATION_LONG})
    @interface AlerterDuration {}


    /** Request types */
    int GENRE_LIST_REQ = 1, TAG_LIST_REQ = 2, EPISODE_LATEST_REQ = 3, EPISODE_POPULAR_REQ = 4, EPISODE_FEED_REQ = 5,
        UPDATE_CHECKER_REQ = 6, GIPHY_TRENDING_REQ = 7, GIPHY_SEARCH_REQ = 8;

    @IntDef({GENRE_LIST_REQ, TAG_LIST_REQ, EPISODE_LATEST_REQ, EPISODE_POPULAR_REQ, EPISODE_FEED_REQ,
            UPDATE_CHECKER_REQ, GIPHY_TRENDING_REQ, GIPHY_SEARCH_REQ
    })
    @interface RequestMode {}

    // Unaffiliated values
    String[] AnimeMediaTypes = {null, "Tv","Tv Short","Movie","Special","OVA","ONA"};
    String[] MangaMediaTypes = {null, "Manga","Novel","One Shot","Doujin","Manhua","Manhwa"};

    // Deep link types
    String DEEP_LINK_USER = "user", DEEP_LINK_MANGA = "manga", DEEP_LINK_ANIME = "anime",
            DEEP_LINK_CHARACTER = "character", DEEP_LINK_STAFF = "staff", DEEP_LINK_ACTOR = "actor";

    @StringDef({DEEP_LINK_USER, DEEP_LINK_MANGA, DEEP_LINK_ANIME, DEEP_LINK_CHARACTER, DEEP_LINK_STAFF, DEEP_LINK_ACTOR})
    @interface DeepLinkType {}


    String MD_BOLD = "__", MD_ITALIC = "_", MD_STRIKE = "~~",
            MD_NUMBER = "1. ", MD_BULLET = "- ", MD_HEADING = "# ",
            MD_CENTER_ALIGN = "~~~", MD_QUOTE = "> " ,MD_CODE = "`";

    // Share types
    int PLAIN_TYPE = 0, LINK_TYPE = 1, IMAGE_TYPE = 2, YOUTUBE_TYPE = 3, WEBM_TYPE = 4;

    String[] ShareTypes = {"plain_text", "link", "image", "youtube", "webm"};
    @IntDef({PLAIN_TYPE, LINK_TYPE, IMAGE_TYPE, YOUTUBE_TYPE, WEBM_TYPE})
    @interface ShareType {}

    // Hub request types
    int FEED_TYPE = 0, RSS_TYPE = 1, PLAYLIST_TYPE = 2, VIDEO_TYPE = 3, PLAYLIST_COLLECTION = 4;

    @IntDef({FEED_TYPE, RSS_TYPE, PLAYLIST_TYPE, VIDEO_TYPE, PLAYLIST_COLLECTION})
    @interface HubType {}

    // Token grant types
    int AUTHENTICATION_TYPE = 0, AUTHENTICATION_CODE = 1, REFRESH_TYPE = 2;

    String[] GrantTypes = {"client_credentials", "authorization_code", "refresh_token"};
    @IntDef({AUTHENTICATION_TYPE, AUTHENTICATION_CODE, REFRESH_TYPE})
    @interface GrantType {}

    // Media Types
    int ANIME = 0, MANGA = 1;

    String[] SeriesTypes = {"anime", "manga"};
    @IntDef({ANIME, MANGA})
    @interface SeriesType {}

    // Season Types
    int WINTER = 0, SPRING = 1, SUMMER = 2, FALL = 3;

    String[] SeasonTitles = {"winter","spring","summer","fall"};
    @IntDef({WINTER, SPRING, SUMMER, FALL})
    @interface SeasonType {}


    // Activity types
    int PROGRESS = 0, STATUS = 1, PUBIC_STATUS = 2, MESSAGE = 3, LIST = 4;

    String[] ActivityTypes = { "series", "text", "public-status-replies", "message", "list"};
    @IntDef({PROGRESS, STATUS, PUBIC_STATUS, MESSAGE, LIST})
    @interface ActivityType {}

    // Review Types
    int LATEST = 0, POPULAR = 1, NEED_LOVE = 2, CONTROVERSIAL = 3;

    String[] ReviewTypes = { "latest", "popular", "love", "controversial" };
    @IntDef({LATEST,POPULAR,NEED_LOVE,CONTROVERSIAL})
    @interface ReviewType {}

    // Review Request Types
    int SERIES_REVIEW_ANIME = 0, SERIES_REVIEW_MANGA = 1;

    @IntDef({SERIES_REVIEW_ANIME, SERIES_REVIEW_MANGA})
    @interface SeriesReviewType {}

    // Review Status
    int NO_RATING = 0, UP_VOTE = 1, DOWN_VOTE = 2;

    @IntDef({NO_RATING, UP_VOTE, DOWN_VOTE})
    @interface ReviewStatus {}

    // Share series status types
    int ALL_ITEMS = 0, CANCELLED = 4; String key_cancelled = "cancelled";

    String key_finished_airing = "finished airing";
    String key_currently_airing = "currently airing";
    String key_not_yet_aired = "not yet aired";

    // Anime status types
    int FINISHED_AIRING = 1, CURRENTLY_AIRING = 2, NOT_YET_AIRED = 3;

    String[] AnimeStatusTypes = {null,key_finished_airing,key_currently_airing,key_not_yet_aired,key_cancelled};
    @IntDef({ALL_ITEMS, FINISHED_AIRING, CURRENTLY_AIRING, NOT_YET_AIRED, CANCELLED})
    @interface AnimeStatusType {}


    // Manga status types
    int FINISHED_PUBLISHING = 1, PUBLISHING = 2, NOT_YET_PUBLISHED = 3;

    String key_finished_publishing = "finished publishing";
    String key_publishing = "publishing";
    String key_not_yet_published = "not yet published";

    String[] MangaStatusTypes = {null,key_finished_publishing,key_publishing,key_not_yet_published,key_cancelled};
    @IntDef({ALL_ITEMS ,FINISHED_PUBLISHING, PUBLISHING, NOT_YET_PUBLISHED, CANCELLED})
    @interface MangaStatusType {}


    // Season sorting types
    int TITLE = 0 ,SCORE = 1, POPULARITY = 2, START_DATE = 3, END_DATE = 4;

    String[] SeriesSortTypes = { "title_romaji" ,"score", "popularity", "start_date" ,"end_date" };
    @IntDef({TITLE ,SCORE, POPULARITY, START_DATE, END_DATE})
    @interface SeriesSortType {}


    // Order types
    int ASC = 0, DESC = 1;

    String[] OrderTypes = { "asc", "desc" };
    @IntDef({ASC, DESC})
    @interface OrderType {}


    // Shared int defs
    int COMPLETED = 2, ON_HOLD = 3, DROPPED = 4;

    // Anime Status
    int PLAN_TO_WATCH = 1,  WATCHING= 0;

    String[] UserAnimeStatus = {"watching","plan to watch","completed","on-hold","dropped"};
    @IntDef({PLAN_TO_WATCH, WATCHING, COMPLETED, ON_HOLD, DROPPED})
    @interface UserAnimeStatusKey {}


    // Manga Status
    int PLAN_TO_READ = 1, READING = 0;

    String[] UserMangaStatus = {"reading","plan to read","completed","on-hold","dropped"};
    @IntDef({PLAN_TO_READ, READING, COMPLETED, ON_HOLD, DROPPED})
    @interface UserMangaStatusKey {}

    // User List Keys
    int KEY_ON_HOLD = 0, KEY_PLAN_TO_READ = 1, KEY_DROPPED = 2, KEY_COMPLETED = 3,
            KEY_READING = 4, KEY_WATCHING = 5, KEY_PLAN_TO_WATCH = 6;

    String[] UserListKeys = {"on_hold", "plan_to_read", "dropped", "completed", "reading", "watching", "plan_to_watch"};
    @IntDef({KEY_ON_HOLD, KEY_PLAN_TO_READ, KEY_DROPPED, KEY_COMPLETED, KEY_READING, KEY_WATCHING, KEY_PLAN_TO_WATCH})
    @interface UserListType {}


    // Application shortcuts
    int SHORTCUT_SEARCH = 0, SHORTCUT_NOTIFICATION = 1, SHORTCUT_AIRING = 2, SHORTCUT_TRENDING = 3, SHORTCUT_MY_ANIME = 4,
            SHORTCUT_MY_MANGA = 5, SHORTCUT_FEEDS = 6, SHORTCUT_PROFILE = 7;

    String[] ShortcutTypes = {"SHORTCUT_SEARCH", "SHORTCUT_NOTIFICATION", "SHORTCUT_AIRING", "SHORTCUT_TRENDING",
            "SHORTCUT_ANIME", "SHORTCUT_MANGA", "SHORTCUT_FEEDS", "SHORTCUT_PROFILE"};
    @IntDef({SHORTCUT_SEARCH, SHORTCUT_NOTIFICATION, SHORTCUT_AIRING, SHORTCUT_TRENDING, SHORTCUT_MY_ANIME, SHORTCUT_MY_MANGA,
            SHORTCUT_FEEDS, SHORTCUT_PROFILE})
    @interface ShortcutType {}


    // TitleBase preferences for series
    int LANGUAGE_ROMAJI = 0, LANGUAGE_ENGLISH = 1, LANGUAGE_JAPANESE = 2;

    String key_language_romaji = "romaji", key_language_english = "english", key_language_japanese = "japanese";

    @IntDef({LANGUAGE_ROMAJI, LANGUAGE_ENGLISH, LANGUAGE_JAPANESE})
    @interface LanguageType {}

    // Notification Types
    int NOTIFICATION_DIRECT_MESSAGE = 2, NOTIFICATION_REPLY_ACTIVITY = 3, NOTIFICATION_FOLLOW_ACTIVITY = 4, NOTIFICATION_MENTION_ACTIVITY = 5,
    NOTIFICATION_COMMENT_FORUM = 7, NOTIFICATION_REPLY_FORUM = 8, NOTIFICATION_AIRING = 9, NOTIFICATION_LIKE_ACTIVITY = 10,
    NOTIFICATION_LIKE_ACTIVITY_REPLY = 11, NOTIFICATION_LIKE_FORUM = 12, NOTIFICATION_LIKE_FORUM_COMMENT = 13;


    @IntDef({NOTIFICATION_DIRECT_MESSAGE, NOTIFICATION_REPLY_ACTIVITY, NOTIFICATION_FOLLOW_ACTIVITY, NOTIFICATION_MENTION_ACTIVITY,
            NOTIFICATION_COMMENT_FORUM, NOTIFICATION_REPLY_FORUM, NOTIFICATION_AIRING, NOTIFICATION_LIKE_ACTIVITY,
            NOTIFICATION_LIKE_ACTIVITY_REPLY, NOTIFICATION_LIKE_FORUM, NOTIFICATION_LIKE_FORUM_COMMENT})
    @interface NotificationType {}

    // Giphy types
    int GIPHY_LARGE_DOWN_SAMPLE = 0, GIPHY_ORIGINAL_STILL = 1, GIPHY_ORIGINAL_ANIMATED = 2, GIPHY_PREVIEW = 3;

    String[] GiphyTypes = { "downsized_large", "original_still", "original", "preview_gif" };
    @IntDef({GIPHY_LARGE_DOWN_SAMPLE, GIPHY_ORIGINAL_STILL, GIPHY_ORIGINAL_ANIMATED, GIPHY_PREVIEW})
    @interface GiphyType {}


    // Time unit conversion place identifiers
    int TIME_UNIT_DAYS = 0, TIME_UNIT_HOURS = 1, TIME_UNIT_MINUTES = 2, TIME_UNITS_SECONDS = 3;

    @IntDef({TIME_UNIT_DAYS, TIME_UNIT_HOURS, TIME_UNIT_MINUTES, TIME_UNITS_SECONDS})
    @interface TimeTargetType {}


    // Group types for recycler view
    int RECYCLER_TYPE_CONTENT = 0x00000010,  RECYCLER_TYPE_HEADER = 0x00000011, RECYCLER_TYPE_LOADING = 0x00000100, RECYCLER_TYPE_EMPTY = 0x00000101;

    @IntDef({RECYCLER_TYPE_CONTENT, RECYCLER_TYPE_HEADER, RECYCLER_TYPE_LOADING, RECYCLER_TYPE_EMPTY})
    @interface RecyclerViewType {}

    /** Application Tips */
    String KEY_MAIN_TIP = "KEY_MAIN_TIP", KEY_DETAIL_TIP = "KEY_DETAIL_TIP", KEY_NOTIFICATION_TIP = "KEY_NOTIFICATION_TIP",
    KEY_MESSAGE_TIP = "KEY_MESSAGE_TIP", KEY_COMPOSE_TIP = "KEY_COMPOSE_TIP", KEY_CHARACTER_TIP = "KEY_CHARACTER_TIP",
    KEY_STAFF_TIP = "KEY_STAFF_TIP", KEY_STATUS_POST_TIP = "KEY_STATUS_POST_TIP", KEY_USER_PROFILE_TIP = "KEY_USER_PROFILE_TIP",
    KEY_REVIEW_TYPE_TIP = "KEY_REVIEW_TYPE_TIP", KEY_LOGIN_TIP = "KEY_LOGIN_TIP", KEY_GIPHY_TIP = "KEY_GIPHY_TIP",
    KEY_POST_TYPE_TIP = "KEY_POST_TYPE_TIP";

    @StringDef({KEY_MAIN_TIP, KEY_DETAIL_TIP, KEY_NOTIFICATION_TIP, KEY_MESSAGE_TIP, KEY_COMPOSE_TIP, KEY_CHARACTER_TIP,
            KEY_STAFF_TIP, KEY_STATUS_POST_TIP, KEY_USER_PROFILE_TIP, KEY_REVIEW_TYPE_TIP, KEY_LOGIN_TIP, KEY_GIPHY_TIP,
            KEY_POST_TYPE_TIP})
    @interface TapTargetType {}


    String ROMAJI = "ROMAJI", ENGLISH = "ENGLISH", NATIVE = "NATIVE",
            ROMAJI_STYLISED = "ROMAJI_STYLISED",
            ENGLISH_STYLISED = "ENGLISH_STYLISED",
            NATIVE_STYLISED = "NATIVE_STYLISED";

    @StringDef({ROMAJI, ENGLISH, NATIVE, ROMAJI_STYLISED,
            ENGLISH_STYLISED, NATIVE_STYLISED})
    @interface UserLanguageTitle {}
}
