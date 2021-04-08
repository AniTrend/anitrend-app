package com.mxt.anitrend.service

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.model.api.retro.base.RepositoryModel
import com.mxt.anitrend.model.entity.base.VersionBase
import java.util.*

object DownloaderService {

    /**
     * Handles downloading of new version of AniTrend
     * @see RepositoryModel.DOWNLOAD_LINK
     */
    fun downloadNewVersion(context: Context?, versionBase: VersionBase) {
        val versionSuffix = when (BuildConfig.FLAVOR) {
            "github" -> "-github"
            else -> ""
        }
        val downloadLink = String.format(RepositoryModel.DOWNLOAD_LINK, versionBase.version, versionSuffix)
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val request = DownloadManager.Request(Uri.parse(downloadLink))
        request.setTitle(String.format(Locale.getDefault(), "anitrend_v%s_%d.apk", versionBase.version, versionBase.code))

        val ext = MimeTypeMap.getFileExtensionFromUrl(RepositoryModel.DOWNLOAD_LINK)
        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext))
        request.setDescription(context?.getString(R.string.text_downloading_update))
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                String.format(Locale.getDefault(), "anitrend_v%s_%d.apk", versionBase.version, versionBase.code))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager?.enqueue(request)
    }
}
