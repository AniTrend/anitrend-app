package com.mxt.anitrend.base.custom.activity

import android.content.Context
import com.mxt.anitrend.view.activity.index.MainActivity
import timber.log.Timber

private fun Context.unsupportedFeature() {
    Timber.i("$packageName does not support checking updates, migrate to play services")
}

fun MainActivity.launchUpdateWorker() {
    unsupportedFeature()
}

fun MainActivity.checkUpdate() {
    unsupportedFeature()
}
