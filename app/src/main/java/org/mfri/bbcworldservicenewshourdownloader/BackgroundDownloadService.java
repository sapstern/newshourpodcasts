package org.mfri.bbcworldservicenewshourdownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 */
public class BackgroundDownloadService extends Service {

    private BackgroundDowmloadRunnable theRunnable = null;
    /**
     * @deprecated
     */
    public BackgroundDownloadService() {
        super();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        theRunnable = new BackgroundDowmloadRunnable(getApplicationContext());
        theRunnable.run();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //theRunnable.notify(); MFRI: Pruefen, ob es funktioniert
    }


    private boolean fileExists(String fileName) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Podcasts");
        if (!myDir.exists()) {
            return false;
        }
        File theFile = new File(root + "/Podcasts/" + fileName);
        return theFile.exists();
    }


}
