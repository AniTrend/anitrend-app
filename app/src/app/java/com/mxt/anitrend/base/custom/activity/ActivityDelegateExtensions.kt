package com.mxt.anitrend.base.custom.activity

import android.content.Context
import android.view.Menu
import androidx.fragment.app.FragmentActivity
import timber.log.Timber
import com.mxt.anitrend.view.activity.index.MainActivity

private fun Context.unsupportedFeature() {
    Timber.i("$packageName does not support checking updates, migrate to play services")
}

private fun FragmentActivity.onLatestUpdateInstalled() {
    unsupportedFeature()
}

private fun FragmentActivity.onUpdateChecked(silent: Boolean, menuItems: Menu) {
    unsupportedFeature()
}

fun FragmentActivity.launchUpdateWorker(menuItems: Menu) {
    unsupportedFeature()
}

fun MainActivity.checkUpdate() {
    unsupportedFeature()
}