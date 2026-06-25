package com.mxt.anitrend.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.detail.MediaListActivity
import com.mxt.anitrend.view.activity.detail.NotificationActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.activity.index.SearchActivity

/**
 * Created by max on 2017/11/06.
 * Application shortcutType helper
 */
@TargetApi(Build.VERSION_CODES.N_MR1)
@RequiresApi(Build.VERSION_CODES.N_MR1)
object ShortcutUtil {
    private fun <S> createIntentAction(
        context: Context,
        targetActivity: Class<S>,
        param: Bundle,
    ): Intent = Intent(context, targetActivity).apply {
        putExtras(param)
        action = Intent.ACTION_VIEW
        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    @JvmStatic
    fun getShortcutManager(context: Context): ShortcutManager = context.getSystemService(ShortcutManager::class.java)

    @JvmStatic
    fun createShortcuts(
        context: Context,
        vararg builders: ShortcutBuilder,
    ): Boolean {
        val shortcutManager = getShortcutManager(context)
        val shortcutInfo = ArrayList<ShortcutInfo>(builders.size)
        for (shortcutBuilder in builders) {
            when (shortcutBuilder.shortcutType) {
                KeyUtil.SHORTCUT_NOTIFICATION ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.menu_title_notifications))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_notifications))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    NotificationActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_AIRING ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.drawer_title_airing))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_airing))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    MainActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_MY_ANIME ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.drawer_title_myanime))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_anime))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    MediaListActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_MY_MANGA ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.drawer_title_mymanga))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_manga))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    MediaListActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_FEEDS ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.drawer_title_home))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_feeds))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    MainActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_PROFILE ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.drawer_title_profile))
                            .setDisabledMessage(context.getString(R.string.info_login_req))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_profile))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    ProfileActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_SEARCH ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.action_search))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_search))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    SearchActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                KeyUtil.SHORTCUT_TRENDING ->
                    shortcutInfo.add(
                        ShortcutInfo
                            .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                            .setShortLabel(context.getString(R.string.drawer_title_trending))
                            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_trending))
                            .setIntent(
                                createIntentAction(
                                    context,
                                    MainActivity::class.java,
                                    shortcutBuilder.params,
                                ),
                            ).build(),
                    )
                else -> return false
            }
        }
        return shortcutManager.addDynamicShortcuts(shortcutInfo)
    }

    @JvmStatic
    fun disableShortcut(
        context: Context,
        @KeyUtil.ShortcutType vararg shortcuts: Int,
    ) {
        val shortcutQueue = ArrayList<String>(shortcuts.size)
        for (shortcut in shortcuts) {
            shortcutQueue.add(KeyUtil.ShortcutTypes[shortcut])
        }
        getShortcutManager(context).disableShortcuts(shortcutQueue)
    }

    @JvmStatic
    fun enableShortcuts(
        context: Context,
        @KeyUtil.ShortcutType vararg shortcuts: Int,
    ) {
        val shortcutQueue = ArrayList<String>(shortcuts.size)
        for (shortcut in shortcuts) {
            shortcutQueue.add(KeyUtil.ShortcutTypes[shortcut])
        }
        getShortcutManager(context).enableShortcuts(shortcutQueue)
    }

    @JvmStatic
    fun reportShortcutUsage(
        context: Context,
        @KeyUtil.ShortcutType shortcutType: Int,
    ) {
        getShortcutManager(context).reportShortcutUsed(KeyUtil.ShortcutTypes[shortcutType])
    }

    @JvmStatic
    fun removeAllDynamicShortcuts(context: Context) {
        getShortcutManager(context).removeAllDynamicShortcuts()
    }

    /** Shortcut Builder Helper Class */
    class ShortcutBuilder {
        @KeyUtil.ShortcutType
        var shortcutType: Int = 0
            private set

        var params: Bundle = Bundle()
            private set

        fun build(): ShortcutBuilder {
            params.putInt(KeyUtil.arg_shortcut_used, shortcutType)
            return this
        }

        fun setShortcutType(
            @KeyUtil.ShortcutType shortcutType: Int,
        ): ShortcutBuilder {
            this.shortcutType = shortcutType
            return this
        }

        fun setShortcutParams(params: Bundle): ShortcutBuilder {
            this.params = params
            return this
        }
    }
}
