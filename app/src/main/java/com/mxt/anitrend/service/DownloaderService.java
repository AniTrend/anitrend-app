package com.mxt.anitrend.service;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mxt.anitrend.R;
import com.mxt.anitrend.model.api.retro.base.RepositoryModel;
import com.mxt.anitrend.model.entity.base.VersionBase;
import com.mxt.anitrend.util.ApplicationPref;

import java.util.Locale;

public class DownloaderService {

    /**
     * Handles downloading of new version of AniTrend
     * TODO: 2018/04/08 add update channel option
     * @see RepositoryModel#DOWNLOAD_LINK
     */
    public static void downloadNewVersion(Context context, VersionBase versionBase) {
        ApplicationPref applicationPref = new ApplicationPref(context);
        String downloadLink = String.format(RepositoryModel.DOWNLOAD_LINK, applicationPref.getUpdateChannel());
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadLink));
        request.setTitle(String.format(Locale.getDefault(), "AniTrend V%s RC%d.apk", versionBase.getVersion(), versionBase.getCode()));
        request.allowScanningByMediaScanner();
        String ext = MimeTypeMap.getFileExtensionFromUrl(RepositoryModel.DOWNLOAD_LINK);
        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        request.setDescription(context.getString(R.string.text_downloading_update));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                String.format(Locale.getDefault(), "AniTrend V%s RC%d.apk", versionBase.getVersion(), versionBase.getCode()));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if(downloadManager != null)
            downloadManager.enqueue(request);
    }
}
