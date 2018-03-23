package com.mxt.anitrend.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.mxt.anitrend.R;
import com.mxt.anitrend.view.activity.detail.MediaListActivity;
import com.mxt.anitrend.view.activity.detail.NotificationActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;
import com.mxt.anitrend.view.activity.index.MainActivity;
import com.mxt.anitrend.view.activity.index.SearchActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/11/06.
 * Application shortcutType helper
 */

@TargetApi(Build.VERSION_CODES.N_MR1)
@RequiresApi(Build.VERSION_CODES.N_MR1)
public class ShortcutHelper {

    private static <S> Intent createIntentAction(Context context, Class<S> targetActivity, Bundle param) {
        Intent intent = new Intent(context, targetActivity);
        intent.putExtras(param);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public static ShortcutManager getShortcutManager(Context context) {
        return context.getSystemService(ShortcutManager.class);
    }

    public static boolean createShortcuts(Context context, ShortcutBuilder... builders) {
        ShortcutManager shortcutManager = getShortcutManager(context);
        List<ShortcutInfo> shortcutInfo = new ArrayList<>(builders.length);
        for (ShortcutBuilder shortcutBuilder : builders) {
            switch (shortcutBuilder.getShortcutType()) {
                case KeyUtils.SHORTCUT_NOTIFICATION:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.menu_title_notifications))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_notifications))
                            .setIntent(createIntentAction(context, NotificationActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_AIRING:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.drawer_title_airing))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_airing))
                            .setIntent(createIntentAction(context, MainActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_MY_ANIME:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.drawer_title_myanime))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_anime))
                            .setIntent(createIntentAction(context, MediaListActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_MY_MANGA:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.drawer_title_mymanga))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_manga))
                            .setIntent(createIntentAction(context, MediaListActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_FEEDS:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.drawer_title_home))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_feeds))
                            .setIntent(createIntentAction(context, MainActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_PROFILE:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.drawer_title_profile))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_profile))
                            .setIntent(createIntentAction(context, ProfileActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_SEARCH:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.action_search))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_search))
                            .setIntent(createIntentAction(context, SearchActivity.class, shortcutBuilder.getParams())).build());
                    break;
                case KeyUtils.SHORTCUT_TRENDING:
                    shortcutInfo.add(new ShortcutInfo.Builder(context, KeyUtils.ShortcutTypes[shortcutBuilder.getShortcutType()])
                            .setShortLabel(context.getString(R.string.drawer_title_trending))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_trending))
                            .setIntent(createIntentAction(context, MainActivity.class, shortcutBuilder.getParams())).build());
                    break;
                default:
                    return false;
            }
        }
        return shortcutManager.addDynamicShortcuts(shortcutInfo);
    }

    public static void disableShortcut(Context context, @KeyUtils.ShortcutType int... shortcuts) {
        List<String> shortcutQueue = new ArrayList<>(shortcuts.length);
        for (int shortcut: shortcuts)
            shortcutQueue.add(KeyUtils.ShortcutTypes[shortcut]);
        getShortcutManager(context).disableShortcuts(shortcutQueue);
    }

    public static void enableShortcuts(Context context, @KeyUtils.ShortcutType int... shortcuts) {
        List<String> shortcutQueue = new ArrayList<>(shortcuts.length);
        for (int shortcut: shortcuts)
            shortcutQueue.add(KeyUtils.ShortcutTypes[shortcut]);
        getShortcutManager(context).enableShortcuts(shortcutQueue);
    }

    public static void reportShortcutUsage(Context context, @KeyUtils.ShortcutType int shortcutType) {
        getShortcutManager(context).reportShortcutUsed(KeyUtils.ShortcutTypes[shortcutType]);
    }

    public static void removeAllDynamicShortcuts(Context context) {
        getShortcutManager(context).removeAllDynamicShortcuts();
    }

    /** Shortcut Builder Helper Class */
    public static class ShortcutBuilder {

        private @KeyUtils.ShortcutType int shortcutType;
        private @NonNull Bundle params;

        public ShortcutBuilder() {
            params = new Bundle();
        }

        public int getShortcutType() {
            return shortcutType;
        }

        public @NonNull Bundle getParams() {
            return params;
        }

        public ShortcutBuilder build() {
            params.putInt(KeyUtils.arg_shortcut_used, shortcutType);
            return this;
        }

        public ShortcutBuilder setShortcutType(@KeyUtils.ShortcutType int shortcutType) {
            this.shortcutType = shortcutType;
            return this;
        }

        public ShortcutBuilder setShortcutParams(@NonNull Bundle params) {
            this.params = params;
            return this;
        }
    }
}