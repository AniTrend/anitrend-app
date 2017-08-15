package com.mxt.anitrend.api.core;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Maxwell on 10/2/2016.
 * Previously used to store token to app data space storage
 * TODO: Upgrade caching mechanism to account manager
 */
@Deprecated
public final class Cache {

    private File mFile;

    public Token getCachedToken() throws IOException, ClassNotFoundException {
        if(mFile.exists()) {
            FileInputStream fis = new FileInputStream(mFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (Token)ois.readObject();
        }
        return null;
    }

    public boolean saveTokenToCache(Token mToken) throws IOException {
        if(mToken != null) {
            if (mFile.exists()) {
                return fileCreator(mToken);
            } else {
                boolean result = mFile.createNewFile();
                return result && fileCreator(mToken);
            }
        }
        return false;
    }

    public boolean invalidateCachedToken() {
        return mFile.exists() && mFile.delete();
    }

    private boolean fileCreator(Token ref) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ref);
        oos.flush();
        oos.close();
        if(mFile.canWrite()) {
            FileOutputStream fos = new FileOutputStream(mFile,false);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            return true;
        }
        return false;
    }

    public Cache(Context appContext) {
        String preferred_name = "app_cache.atr";
        mFile = new File(appContext.getFilesDir(), preferred_name);
    }

    public static okhttp3.Cache getCache(Context context) {
        return new okhttp3.Cache(context.getCacheDir(), 50 * 1024 * 1024);
    }
}