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
    int PAGING_LIMIT = 21, GLIDE_REQUEST_TIMEOUT = 10000;

    /** Notification Channels */
    String CHANNEL_ID = "anitrend_app";
    String CHANNEL_TITLE = "AniTrend Notifications";

    /** Base Application Args */
    String arg_id = "arg_id";
    String arg_page = "page";
    String arg_per_page = "perPage";
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


    /** Request types */
    int GENRE_LIST_REQ = 1, TAG_LIST_REQ = 2, EPISODE_LATEST_REQ = 3, EPISODE_POPULAR_REQ = 4, EPISODE_FEED_REQ = 5,
            UPDATE_CHECKER_REQ = 6, GIPHY_TRENDING_REQ = 7, GIPHY_SEARCH_REQ = 8,

    MEDIA_BROWSE_REQ = 10, MEDIA_LIST_REQ = 11, MEDIA_LIST_BROWSE_REQ = 12,
            MEDIA_LIST_SAVE = 13, MEDIA_LIST_UPDATE = 14, MEDIA_LIST_DELETE = 15 ;

    @IntDef({GENRE_LIST_REQ, TAG_LIST_REQ, EPISODE_LATEST_REQ, EPISODE_POPULAR_REQ, EPISODE_FEED_REQ,
            UPDATE_CHECKER_REQ, GIPHY_TRENDING_REQ, GIPHY_SEARCH_REQ, MEDIA_BROWSE_REQ,
            MEDIA_LIST_REQ, MEDIA_LIST_BROWSE_REQ, MEDIA_LIST_SAVE, MEDIA_LIST_UPDATE, MEDIA_LIST_DELETE
    })
    @interface RequestMode {}

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
    String AUTHENTICATION_TYPE = "client_credentials", AUTHENTICATION_CODE = "authorization_code", REFRESH_TYPE = "refresh_token";
    @StringDef({AUTHENTICATION_TYPE, AUTHENTICATION_CODE, REFRESH_TYPE})
    @interface GrantType {}

    // Language Title Preference
    String ROMAJI = "ROMAJI", ENGLISH = "ENGLISH", NATIVE = "NATIVE",
            ROMAJI_STYLISED = "ROMAJI_STYLISED", ENGLISH_STYLISED = "ENGLISH_STYLISED",
            NATIVE_STYLISED = "NATIVE_STYLISED";

    @StringDef({ROMAJI, ENGLISH, NATIVE, ROMAJI_STYLISED, ENGLISH_STYLISED, NATIVE_STYLISED})
    @interface UserLanguageTitle {}


    // ------------------------------------------------------------------------------------
    // Sorting & Order Type Attributes
    // ------------------------------------------------------------------------------------

    String ID = "ID", MEDIA_ID = "MEDIA_ID", SCORE = "SCORE", STATUS = "STATUS", SEARCH_MATCH = "SEARCH_MATCH", ROLE = "ROLE", LANGUAGE = "LANGUAGE";


    String ASC = "", DESC = "_DESC";
    @StringDef({ASC, DESC})
    @interface SortOrderType {}

    String TITLE_ROMAJI = "TITLE_ROMAJI", TITLE_ENGLISH = "TITLE_ENGLISH", TITLE_NATIVE = "TITLE_NATIVE", TYPE = "TYPE",
            FORMAT = "FORMAT", START_DATE = "START_DATE", END_DATE = "END_DATE", POPULARITY = "POPULARITY", TRENDING = "TRENDING",
            EPISODES = "EPISODES", DURATION = "DURATION", CHAPTERS = "CHAPTERS", VOLUMES = "VOLUMES";
    @StringDef({ID, TITLE_ROMAJI, TITLE_ENGLISH, TITLE_NATIVE, TYPE, FORMAT, START_DATE, END_DATE,
            SCORE, POPULARITY, TRENDING, EPISODES, DURATION, STATUS, CHAPTERS, VOLUMES})
    @interface MediaSort {}


    String PROGRESS = "PROGRESS",PROGRESS_VOLUMES = "PROGRESS_VOLUMES", REPEAT = "REPEAT", PRIORITY = "PRIORITY", STARTED_ON = "STARTED_ON",
            FINISHED_ON = "FINISHED_ON", ADDED_TIME = "ADDED_TIME", UPDATED_TIME = "UPDATED_TIME";
    @StringDef({MEDIA_ID, SCORE, STATUS, PROGRESS, PROGRESS_VOLUMES, REPEAT, PRIORITY, STARTED_ON, FINISHED_ON, ADDED_TIME, UPDATED_TIME})
    @interface MediaListSort {}


    String RATING = "RATING", CREATED_AT = "CREATED_AT", UPDATED_AT = "UPDATED_AT";
    @StringDef({ID, SCORE, RATING, CREATED_AT, UPDATED_AT})
    @interface ReviewSort {}


    String TIME = "TIME", EPISODE = "EPISODE";
    @StringDef({ID, MEDIA_ID, TIME, EPISODE})
    @interface AiringSort {}


    @StringDef({ID, ROLE, SEARCH_MATCH})
    @interface CharacterSort {}


    String[] SortOrderTypes = {ASC, DESC};
    String[] MediaSortTypes = {ID, TITLE_ROMAJI, TITLE_ENGLISH, TITLE_NATIVE, TYPE, FORMAT, START_DATE, END_DATE, SCORE, POPULARITY, TRENDING, EPISODES, DURATION, STATUS, CHAPTERS, VOLUMES};
    String[] MediaListSortTypes = {MEDIA_ID, SCORE, STATUS, PROGRESS, PROGRESS_VOLUMES, REPEAT, PRIORITY, STARTED_ON, FINISHED_ON, ADDED_TIME, UPDATED_TIME};
    String[] ReviewSortTypes = {ID, SCORE, RATING, CREATED_AT, UPDATED_AT};
    String[] AiringSortTypes = {ID, MEDIA_ID, TIME, EPISODE};
    String[] CharacterSortTypes = {ID, ROLE, SEARCH_MATCH};

    // ------------------------------------------------------------------------------------




    // ------------------------------------------------------------------------------------
    // Media Type Attributes
    // ------------------------------------------------------------------------------------

    String ANIME = "ANIME", MANGA = "MANGA";
    @StringDef({ANIME, MANGA})
    @interface MediaType {}


    String WINTER = "WINTER", SPRING = "SPRING", SUMMER = "SUMMER", FALL = "FALL";
    @StringDef({WINTER, SPRING, SUMMER, FALL})
    @interface MediaSeason {}


    String FINISHED = "FINISHED", RELEASING = "RELEASING",NOT_YET_RELEASED = "NOT_YET_RELEASED",
            CANCELLED = "CANCELLED";
    @StringDef({FINISHED, RELEASING, NOT_YET_RELEASED, CANCELLED})
    @interface MediaStatus {}


    String ORIGINAL = "ORIGINAL", LIGHT_NOVEL = "LIGHT_NOVEL",
            VISUAL_NOVEL = "VISUAL_NOVEL", VIDEO_GAME = "VIDEO_GAME", OTHER = "OTHER";
    @StringDef({ORIGINAL, MANGA, LIGHT_NOVEL, VISUAL_NOVEL, VIDEO_GAME, OTHER})
    @interface MediaSource {}


    String TV = "TV", TV_SHORT = "TV_SHORT", MOVIE = "MOVIE", SPECIAL = "SPECIAL",
            OVA = "OVA", ONA = "ONA", MUSIC = "MUSIC", NOVEL = "NOVEL", ONE_SHOT = "ONE_SHOT";
    @StringDef({TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, MANGA, NOVEL, ONE_SHOT})
    @interface MediaFormat {}


    String[] MediaSeasons = {WINTER, SPRING, SUMMER, FALL};
    String[] MediaSources = {null, ORIGINAL, MANGA, LIGHT_NOVEL, VISUAL_NOVEL, VIDEO_GAME, OTHER};
    String[] MediaFormats = {null, TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, MANGA, NOVEL, ONE_SHOT};

    // ------------------------------------------------------------------------------------




    // ------------------------------------------------------------------------------------
    // MediaList Type Attributes
    // ------------------------------------------------------------------------------------

    String CURRENT = "CURRENT", PLANNING = "PLANNING", COMPLETED = "COMPLETED",
            DROPPED = "DROPPED", PAUSED = "PAUSED", REPEATING = "REPEATING";
    @StringDef({CURRENT, PLANNING, COMPLETED, DROPPED, PAUSED, REPEATING})
    @interface MediaListStatus {}

    // ------------------------------------------------------------------------------------




    // ------------------------------------------------------------------------------------
    // Feed Type Attributes
    // ------------------------------------------------------------------------------------

    String TEXT = "TEXT", ANIME_LIST = "ANIME_LIST", MANGA_LIST = "MANGA_LIST",
            MESSAGE = "MESSAGE", MEDIA_LIST = "MEDIA_LIST";
    @StringDef({TEXT, ANIME_LIST, MANGA_LIST, MESSAGE, MEDIA_LIST})
    @interface FeedType {}

    // ------------------------------------------------------------------------------------



    // ------------------------------------------------------------------------------------
    // Review Type Attributes
    // ------------------------------------------------------------------------------------

    String NO_VOTE = "NO_VOTE", UP_VOTE = "UP_VOTE", DOWN_VOTE = "DOWN_VOTE";
    @StringDef({NO_VOTE, UP_VOTE, DOWN_VOTE})
    @interface ReviewRating {}

    // ------------------------------------------------------------------------------------


    /** Alerter Durations */
    long DURATION_SHORT = 2000L, DURATION_MEDIUM = 3500L, DURATION_LONG = 6500L;

    @IntDef({DURATION_SHORT, DURATION_MEDIUM, DURATION_LONG})
    @interface AlerterDuration {}


    int SHORTCUT_SEARCH = 0, SHORTCUT_NOTIFICATION = 1, SHORTCUT_AIRING = 2, SHORTCUT_TRENDING = 3, SHORTCUT_MY_ANIME = 4,
            SHORTCUT_MY_MANGA = 5, SHORTCUT_FEEDS = 6, SHORTCUT_PROFILE = 7;

    String[] ShortcutTypes = {"SHORTCUT_SEARCH", "SHORTCUT_NOTIFICATION", "SHORTCUT_AIRING", "SHORTCUT_TRENDING",
            "SHORTCUT_ANIME", "SHORTCUT_MANGA", "SHORTCUT_FEEDS", "SHORTCUT_PROFILE"};
    @IntDef({SHORTCUT_SEARCH, SHORTCUT_NOTIFICATION, SHORTCUT_AIRING, SHORTCUT_TRENDING, SHORTCUT_MY_ANIME, SHORTCUT_MY_MANGA,
            SHORTCUT_FEEDS, SHORTCUT_PROFILE})
    @interface ShortcutType {}


    String GIPHY_LARGE_DOWN_SAMPLE = "downsized_large", GIPHY_ORIGINAL_STILL = "original_still",
            GIPHY_ORIGINAL_ANIMATED = "original", GIPHY_PREVIEW = "preview_gif";
    @StringDef({GIPHY_LARGE_DOWN_SAMPLE, GIPHY_ORIGINAL_STILL, GIPHY_ORIGINAL_ANIMATED, GIPHY_PREVIEW})
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
}
