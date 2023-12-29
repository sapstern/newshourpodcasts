package org.mfri.bbc.mediamanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

public class RadioLive extends MediaPlayer{

    private static final RadioLive INSTANCE = new RadioLive();


    private int oTime = 0, sTime = 0, eTime = 0, fTime = 5000, bTime = 5000;

    public boolean isInitial;

    private RadioLive() {
        super();
        isInitial = true;
    }

    public static RadioLive getInstance()
    {
        return INSTANCE;
    }



    public void initMplayer(Context context, String url) {
        Log.d("RadioLive", "initMplayer start");

        this.setOnPreparedListener(mPlayer -> this.start());
        this.setOnErrorListener((mp, what, extra) -> {
            Log.d("RadioLive.mPlayer", "onError start " + mp.isPlaying());
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }

        try {
            this.setDataSource(url);
            this.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_PLAY"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_START"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_RESUME"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_STOP"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_PAUSE"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_MOVE_FORWARD"));
        lbm.registerReceiver(bReceiverRadioLive, new IntentFilter("ON_MOVE_BACKWARD"));
        isInitial = false;
    }



    /**
     * Gets called from Radiolive class
     */
    private BroadcastReceiver bReceiverRadioLive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver_RL", "onReceive() start processing Media Player actions");

            switch (intent.getAction()) {
                case "ON_PLAY":
                    if(!RadioLive.this.isPlaying()) {
                        RadioLive.this.prepareAsync();
                        RadioLive.this.start();
                    }
                    Intent playIntent = new Intent("UPDATE_PLAY_TIME");
                    playIntent.putExtra("eTime", RadioLive.this.getDuration());
                    playIntent.putExtra("sTime", RadioLive.this.getCurrentPosition());
                    if(oTime == 0){
                        oTime = 1;
                    }
                    playIntent.putExtra("oTime", oTime);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(playIntent);
                    break;
                case "ON_START":
                case "ON_RESUME":
                    RadioLive.this.prepareAsync();
                    RadioLive.this.start();
                    break;
                case "ON_STOP":
                case "ON_PAUSE":
                    //stopThread();
                    RadioLive.this.stop();
                    //RadioLive.this.release();
                    break;
                case "ON_MOVE_FORWARD":
                    //onMoveForwardButton();
                    break;
                case "ON_MOVE_BACKWARD":
                    //onMoveBackwardButton();
                    break;
                default:
                    break;
            }

        }

    };

    public void killMplayer() {

        this.stop();
        this.release();
    }


}