package org.mfri.bbcworldservicenewshourdownloader;

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

import org.mfri.bbcworldservicenewshourdownloader.VolleyRequest;

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
            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            this.bundle = intent.getExtras();
            final String fileName = bundle.getString("fileName");
            BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
            File theFile = utils.fileExists(fileName, getApplicationContext());
            if (theFile != null) {
                if (bundle.getBoolean("isToastOnFileExists")) {
                    sendBroadcast(true, theFile.getName(), fileName, bundle.getBoolean("isStartedInBackground"));
                }
                return;
            }

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
            lbm.registerReceiver(bReceiver, new IntentFilter("DOWNLOAD_REDIRECT"));

            Log.d("DownloadService", "onHandleIntent start LocalBroadcastManager started");

            executeVolleyRequest(fileName, bundle.getString("url"));

        }

    }

    private void executeVolleyRequest(String fileName, String url) {

        Log.d("HANDLE_INTENT", "DownloadService: executeVolleyRequest url: "+ url);
        RequestQueue queue = Volley.newRequestQueue(this);
        VolleyRequest bytesRequest = new VolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {

                        try {
                            if (response != null) {
                                try {
                                    String fileNameSaved = savePodcast(fileName, response);
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                error.printStackTrace();
            }
        }, null, getApplicationContext());

        queue.add(bytesRequest);
    }


    private String savePodcast(String fileName, byte[] barry) throws IOException {

        String root = PreferenceManager.getDefaultSharedPreferences(this).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());

        File myDir = new File(root);
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
                    executeVolleyRequest(bundle.getString("fileName"), intent.getExtras().getString("redirectUrl"));
                    break;
                default:
                    break;
            }


        }

    };

}
