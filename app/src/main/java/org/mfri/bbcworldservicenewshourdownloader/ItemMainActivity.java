package org.mfri.bbcworldservicenewshourdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

public class ItemMainActivity extends Activity implements BBCWorldServiceDownloaderStaticValues{

    BBCWorldServiceDownloaderUtils utils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MAIN_CREATE", "onCreate start");
        setContentView(R.layout.activity_item_main);
       
        utils = BBCWorldServiceDownloaderUtils.getInstance();
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Permission stuff
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (       checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission. READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
            ) {
                startBackgroundWorkerAndService();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE }, 0);
            }
        }
        Log.d("MAIN_CREATE", "onCreate end");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 0:
                startBackgroundWorkerAndService();
                break;
            default:
                System.runFinalization();
                System.exit(0);
                break;
        }
    }
    private void startBackgroundWorkerAndService() {
        Log.d("MAIN", "startBackgroundWorkerAndService start");
        //Start delete service if selected
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("keep_forever", true)!=true){

            for (String currentProgram : URL_MAP.keySet()) {
                Intent intent = new Intent(this, DeleteOldPodcastsService.class);
                intent.putExtra("theProgram", currentProgram);
                this.startService(intent);
            }

        }

            //Schedule background download processing (if user wants it)
        utils.processChoosenDownloadOptions(this);


        //Proceed to next activity (display list of download options)
       utils.startListService(this, PROGRAM_NEWSHOUR, "org.mfri.bbcworldservicenewshourdownloader.ItemListActivityNewshour");
       Log.d("MAIN", "startBackgroundWorkerAndService end");
    }


}
