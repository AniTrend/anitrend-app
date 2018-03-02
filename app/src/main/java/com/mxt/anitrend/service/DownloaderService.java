package com.mxt.anitrend.service;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mxt.anitrend.R;
import com.mxt.anitrend.model.api.retro.base.RepositoryModel;
import com.mxt.anitrend.model.entity.base.Version;

public class DownloaderService {

    /**
     * Handles downloading of new version of AniTrend
     */
    public static void downloadNewVersion(Context context, Version version) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(RepositoryModel.DOWNLOAD_LINK));
        request.setTitle(String.format("AniTrend v%s.apk", version.getVersion()));
        request.allowScanningByMediaScanner();
        String ext = MimeTypeMap.getFileExtensionFromUrl(RepositoryModel.DOWNLOAD_LINK);
        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        request.setDescription(context.getString(R.string.text_downloading_update));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, String.format("AniTrend v%s.apk", version.getVersion()));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if(downloadManager != null)
            downloadManager.enqueue(request);
    }
}
