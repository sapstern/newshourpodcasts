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

    private final Context theContext;


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

        return getResult(utils, itemList);

    }

    private Result getResult(BBCWorldServiceDownloaderUtils utils, ItemList itemList) {
        Log.d("WORK", "DownloadWorker.doWork() start download size of list is "+itemList.ITEMS.size());
        if (itemList.ITEMS.size() < 1)
            return Result.retry();
        DownloadListItem currentItem = itemList.ITEMS.get(1);
        Intent theDownloadIntent = utils.prepareItemDownload(currentItem, theContext, false, true);

        theContext.startService(theDownloadIntent);
        Log.d("WORK", "DownloadWorker.doWork() start download intent started ");
        try {
                //System.out.println(new Date());
                Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            Log.d("WORK", "DownloadWorker.doWork() start download interrupted ");
            Thread.currentThread().interrupt();
        }
        Log.d("WORK", "DownloadWorker.doWork() start download after pause ");
        //get out of download loop if network state has changed
        if (!BBCWorldServiceDownloaderUtils.isDeviceConnected(theContext) || !BBCWorldServiceDownloaderUtils.isDeviceOnWlan(theContext))
            return Result.retry();
        else {
            itemList.ITEMS.remove(1);
            return getResult(utils, itemList);
        }

    }


}


