package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

public class DownloadWorker extends Worker {

    private Context theContext;

    public DownloadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        theContext = context;
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.
        boolean isWorking = true;
        Log.d("WORK", "DownloadWorker.doWork() start");

        if (!BBCWorldServiceDownloaderUtils.isDeviceConnected(theContext) || !BBCWorldServiceDownloaderUtils.isDeviceOnWlan(theContext)) {
            Log.d("WORK", "DownloadWorker.doWork() device not on wlan");
            return Result.retry();
        }
        //Check network state for wlan connection
        BBCWorldServiceDownloaderUtils utils = new BBCWorldServiceDownloaderUtils();
        //connected: lets first get all available downloads from bbc
        Bundle downLoadOptionsBundle = null;
        try {
            downLoadOptionsBundle = utils.getCurrentDownloadOptions();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
        ItemList itemList = new ItemList(downLoadOptionsBundle);
        Log.d("WORK", "DownloadWorker.doWork() got items: " + itemList.ITEMS.size());
        if (itemList.ITEMS.size() < 2)
            return Result.retry();
        for (int i = 1; i < itemList.ITEMS.size(); i++) {
            DownloadListItem currentItem = itemList.ITEMS.get(i);
            Intent theDownloadIntent = utils.prepareItemDownload(currentItem, theContext, false, true);
            Log.d("WORK", "DownloadWorker.doWork() start download");
            theContext.startService(theDownloadIntent);
            //get out of download loop if network state has changed
            //if (!BBCWorldServiceDownloaderUtils.isDeviceConnected(theContext) || !BBCWorldServiceDownloaderUtils.isDeviceOnWlan(theContext))
                return Result.retry();
        }


        // Indicate whether the work finished successfully with the Result
        return Result.success();

    }
}


