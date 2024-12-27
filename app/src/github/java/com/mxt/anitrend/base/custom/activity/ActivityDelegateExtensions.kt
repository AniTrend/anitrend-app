package com.mxt.anitrend.base.custom.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afollestad.materialdialogs.DialogAction
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.service.DownloaderService
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.sheet.BottomSheetMessage

private fun FragmentActivity.onLatestUpdateInstalled() {
    NotifyUtil.createAlerter(
        this,
        getString(R.string.title_update_infodadat),
        getString(R.string.app_no_date),
        R.drawable.ic_cloud_done_white_24dp,
        R.color.colorStateGreen
    )
}

private fun MainActivity.onUpdateChecked(silent: Boolean, menuItems: Menu) {
    val remoteVersion = presenter.database.remoteVersion

    if (remoteVersion != null) {
        if (remoteVersion.isNewerVersion) {
            // If a new version of the application is available on GitHub
            val mAppUpdateWidget = menuItems.findItem(R.id.nav_check_update)
                .actionView?.findViewById<TextView>(R.id.app_update_info)
            mAppUpdateWidget?.text = getString(R.string.app_update, remoteVersion.version)
            mAppUpdateWidget?.visibility = View.VISIBLE
        } else if (!silent) {
            onLatestUpdateInstalled()
        }
    }
}

fun MainActivity.launchUpdateWorker(menuItems: Menu) {
    WorkManager.getInstance(this)
        .getWorkInfosByTagLiveData(
            KeyUtil.WorkUpdaterId
        ).observe(this) { workInfoList ->
            workInfoList.firstOrNull { workInfo ->
                workInfo.state == WorkInfo.State.SUCCEEDED
            }?.run {
                onUpdateChecked(
                    outputData.getBoolean(
                        KeyUtil.WorkUpdaterSilentId,
                        false
                    ),
                    menuItems
                )
            }
        }
}

fun MainActivity.checkUpdate() {
    mBottomSheet = BottomSheetMessage.Builder()
        .setText(R.string.drawer_update_text)
        .setTitle(R.string.drawer_update_title)
        .setPositiveText(R.string.Yes)
        .setNegativeText(R.string.No)
        .buildWithCallback(object : BottomSheetChoice {
            override fun onPositiveButton() {
                val versionBase = presenter.database.remoteVersion
                if (versionBase != null && versionBase.isNewerVersion)
                    DownloaderService.downloadNewVersion(
                        this@checkUpdate,
                        versionBase
                    )
                else presenter.checkForUpdates(false)
            }

            override fun onNegativeButton() {

            }
        })
    showBottomSheet()
}