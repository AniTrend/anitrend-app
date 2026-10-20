package com.mxt.anitrend.base.custom.activity

import androidx.fragment.app.FragmentActivity
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil

private fun FragmentActivity.onLatestUpdateInstalled() {
    NotifyUtil.createAlerter(
        this,
        getString(R.string.title_update_info),
        getString(R.string.app_no_date),
        R.drawable.ic_cloud_done_white_24dp,
        R.color.colorStateGreen,
    )
}

private fun FragmentActivity.onUpdateChecked(silent: Boolean) {
    val remoteVersion = koinOf<BoxQuery>().remoteVersion

    if (remoteVersion != null && remoteVersion.isNewerVersion()) {
        // A new version of the application is available on GitHub: surfaced
        // as a nonblocking alerter now that the drawer carries no update row.
        NotifyUtil.createAlerter(
            this,
            getString(R.string.title_update_info),
            getString(R.string.app_update, remoteVersion.version),
            R.drawable.ic_cloud_done_white_24dp,
            R.color.colorStateGreen,
        )
    } else if (!silent) {
        onLatestUpdateInstalled()
    }
}

fun FragmentActivity.launchUpdateWorker() {
    WorkManager
        .getInstance(this)
        .getWorkInfosByTagLiveData(KeyUtil.WorkUpdaterId)
        .observe(this) { workInfoList ->
            workInfoList
                .firstOrNull { workInfo -> workInfo.state == WorkInfo.State.SUCCEEDED }
                ?.run {
                    onUpdateChecked(
                        outputData.getBoolean(
                            KeyUtil.WorkUpdaterSilentId,
                            false,
                        ),
                    )
                }
        }
}
