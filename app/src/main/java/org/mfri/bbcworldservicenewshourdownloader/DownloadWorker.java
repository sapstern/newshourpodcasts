package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
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

        // Do the work here
        Log.d("WORK", "DownloadWorker.doWork() start");

        BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
        // lets first get all available downloads from bbc
        Bundle downLoadOptionsBundle = null;
        try {
            downLoadOptionsBundle = utils.getCurrentDownloadOptions(theContext);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
        if (downLoadOptionsBundle==null)
            return Result.failure();
        ItemList itemList = new ItemList(downLoadOptionsBundle);
        Log.d("WORK", "DownloadWorker.doWork() got items: " + itemList.ITEMS.size());
        if (itemList.ITEMS.size() < 2)
            return Result.failure();

        return getResult(utils, itemList);

    }

    private Result getResult(BBCWorldServiceDownloaderUtils utils, ItemList itemList) {
        Log.d("WORK", "DownloadWorker.doWork() start download size of list is "+itemList.ITEMS.size());
        if (itemList.ITEMS.size() < 1)
            return Result.failure();
        DownloadListItem currentItem = null;
        for(int i = 0;i<itemList.ITEMS.size();i++) {
            //int currentPos = i+1;
            currentItem = itemList.ITEMS.get(i);
            if( utils.fileExists(currentItem.fileName) == null )
                break;
        }

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

        itemList.ITEMS.remove(1);
        return getResult(utils, itemList);

    }


}


