package org.mfri.bbcworldservicepodcastdownloader;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {


    private Bundle bundle;
    /**
     * @deprecated
     */
    public DownloadService() {
        super("DownloadService");
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HANDLE_INTENT", "DownloadService: onHandleIntent start");

        synchronized (intent) {
            BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();


            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            this.bundle = intent.getExtras();
            final String fileName = bundle.getString("fileName");
            final boolean isStartedInBackground = bundle.getBoolean("isStartedInBackground", true);
            //Background download check wLan connection
            if (!utils.isWlanConnection(this) && isStartedInBackground){
                return;
            }
            File theFile = utils.fileExists(fileName, getApplicationContext(), bundle.getString("theProgram"));
            if (theFile != null) {
                if (bundle.getBoolean("isToastOnFileExists")) {
                    sendBroadcast(true, theFile.getName(), fileName, isStartedInBackground);
                }
                return;
            }

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
            lbm.registerReceiver(bReceiver, new IntentFilter("DOWNLOAD_REDIRECT"));

            Log.d("DownloadService", "onHandleIntent start LocalBroadcastManager started");

            executeVolleyRequest(fileName, bundle.getString("url"), bundle.getString("theProgram"));

        }

    }

    private void executeVolleyRequest(String fileName, String url, String theProgram) {

        Log.d("HANDLE_INTENT", "DownloadService: executeVolleyRequest url: "+ url);
        RequestQueue queue = Volley.newRequestQueue(this);
        VolleyRequest bytesRequest = new VolleyRequest(Request.Method.GET, url,
                response -> {

                    try {
                        if (response != null) {
                            try {
                                String fileNameSaved = savePodcast(fileName, response, theProgram);
                                Toast.makeText(getApplicationContext(), "Saved to: " + fileNameSaved, Toast.LENGTH_LONG).show();
                                sendBroadcast(true, fileNameSaved, fileName, bundle.getBoolean("isStartedInBackground"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error saving file " + fileName + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                        e.printStackTrace();
                    }
                }, error -> {
                    // TODO handle the error
                    error.printStackTrace();
                }, null, getApplicationContext());

        queue.add(bytesRequest);
    }


    private String savePodcast(String fileName, byte[] barry, String theProgram) throws IOException {

        String root = PreferenceManager.getDefaultSharedPreferences(this).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());

        File myDir = new File(root+"/"+theProgram);
        BBCWorldServiceDownloaderUtils.checkDir(myDir, this);


        File file = new File(myDir, fileName);
        if (file.exists())
            file.delete();

        FileOutputStream out = new FileOutputStream(file);
        out.write(barry);
        out.close();
        return file.getName();

    }



    private void sendBroadcast(boolean success, String fileName, String fileNameWithoutDir, boolean isStartedInBackground) {
        Intent intent = new Intent("IMPLICIT_INTENT_START_PODCAST"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", success);
        intent.putExtra("fileName", fileName);
        intent.putExtra("fileNameWithoutDir", fileNameWithoutDir);
        intent.putExtra("isStartedInBackground", isStartedInBackground);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "onReceive start");
            switch (intent.getAction()){
                case "DOWNLOAD_REDIRECT":
                    Log.d("BroadcastReceiver", "onReceive start redirect Volley request to: "+intent.getExtras().getString("redirectUrl"));
                    executeVolleyRequest(bundle.getString("fileName"), intent.getExtras().getString("redirectUrl"), bundle.getString("theProgram"));
                    break;
                default:
                    break;
            }


        }

    };

}
