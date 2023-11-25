package org.mfri.bbc.mediamanager;

import static org.mfri.bbc.mediamanager.BBCWorldServiceDownloaderStaticValues.PROGRAM_RADIOLIVE;
import static org.mfri.bbc.mediamanager.BBCWorldServiceDownloaderStaticValues.URL_MAP;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;


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

        BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();

        utils.startRadioLive(this);


        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService end");

    }



}
