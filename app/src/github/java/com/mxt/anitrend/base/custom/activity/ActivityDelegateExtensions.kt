package com.mxt.anitrend.base.custom.activity

import androidx.fragment.app.FragmentActivity
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.service.DownloaderService
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.sheet.BottomSheetMessage

private fun FragmentActivity.onLatestUpdateInstalled() {
    NotifyUtil.createAlerter(
        this,
        getString(R.string.title_update_info),
        getString(R.string.app_no_date),
        R.drawable.ic_cloud_done_white_24dp,
        R.color.colorStateGreen,
    )
}

private fun MainActivity.onUpdateChecked(silent: Boolean) {
    val remoteVersion = koinOf<BoxQuery>().remoteVersion

    if (remoteVersion != null) {
        if (remoteVersion.isNewerVersion()) {
            // If a new version of the application is available on GitHub
            updateDrawerUpdateBadge(getString(R.string.app_update, remoteVersion.version))
        } else if (!silent) {
            onLatestUpdateInstalled()
        }
    }
}

fun MainActivity.launchUpdateWorker() {
    WorkManager
        .getInstance(this)
        .getWorkInfosByTagLiveData(
            KeyUtil.WorkUpdaterId,
        ).observe(this) { workInfoList ->
            workInfoList
                .firstOrNull { workInfo ->
                    workInfo.state == WorkInfo.State.SUCCEEDED
                }?.run {
                    onUpdateChecked(
                        outputData.getBoolean(
                            KeyUtil.WorkUpdaterSilentId,
                            false,
                        ),
                    )
                }
        }
}

fun MainActivity.checkUpdate() {
    mBottomSheet =
        BottomSheetMessage
            .Builder()
            .setText(R.string.drawer_update_text)
            .setTitle(R.string.drawer_update_title)
            .setPositiveText(R.string.Yes)
            .setNegativeText(R.string.No)
            .buildWithCallback(
                object : BottomSheetChoice {
                    override fun onPositiveButton() {
                        val versionBase = koinOf<BoxQuery>().remoteVersion
                        if (versionBase != null && versionBase.isNewerVersion()) {
                            DownloaderService.downloadNewVersion(
                                this@checkUpdate,
                                versionBase,
                            )
                        } else {
                            JobSchedulerUtil(KoinExt.get(Settings::class.java))
                                .startUpdateJob(applicationContext, false)
                        }
                    }

                    override fun onNegativeButton() {
                    }
                },
            )
    showBottomSheet()
}
