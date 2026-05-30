package com.mxt.anitrend.data.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object ShortcutManager {

    private const val SHORTCUT_SEARCH = "shortcut_search"
    private const val SHORTCUT_PROFILE = "shortcut_profile"
    private const val SHORTCUT_BROWSE_ANIME = "shortcut_browse_anime"
    private const val SHORTCUT_BROWSE_MANGA = "shortcut_browse_manga"

    fun initializeShortcuts(context: Context, targetActivityClass: Class<*>) {
        val shortcuts = listOf(
            ShortcutInfoCompat.Builder(context, SHORTCUT_SEARCH)
                .setShortLabel("Search")
                .setLongLabel("Search anime & manga")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_search))
                .setIntent(
                    Intent(Intent.ACTION_VIEW).apply {
                        setClassName(context.packageName, targetActivityClass.name)
                        data = android.net.Uri.parse("anitrend://search")
                    }
                )
                .build(),
            ShortcutInfoCompat.Builder(context, SHORTCUT_PROFILE)
                .setShortLabel("Profile")
                .setLongLabel("View your profile")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_myplaces))
                .setIntent(
                    Intent(Intent.ACTION_VIEW).apply {
                        setClassName(context.packageName, targetActivityClass.name)
                        data = android.net.Uri.parse("anitrend://profile")
                    }
                )
                .build(),
            ShortcutInfoCompat.Builder(context, SHORTCUT_BROWSE_ANIME)
                .setShortLabel("Anime")
                .setLongLabel("Browse trending anime")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_slideshow))
                .setIntent(
                    Intent(Intent.ACTION_VIEW).apply {
                        setClassName(context.packageName, targetActivityClass.name)
                        data = android.net.Uri.parse("anitrend://browse/anime")
                    }
                )
                .build(),
            ShortcutInfoCompat.Builder(context, SHORTCUT_BROWSE_MANGA)
                .setShortLabel("Manga")
                .setLongLabel("Browse trending manga")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_gallery))
                .setIntent(
                    Intent(Intent.ACTION_VIEW).apply {
                        setClassName(context.packageName, targetActivityClass.name)
                        data = android.net.Uri.parse("anitrend://browse/manga")
                    }
                )
                .build(),
        )

        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts)
    }
}
