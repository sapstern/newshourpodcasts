package org.mfri.bbcworldservicepodcastdownloader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DownloadWorker extends Worker {

    private final Context theContext;
    //private int programFlag;

    public DownloadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
            ) {
        super(context, params);
        theContext = context;

    }

    @NonNull
    @Override
    public Result doWork() {

        // Do the work here
        Log.d("WORK", "DownloadWorker.doWork() start");

        BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
        // lets first get all available downloads from bbc
        Bundle downLoadOptionsBundle = utils.getCurrentDownloadOptions(theContext, getInputData().getString("PROGRAM_TYPE"));

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
        if(PreferenceManager.getDefaultSharedPreferences(theContext).getBoolean("dl_background", true)!=true) {
            return Result.failure();
        }
            if (itemList.ITEMS.size() < 1)
            return Result.failure();
        DownloadListItem currentItem = null;
        for(int i = 1;i<itemList.ITEMS.size();i++) {
            //find first item which has not been downloaded yet
            //start at pos 1, as the item at 0 holds only the description
            currentItem = itemList.ITEMS.get(i);
            if( utils.fileExists(currentItem.fileName, getApplicationContext(), getInputData().getString("PROGRAM_TYPE")) == null )
                break;
        }

        assert currentItem != null;
        Intent theDownloadIntent = utils.prepareItemDownload(currentItem, theContext, false, true, getInputData().getString("PROGRAM_TYPE"));

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


