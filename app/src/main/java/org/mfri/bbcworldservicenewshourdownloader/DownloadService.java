package org.mfri.bbcworldservicenewshourdownloader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;



/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {

    private BBCWorldServiceDownloaderUtils utils = null;
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
            List<Hashtable<String, String>> resultList = null;
            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            final Bundle bundle = intent.getExtras();
            final String fileName = bundle.getString("fileName");
            utils = BBCWorldServiceDownloaderUtils.getInstance();
            File theFile = utils.fileExists(fileName);
            if (theFile != null) {
                if (bundle.getBoolean("isToastOnFileExists")) {
//                    Toast.makeText(getApplicationContext(), "File exists: " + theFile.getName(), Toast.LENGTH_LONG).show();
                    sendBroadcast(true, theFile.getName(), fileName, bundle.getBoolean("isStartedInBackground"));
                }
                return;
            }
            RequestQueue queue = Volley.newRequestQueue(this);
            Log.d("HANDLE_INTENT", "DownloadService: onHandleIntent url: "+bundle.getString("url"));
            InputStreamVolleyRequest bytesRequest = new InputStreamVolleyRequest(Request.Method.GET, bundle.getString("url"),
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
            }, null);
            queue.add(bytesRequest);
        }

    }



    private String savePodcast(String fileName, byte[] barry) throws IOException {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/"+BBCWorldServiceDownloaderStaticValues.BBC_PODCAST_DIR);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }


        File file = new File(myDir, fileName);
        if (file.exists())
            file.delete();

        FileOutputStream out = new FileOutputStream(file);
        out.write(barry);
        out.close();
        return file.getPath() + "/" + file.getName();

    }

    private void sendBroadcast(boolean success, String fileName, String fileNameWithoutDir, boolean isStartedInBackground) {
        Intent intent = new Intent("messageFromDownloadService"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", success);
        intent.putExtra("fileName", fileName);
        intent.putExtra("fileNameWithoutDir", fileNameWithoutDir);
        intent.putExtra("isStartedInBackground", isStartedInBackground);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
