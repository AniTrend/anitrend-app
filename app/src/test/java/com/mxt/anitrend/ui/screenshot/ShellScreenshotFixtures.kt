package com.mxt.anitrend.ui.screenshot

import android.app.Activity
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import androidx.appcompat.view.SupportMenuInflater
import com.google.android.material.navigation.NavigationBarView
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityMainBinding
import java.util.Locale
import java.util.TimeZone

/**
 * Deterministic fixtures for shell captures. Every fixture establishes state
 * the production surfaces consume and then renders the production layouts; no
 * screenshot-only replica views are created.
 */
internal object ShellScreenshotFixtures {

    /** Deterministic environment: English locale, UTC clocks. */
    fun applyDeterministicEnvironment(activity: Activity) {
        Locale.setDefault(Locale.ENGLISH)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val configuration = Configuration(activity.resources.configuration)
        configuration.setLocale(Locale.ENGLISH)
        activity.resources.updateConfiguration(configuration, activity.resources.displayMetrics)
    }

    /** Applies a font scale to the fixture before inflation. */
    fun applyFontScale(activity: Activity, fontScale: Float) {
        val configuration = Configuration(activity.resources.configuration)
        configuration.fontScale = fontScale
        activity.resources.updateConfiguration(configuration, activity.resources.displayMetrics)
    }

    /** A themed context for surfaces that do not host fragments. */
    fun themed(activity: Activity, themeRes: Int): ContextThemeWrapper =
        ContextThemeWrapper(activity, themeRes)

    /**
     * Lays a surface out at an exact size before capture. Hosts below the
     * resumed state get no window traversal, so captures measure explicitly.
     */
    fun layoutForCapture(view: android.view.View, width: Int, height: Int) {
        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY)
        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, width, height)
    }

    /**
     * Inflates the production main shell for the active window configuration
     * (compact bar or w600dp rail). The theme is applied to the host activity
     * and the activity's own inflater is used so the NavHost fragment factory
     * is available to `FragmentContainerView`.
     */
    fun inflateShell(activity: Activity, themeRes: Int): ActivityMainBinding {
        activity.setTheme(themeRes)
        return ActivityMainBinding.inflate(LayoutInflater.from(activity))
    }

    /** Returns the shell's primary navigation surface (bar or rail). */
    fun primaryNavigation(binding: ActivityMainBinding): NavigationBarView =
        binding.root.findViewById(R.id.primary_navigation)

    /** Inflates the production toolbar menu into the shell toolbar. */
    fun inflateToolbarMenu(binding: ActivityMainBinding): Menu {
        val toolbar = binding.appBarMain.customToolbar.toolbar
        val menu = toolbar.menu
        menu.clear()
        SupportMenuInflater(toolbar.context).inflate(R.menu.main_menu, menu)
        return menu
    }

    /** Mirrors the production signed-out primary item visibility. */
    fun applySignedOutPrimaryVisibility(navigation: NavigationBarView) {
        navigation.menu.findItem(R.id.nav_myanime)?.isVisible = false
        navigation.menu.findItem(R.id.nav_mymanga)?.isVisible = false
    }

    /** Mirrors the production signed-in primary item visibility. */
    fun applySignedInPrimaryVisibility(navigation: NavigationBarView) {
        navigation.menu.findItem(R.id.nav_myanime)?.isVisible = true
        navigation.menu.findItem(R.id.nav_mymanga)?.isVisible = true
    }
}