package org.mfri.bbc.mediamanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;




public class ItemListActivityRadiolive extends AbstractItemListActivity{


    private ImageButton pausebtn;
    private ImageButton playbtn;
    //private MediaPlayer mPlayer;
    private TextView startTime, podcastTime;
    private SeekBar podcastPrgs;


    @Override
    protected void onStop (){
        super.onStop();
        sendBroadcast("ON_STOP");

    }

    @Override
    protected void onResume (){
        super.onResume();
        sendBroadcast("ON_RESUME");
    }

    @Override
    protected void onStart (){
        super.onStart();
        sendBroadcast("ON_START");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_live);
        Log.d("RADIOLIVE_CREATE", "onCreate start");
        super.setupRadioLiveLayout(PROGRAM_RADIOLIVE);


        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


//        ImageButton backwardbtn = findViewById(R.id.btnBackward);
//        ImageButton forwardbtn = findViewById(R.id.btnForward);
        playbtn = findViewById(R.id.btnPlay);
        pausebtn = findViewById(R.id.btnPause);

        startTime = findViewById(R.id.txtStartTime);
        podcastTime = findViewById(R.id.txtPodTime);


        podcastPrgs = findViewById(R.id.sBar);
        podcastPrgs.setClickable(false);
        pausebtn.setEnabled(true);
        playbtn.setEnabled(true);

        playbtn.setOnClickListener(v -> {
            Toast.makeText(ItemListActivityRadiolive.this, "Playing Audio", Toast.LENGTH_SHORT).show();


            sendBroadcast("ON_PLAY");
        });
        pausebtn.setOnClickListener(v -> {

            Toast.makeText(getApplicationContext(),"Pausing Audio", Toast.LENGTH_SHORT).show();
            sendBroadcast("ON_PAUSE");
        });
//        forwardbtn.setOnClickListener(v -> {
//            if(!playbtn.isEnabled()){
//                playbtn.setEnabled(true);
//            }
//            sendBroadcast("ON_MOVE_FORWARD");
//        });
//        backwardbtn.setOnClickListener(v -> {
//            if(!playbtn.isEnabled()){
//                playbtn.setEnabled(true);
//            }
//            sendBroadcast("ON_MOVE_BACKWARD");
//        });


        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(bReceiverItemListRadioLive, new IntentFilter("UPDATE_RADIO_TIME"));
        lbm.registerReceiver(bReceiverItemListRadioLive, new IntentFilter("UPDATE_PLAY_TIME"));


        Intent radioLiveIntent = new Intent(this, RadioLiveService.class);
        getApplicationContext().startService(radioLiveIntent);


        Log.d("RADIOLIVE_CREATE", "onCreate end");
    }


    /**
     * Gets called from ItemListActivityRadiolive class
     */
    private final BroadcastReceiver bReceiverItemListRadioLive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BR_ActivityILRadioLive", "onReceive() start Updating view progress indicator");

            switch (intent.getAction()){
                case "UPDATE_RADIO_TIME":
                    startTime.setText(String.format(Locale.ENGLISH, "%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(intent.getIntExtra("sTime", 0)), TimeUnit.MILLISECONDS.toSeconds(intent.getIntExtra("sTime", 0)) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(intent.getIntExtra("sTime", 0)))) );
                    podcastPrgs.setProgress(intent.getIntExtra("sTime", 0));
                    break;
                case "UPDATE_PLAY_TIME":
                    if(intent.getIntExtra("oTime", 0) == 0){
                        podcastPrgs.setMax(intent.getIntExtra("eTime", 0));
                    }
                    podcastTime.setText(String.format(Locale.ENGLISH, "%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(intent.getIntExtra("eTime", 0)), TimeUnit.MILLISECONDS.toSeconds(intent.getIntExtra("eTime", 0)) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(intent.getIntExtra("eTime", 0)))) );
                    startTime.setText(String.format(Locale.ENGLISH, "%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(intent.getIntExtra("sTime", 0)), TimeUnit.MILLISECONDS.toSeconds(intent.getIntExtra("sTime", 0)) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(intent.getIntExtra("sTime", 0)))) );
                    podcastPrgs.setProgress(intent.getIntExtra("sTime", 0));
                    break;

                default:
                    break;
            }


        }

    };
    private void sendBroadcast(String intendFilter) {

        Intent intent = new Intent(intendFilter);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
