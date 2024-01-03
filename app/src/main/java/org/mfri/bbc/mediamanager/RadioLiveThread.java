package org.mfri.bbc.mediamanager;


import android.content.Context;
import android.util.Log;

public class RadioLiveThread extends Thread{

    private Context context;
    private String url;

    public RadioLiveThread(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    public void run(){
        RadioLive  bbcWorldserviceLive = RadioLive.getInstance();
        if(bbcWorldserviceLive.isInitial==false){
            if(!bbcWorldserviceLive.isPlaying()){
                Log.d("RadioLiveThread", "startRadioLive reset and then initialization of Mediaplayer");

                bbcWorldserviceLive.reset();
                bbcWorldserviceLive.initMplayer(context, url);
            }
            return;
        }
        Log.d("RadioLiveThread", "startRadioLive initialization of Mediaplayer");
        bbcWorldserviceLive.initMplayer(context, url);
    }
}
