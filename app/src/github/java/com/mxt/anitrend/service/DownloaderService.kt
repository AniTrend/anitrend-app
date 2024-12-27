package com.mxt.anitrend.service

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.model.api.retro.base.RepositoryModel
import com.mxt.anitrend.model.entity.base.VersionBase
import java.io.File

object DownloaderService {

    /**
     * Handles downloading of new version of AniTrend
     * @see RepositoryModel.DOWNLOAD_LINK
     */
    fun downloadNewVersion(context: Context?, versionBase: VersionBase) {
        if (context == null) return
        val versionSuffix = when (BuildConfig.FLAVOR) {
            "github" -> "-github"
            else -> ""
        }
        val downloadLink = Uri.parse(
            String.format(
                RepositoryModel.DOWNLOAD_LINK,
                versionBase.version,
                versionSuffix
            )
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val request = DownloadManager.Request(downloadLink)
        val title = "anitrend_v${versionBase.version}_${versionBase.code}.apk"
        runCatching {
            val destinationFile = File(context.externalCacheDir, title)
            val destinationUri = Uri.fromFile(destinationFile)
            request.setTitle(title)
            val ext = MimeTypeMap.getFileExtensionFromUrl(RepositoryModel.DOWNLOAD_LINK)
            request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext))
            request.setDescription(context.getString(R.string.text_downloading_update))
            request.setDestinationUri(destinationUri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager?.enqueue(request)
        }.onFailure {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, downloadLink)
            )
        }
    }
}
