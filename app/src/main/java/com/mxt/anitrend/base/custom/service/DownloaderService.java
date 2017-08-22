package com.mxt.anitrend.base.custom.service;

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
public class DownloaderService {

    private DownloadManager manager;
    private Context context;
    private MimeTypeMap mimeType;

    public DownloaderService(Context context) {
        this.context = context;
        mimeType = MimeTypeMap.getSingleton();
        manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * Handles downloading of new version of AniTrend
     * </br>
     *
     * @param url url of the actual file not a redirect urls
     * @param name file name to set when download is completed
     */
    public long startUpdateDownload(String url, String name) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(name);
        request.allowScanningByMediaScanner();
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        request.setMimeType(mimeType.getMimeTypeFromExtension(ext));
        request.setDescription(context.getString(R.string.text_downloading_update));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        return manager.enqueue(request);
    }

    /**
     * Cancel downloads and remove them from the download manager.
     * Each download will be stopped if it was running, and it will no longer be accessible through the download manager.
     * If there is a downloaded file, partial or complete, it is deleted.
     * <br/>
     *
     * @param id of the enqueued item
     * @return number of canceled downloads is more than 0
     */
    public boolean cancelDownload(long id) {
        return manager.remove(id) > 0;
    }
}
