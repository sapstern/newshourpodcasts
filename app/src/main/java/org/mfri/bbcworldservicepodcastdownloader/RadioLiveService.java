package org.mfri.bbcworldservicepodcastdownloader;

import static org.mfri.bbcworldservicepodcastdownloader.BBCWorldServiceDownloaderStaticValues.PROGRAM_RADIOLIVE;
import static org.mfri.bbcworldservicepodcastdownloader.BBCWorldServiceDownloaderStaticValues.URL_MAP;

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


    private MediaPlayer mPlayer;

    private static int oTime = 0, sTime = 0, eTime = 0, fTime = 5000, bTime = 5000;

    private final Handler hdlr = new Handler();
    private Thread theThread;

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

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(mediaPlayer -> mPlayer.start());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }

        try {
            mPlayer.setDataSource(URL_MAP.get(PROGRAM_RADIOLIVE));
            mPlayer.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //runThread();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_PLAY"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_START"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_RESUME"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_STOP"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_PAUSE"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_MOVE_FORWARD"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_MOVE_BACKWARD"));




        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService end");

    }

    /**
     * Gets called from ItemListActivityRadiolive class
     */
    private final BroadcastReceiver bReceiverRadioLive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver_RL", "onReceive() start processing Media Player");
            if (mPlayer != null) {
                return;
            }
            switch (intent.getAction()) {
                case "ON_PLAY":
                    mPlayer.start();
                    Intent playIntent = new Intent("UPDATE_PLAY_TIME");
                    playIntent.putExtra("eTime", mPlayer.getDuration());
                    playIntent.putExtra("sTime", mPlayer.getCurrentPosition());
                    if(oTime == 0){
                        oTime = 1;
                    }
                    playIntent.putExtra("oTime", oTime);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playIntent);
                    break;
                case "ON_START":
                case "ON_RESUME":
                    mPlayer.start();
                    break;
                case "ON_STOP":
                case "ON_PAUSE":
                    stopThread();
                    mPlayer.stop();
                    mPlayer.release();
                    break;
                case "ON_MOVE_FORWARD":
                    onMoveForwardButton();
                    break;
                case "ON_MOVE_BACKWARD":
                    onMoveBackwardButton();
                    break;
                default:
                    break;
            }


        }

    };

    private void onMoveBackwardButton() {
        if ((sTime - bTime) > 0) {
            sTime = sTime - bTime;
            mPlayer.seekTo(sTime);
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
        }
    }

    private void onMoveForwardButton() {
        if ((sTime + fTime) <= eTime) {
            sTime = sTime + fTime;
            mPlayer.seekTo(sTime);
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
        }

    }



    public void stopThread(){
        theThread = null;
    }
    public void runThread() {
        Thread localThread = Thread.currentThread();
        while (theThread == localThread) {
            try {
                theThread.sleep(1000);
            } catch (InterruptedException e){
            }
            Intent intent = new Intent("UPDATE_RADIO_TIME");
            intent.putExtra("sTime", mPlayer.getCurrentPosition());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            hdlr.postDelayed(theThread, 1000);
        }
    }

}
