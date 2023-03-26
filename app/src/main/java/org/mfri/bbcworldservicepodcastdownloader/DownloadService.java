package org.mfri.bbcworldservicepodcastdownloader;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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
    @Deprecated
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

            this.bundle = intent.getExtras();
            final String fileName = bundle.getString("fileName");
            final boolean isStartedInBackground = bundle.getBoolean("isStartedInBackground", true);
            //Background download check wLan connection
            if (!utils.isWlanConnection(this) && isStartedInBackground){
                return;
            }
            //First check filesystem on existence of file, if so, we do not need to download
            File theFile = utils.fileExists(fileName, getApplicationContext(), bundle.getString("theProgram"));
            if (theFile != null) {
                if (bundle.getBoolean("isToastOnFileExists")) {
                    sendBroadcast(true, theFile.getName(), fileName, isStartedInBackground);
                }
                return;
            }
            //File does not exist, we have to download it
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
            lbm.registerReceiver(bReceiver, new IntentFilter("DOWNLOAD_REDIRECT"));

            Log.d("DownloadService", "onHandleIntent start LocalBroadcastManager started");

            executeVolleyRequest(fileName, bundle.getString("url"), bundle.getString("theProgram"));

        }

    }

    /**
     * Starts the actual volley request
     * @param fileName
     * @param url
     * @param theProgram
     */
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
                        Toast.makeText(getApplicationContext(), "Error saving file " + fileName + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }, error -> {
                    // TODO handle the error
                    Toast.makeText(getApplicationContext(), "Error saving file " + fileName + ": " + error.getMessage(), Toast.LENGTH_LONG).show();
                }, null, getApplicationContext(), bundle.getBoolean("isStartedInBackground"));

        queue.add(bytesRequest);
    }

    /**
     * Saves the podcast in filesystem
     * @param fileName
     * @param barry
     * @param theProgram name of bbc pdcast program
     * @return filename
     * @throws IOException
     */
    private String savePodcast(String fileName, byte[] barry, String theProgram) throws IOException {

        String root = PreferenceManager.getDefaultSharedPreferences(this).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());

        File myDir = new File(root+"/"+theProgram);
        BBCWorldServiceDownloaderUtils.checkDir(myDir, this);


        File file = new File(myDir, fileName);
        if (file.exists())
            file.delete();

        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        ByteArrayInputStream input = new ByteArrayInputStream(barry);
        byte data[] = new byte[512];

        long total = 0;
        int count;

        while ((count = input.read(data)) != -1) {
            total += count;
            int progress = (int) total * 100 / barry.length;
            output.write(data, 0, count);
            if(!bundle.getBoolean("isStartedInBackground")&&progress > 0) {

                Intent intent = new Intent("DOWNLOAD_DISPLAY_PROGRESS");
                intent.putExtra("progress", progress);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }

        output.flush();

        output.close();
        input.close();
        return file.getName();

    }


    /**
     * Starts implicid intend to play the podcast as it has been downloaded already
     * @param success
     * @param fileName
     * @param fileNameWithoutDir
     * @param isStartedInBackground play podcast only if not downloaded in background
     */
   private void sendBroadcast(boolean success, String fileName, String fileNameWithoutDir, boolean isStartedInBackground) {
        Intent intent = new Intent("IMPLICIT_INTENT_START_PODCAST"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", success);
        intent.putExtra("fileName", fileName);
        intent.putExtra("fileNameWithoutDir", fileNameWithoutDir);
        intent.putExtra("isStartedInBackground", isStartedInBackground);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Gets called from VolleyRequest class if download error happened and user allowed for redirects
     */
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "onReceive() start => try redirected download");
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
