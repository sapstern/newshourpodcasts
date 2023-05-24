package org.mfri.bbc.mediamanager;

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

    private Context context;
    private boolean isStartedInBackground;

    public VolleyRequest(int method, String mUrl, Response.Listener<byte[]> listener, Response.ErrorListener errorListener, HashMap<String, String> params, Context context, boolean isStartedInBackground) {
        super(method, mUrl, listener, errorListener, params);
        this.context = context;
        this.isStartedInBackground = isStartedInBackground;
    }

    @Override
    public void deliverError(final VolleyError error) {
        Log.d("VOLLEY_ERROR", "deliverError");

        final int status = error.networkResponse.statusCode;

        // we get a re direct url from BBC
        if(HttpURLConnection.HTTP_MOVED_PERM == status || status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_SEE_OTHER) {
            final String location = error.networkResponse.headers.get("Location");
            Log.d("VOLLEY_ERROR", "Location: " + location);
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("allow_redirect", false)==true){
                sendBroadcast(location, "DOWNLOAD_REDIRECT", status);
                return;
            }
        }
        //Other error switch back to last activity and show popup
        if(!this.isStartedInBackground) {
            sendBroadcast("none", "DOWNLOAD_VOLLEY_ERROR", status);
        }
    }

    /**
     * In case of http errors we send broadcast
     * @param redirectUrl
     */
    private void sendBroadcast(String redirectUrl, String intendFilter, int httpCode) {
        Intent intent = new Intent(intendFilter);
        switch (intendFilter){
            case "DOWNLOAD_REDIRECT":
                 //re try download if we get a redirect url as http response
                intent.putExtra("redirectUrl", redirectUrl);
                break;
            case "DOWNLOAD_VOLLEY_ERROR":
                //http error, show popup
                intent.putExtra("http_error_code", httpCode);
                break;
            default:
                break;
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
