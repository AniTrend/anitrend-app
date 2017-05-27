package com.mxt.anitrend.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mxt.anitrend.R;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by max on 2017/04/17.
 */
public class Downloader {

    /**
     * Handles downloading of new version of AniTrend
     * </br>
     * @param mContext valid application context
     * @param url url of the actual file not a redirect urls
     * @param name file name to set when download is completed
     */
    public static long startDownload(Context mContext, String url, String name) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(name);
        request.allowScanningByMediaScanner();
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        request.setDescription(mContext.getString(R.string.text_downloading_update));
        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager manager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }
}
