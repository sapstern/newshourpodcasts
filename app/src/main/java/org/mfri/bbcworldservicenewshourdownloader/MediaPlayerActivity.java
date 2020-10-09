package org.mfri.bbcworldservicenewshourdownloader;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class MediaPlayerActivity extends AppCompatActivity {
    private ImageButton forwardbtn, backwardbtn, pausebtn, playbtn;
    private MediaPlayer mPlayer;
    private TextView podcastName, startTime, podcastTime;
    private SeekBar podcastPrgs;
    private static int oTime =0, sTime =0, eTime =0, fTime = 5000, bTime = 5000;
    private Handler  hdlr = new Handler();

    @Override
    protected void onStop (){
        super.onStop();
        if(mPlayer==null){
            return;
        }
        mPlayer.pause();
    }

    @Override
    protected void onResume (){
        super.onResume();
        if(mPlayer==null){
            return;
        }
        mPlayer.start();
    }

    @Override
    protected void onStart (){
        super.onStart();
        if(mPlayer==null){
            return;
        }
        mPlayer.start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        String fileNameWithoutDir = bundle.getString("fileNameWithoutDir");
        backwardbtn = (ImageButton)findViewById(R.id.btnBackward);
        forwardbtn = (ImageButton)findViewById(R.id.btnForward);
        playbtn = (ImageButton)findViewById(R.id.btnPlay);
        pausebtn = (ImageButton)findViewById(R.id.btnPause);
        podcastName = (TextView)findViewById(R.id.txtPname);
        startTime = (TextView)findViewById(R.id.txtStartTime);
        podcastTime = (TextView)findViewById(R.id.txtPodTime);
        podcastName.setText(fileNameWithoutDir);
        mPlayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ "/podcasts/" + fileNameWithoutDir));
        podcastPrgs = (SeekBar)findViewById(R.id.sBar);
        podcastPrgs.setClickable(false);
        pausebtn.setEnabled(false);

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MediaPlayerActivity.this, "Playing Audio", Toast.LENGTH_SHORT).show();
                mPlayer.start();
                eTime = mPlayer.getDuration();
                sTime = mPlayer.getCurrentPosition();
                if(oTime == 0){
                    podcastPrgs.setMax(eTime);
                    oTime =1;
                }
                podcastTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                        TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(eTime))) );
                startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                        TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(sTime))) );
                podcastPrgs.setProgress(sTime);
                hdlr.postDelayed(UpdatepodcastTime, 100);
                pausebtn.setEnabled(true);
                playbtn.setEnabled(false);
            }
        });
        pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.pause();
                pausebtn.setEnabled(false);
                playbtn.setEnabled(true);
                Toast.makeText(getApplicationContext(),"Pausing Audio", Toast.LENGTH_SHORT).show();
            }
        });
        forwardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime + fTime) <= eTime)
                {
                    sTime = sTime + fTime;
                    mPlayer.seekTo(sTime);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
                }
                if(!playbtn.isEnabled()){
                    playbtn.setEnabled(true);
                }
            }
        });
        backwardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime - bTime) > 0)
                {
                    sTime = sTime - bTime;
                    mPlayer.seekTo(sTime);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
                }
                if(!playbtn.isEnabled()){
                    playbtn.setEnabled(true);
                }
            }
        });
    }
    private Runnable UpdatepodcastTime = new Runnable() {
        @Override
        public void run() {
            sTime = mPlayer.getCurrentPosition();
            startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                    TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))) );
            podcastPrgs.setProgress(sTime);
            hdlr.postDelayed(this, 100);
        }
    };
}