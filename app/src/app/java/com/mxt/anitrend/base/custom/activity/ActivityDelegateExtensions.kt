package com.mxt.anitrend.base.custom.activity

import android.content.Context
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

private fun Context.unsupportedFeature() {
    Timber.i("$packageName does not support checking updates, migrate to play services")
}

fun FragmentActivity.launchUpdateWorker() {
    unsupportedFeature()
}
