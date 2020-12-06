package org.mfri.bbcworldservicenewshourdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ItemMainActivity extends Activity {

    Bundle theBundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_main);

        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        boolean isPermissionGiven = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission. READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                isPermissionGiven =  true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
                isPermissionGiven =  false;
            }
        }

        if (isPermissionGiven){
            startBackgroundWorkerAndService();
        }
    }

    private void startBackgroundWorkerAndService() {
        PeriodicWorkRequest downLoadRequest =
                new PeriodicWorkRequest.Builder(DownloadWorker.class, 1, TimeUnit.HOURS)
                        // Constraints
                        .build();
        WorkManager
                .getInstance(this)
                .enqueue(downLoadRequest);

        Intent intent = new Intent(this, ListService.class);
        this.startService(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                boolean isPerpermissionForAllGranted = false;
                if (grantResults.length > 0 && permissions.length == grantResults.length) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isPerpermissionForAllGranted = true;
                        } else {
                            isPerpermissionForAllGranted = false;
                        }
                    }

                    //Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    isPerpermissionForAllGranted = true;
                    //Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                if (isPerpermissionForAllGranted) {
                    startBackgroundWorkerAndService();

                }
                break;
        }
    }
}
