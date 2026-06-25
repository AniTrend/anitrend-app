package com.mxt.anitrend.base.custom.activity

import android.content.Context
import android.view.Menu
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import timber.log.Timber

private fun Context.unsupportedFeature() {
    Timber.i("$packageName does not support checking updates, migrate to play services")
}

private fun FragmentActivity.onLatestUpdateInstalled() {
    unsupportedFeature()
}

private fun FragmentActivity.onUpdateChecked(
    silent: Boolean,
    menuItems: Menu,
) {
    unsupportedFeature()
}

fun FragmentActivity.launchUpdateWorker(menuItems: Menu) {
    unsupportedFeature()
}

fun MainActivity.checkUpdate() {
    unsupportedFeature()
}
