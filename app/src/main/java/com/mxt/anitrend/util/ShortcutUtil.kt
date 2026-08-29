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
import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.index.MainActivity
import org.xmlpull.v1.XmlPullParser

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

    /**
     * Producer-to-route contract: the [MainActivity.EXTRA_ROUTE] value each
     * shortcut type launches with. Every producer intent must carry a route so
     * a cold launch lands on the shortcut's destination instead of the start
     * destination. NFR-004 added the Airing, Feed, and Trending producers.
     */
    @VisibleForTesting
    internal fun routeForShortcutType(shortcutType: Int): String? = when (shortcutType) {
        KeyUtil.SHORTCUT_NOTIFICATION -> MainActivity.ROUTE_NOTIFICATIONS
        KeyUtil.SHORTCUT_MY_ANIME, KeyUtil.SHORTCUT_MY_MANGA -> MainActivity.ROUTE_MEDIA_LIST
        KeyUtil.SHORTCUT_PROFILE -> MainActivity.ROUTE_PROFILE
        KeyUtil.SHORTCUT_SEARCH -> MainActivity.ROUTE_SEARCH
        KeyUtil.SHORTCUT_AIRING -> MainActivity.ROUTE_AIRING
        KeyUtil.SHORTCUT_FEEDS -> MainActivity.ROUTE_FEED
        KeyUtil.SHORTCUT_TRENDING -> MainActivity.ROUTE_TRENDING
        else -> null
    }

    /**
     * NFR-004: deterministic registration priority for the dynamic shortcut
     * set. The launcher budget is runtime-configured and shared between static
     * and dynamic shortcuts, and `setDynamicShortcuts` throws when it is
     * exceeded, so the earliest entries win whenever the budget cannot fit the
     * full set. The four legacy producers keep their pre-existing registration
     * order; the newly routable Airing, Feed, and Trending producers follow so
     * they register whenever capacity allows.
     */
    @VisibleForTesting
    internal val DYNAMIC_SHORTCUT_PRIORITY: List<Int> = listOf(
        KeyUtil.SHORTCUT_NOTIFICATION,
        KeyUtil.SHORTCUT_MY_ANIME,
        KeyUtil.SHORTCUT_MY_MANGA,
        KeyUtil.SHORTCUT_PROFILE,
        KeyUtil.SHORTCUT_AIRING,
        KeyUtil.SHORTCUT_FEEDS,
        KeyUtil.SHORTCUT_TRENDING,
    )

    /**
     * NFR-004: the dynamic shortcut budget is the launcher's max shortcut
     * count minus the manifest static shortcuts, because the platform shares
     * the limit between static and dynamic shortcuts. Never negative: an
     * over-budget launcher yields zero registrations instead of an exception.
     */
    @VisibleForTesting
    internal fun dynamicShortcutBudget(maxShortcutCount: Int, staticShortcutCount: Int): Int = (maxShortcutCount - staticShortcutCount).coerceAtLeast(0)

    private fun priorityOf(@KeyUtil.ShortcutType shortcutType: Int): Int = DYNAMIC_SHORTCUT_PRIORITY.indexOf(shortcutType).let { if (it < 0) Int.MAX_VALUE else it }

    /**
     * NFR-004: deterministic budget selection. Builders are ordered by
     * [DYNAMIC_SHORTCUT_PRIORITY] and truncated to [budget], so the registered
     * set never depends on caller order and never exceeds the launcher budget.
     */
    @VisibleForTesting
    internal fun selectShortcutBuilders(
        builders: List<ShortcutBuilder>,
        budget: Int,
    ): List<ShortcutBuilder> = builders.sortedBy { priorityOf(it.shortcutType) }.take(budget)

    /**
     * Counts the manifest static shortcuts. The platform shares the launcher
     * budget between static and dynamic shortcuts, so the dynamic budget must
     * subtract them; the count is read from the committed shortcuts resource
     * (`@xml/shortcuts`) so enabling a static shortcut cannot silently push a
     * dynamic registration over the limit.
     */
    @JvmStatic
    fun countStaticShortcuts(context: Context): Int {
        val parser = context.resources.getXml(R.xml.shortcuts)
        var count = 0
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "shortcut") {
                count++
            }
            eventType = parser.next()
        }
        parser.close()
        return count
    }

    @JvmStatic
    fun getShortcutManager(context: Context): ShortcutManager = context.getSystemService(ShortcutManager::class.java)

    /**
     * NFR-004: publishes the dynamic shortcut set with replacement semantics.
     * The selected set is authoritative: [ShortcutManager.setDynamicShortcuts]
     * replaces any previously registered dynamic shortcuts, so lower-priority
     * registrations from an earlier call cannot outlive the current selection.
     * An empty selection (for example a zero dynamic budget) clears the
     * existing dynamic set instead of leaving stale shortcuts installed.
     * Returns whether the publish call succeeded.
     */
    @JvmStatic
    fun createShortcuts(
        context: Context,
        vararg builders: ShortcutBuilder,
    ): Boolean {
        val shortcutManager = getShortcutManager(context)
        val budget = dynamicShortcutBudget(shortcutManager.maxShortcutCountPerActivity, countStaticShortcuts(context))
        val selected = selectShortcutBuilders(builders.toList(), budget)
        val shortcutInfo = ArrayList<ShortcutInfo>(selected.size)
        for (shortcutBuilder in selected) {
            shortcutInfo.add(buildShortcutInfo(context, shortcutBuilder) ?: return false)
        }
        // The selected set is authoritative; an empty set clears the dynamic
        // shortcuts instead of returning without cleanup.
        return shortcutManager.setDynamicShortcuts(shortcutInfo)
    }

    /**
     * Builds the [ShortcutInfo] for one producer. The produced intent carries
     * the producer's params, its [MainActivity.EXTRA_ROUTE] so a cold launch
     * lands on the shortcut's destination, and the legacy
     * [KeyUtil.arg_shortcut_used] marker. Returns null for unknown types.
     */
    @VisibleForTesting
    internal fun buildShortcutInfo(
        context: Context,
        shortcutBuilder: ShortcutBuilder,
    ): ShortcutInfo? {
        val intent = createIntentAction(context, MainActivity::class.java, shortcutBuilder.params)
        routeForShortcutType(shortcutBuilder.shortcutType)?.let { route ->
            intent.putExtra(MainActivity.EXTRA_ROUTE, route)
        }
        return when (shortcutBuilder.shortcutType) {
            KeyUtil.SHORTCUT_NOTIFICATION ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.menu_title_notifications))
                    .setDisabledMessage(context.getString(R.string.info_login_req))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_notifications))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_AIRING ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.drawer_title_airing))
                    .setDisabledMessage(context.getString(R.string.info_login_req))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_airing))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_MY_ANIME ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.drawer_title_myanime))
                    .setDisabledMessage(context.getString(R.string.info_login_req))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_anime))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_MY_MANGA ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.drawer_title_mymanga))
                    .setDisabledMessage(context.getString(R.string.info_login_req))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_manga))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_FEEDS ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.drawer_title_home))
                    .setDisabledMessage(context.getString(R.string.info_login_req))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_feeds))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_PROFILE ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.drawer_title_profile))
                    .setDisabledMessage(context.getString(R.string.info_login_req))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_profile))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_SEARCH ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.action_search))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_search))
                    .setIntent(intent)
                    .build()
            KeyUtil.SHORTCUT_TRENDING ->
                ShortcutInfo
                    .Builder(context, KeyUtil.ShortcutTypes[shortcutBuilder.shortcutType])
                    .setShortLabel(context.getString(R.string.drawer_title_trending))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_trending))
                    .setIntent(intent)
                    .build()
            else -> null
        }
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
