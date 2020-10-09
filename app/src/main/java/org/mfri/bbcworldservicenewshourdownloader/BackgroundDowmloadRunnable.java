package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundDowmloadRunnable implements Runnable {

    private Context theContext = null;
    private AtomicBoolean isWorking = new AtomicBoolean(true);

    public BackgroundDowmloadRunnable(Context theContext) {
        this.theContext = theContext;
    }

    @Override
    public void run() {
        Log.d("RUN", "BackgroundDowmloadRunnable.run() start");
        Utils utils = new Utils();
        while (isWorking.get()) {
            try {
                //Check network state for wlan connection
                if (Utils.isDeviceConnected(theContext) && Utils.isDeviceOnWlan(theContext)) {
                    //connected: lets first get all available downloads from bbc
                    Bundle downLoadOptionsBundle = null;
                    try {
                        downLoadOptionsBundle = utils.getCurrentDownloadOptions();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    ItemList itemList = new ItemList(downLoadOptionsBundle);
                    Log.d("RUN", "BackgroundDowmloadRunnable.run() got items: " + itemList.ITEMS.size());
                    if(itemList.ITEMS.size()<2)
                        continue;
                    for (int i = 1; i <= itemList.ITEMS.size(); i++) {
                        if(i==itemList.ITEMS.size()) {
                            isWorking.set(false);
                            break;
                        }
                        DownloadListItem currentItem = itemList.ITEMS.get(i);
                        Intent theDownloadIntent = utils.prepareItemDownload(currentItem, theContext, false);
                        Log.d("RUN", "BackgroundDowmloadRunnable.run() start download");
                        theContext.startService(theDownloadIntent);
                        //get out of download loop if network state has changed
                        if (!Utils.isDeviceConnected(theContext) || !Utils.isDeviceOnWlan(theContext))
                            break;
                    }

                }
                //Wait 1 Minute
                synchronized (this) {
                    wait(60000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                isWorking.set(false);
            }
        }
    }
}
