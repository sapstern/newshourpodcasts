package org.mfri.bbc.mediamanager;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class RadioLiveService extends IntentService {




    /**
     * @deprecated
     */
    @Deprecated
    public RadioLiveService() {

        super("RadioLiveService");


    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService start");
        synchronized (intent) {
            BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();

            utils.startRadioLive(this);
        }


        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService end");

    }



}
