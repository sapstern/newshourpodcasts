package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpURLConnection;
import java.util.HashMap;

public class VolleyRequest extends InputStreamVolleyRequest{

    private Context context = null;

    public VolleyRequest(int method, String mUrl, Response.Listener<byte[]> listener, Response.ErrorListener errorListener, HashMap<String, String> params, Context context) {
        super(method, mUrl, listener, errorListener, params);
        this.context = context;
    }

    @Override
    public void deliverError(final VolleyError error) {
        Log.d("VOLLEY_ERROR", "deliverError");

        final int status = error.networkResponse.statusCode;
        // Handle 30x

        if(HttpURLConnection.HTTP_MOVED_PERM == status || status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_SEE_OTHER) {
            final String location = error.networkResponse.headers.get("Location");
            Log.d("VOLLEY_ERROR", "Location: " + location);
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("allow_redirect", false)==true){
                sendBroadcast(location);
            }
        }
    }
    private void sendBroadcast(String redirectUrl) {
        Intent intent = new Intent("DOWNLOAD_REDIRECT"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("redirectUrl", redirectUrl);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
