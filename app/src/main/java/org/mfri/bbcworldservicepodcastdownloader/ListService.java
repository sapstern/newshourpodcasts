package org.mfri.bbcworldservicepodcastdownloader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ListService extends IntentService {

    /**
     * @deprecated
     */
    @Deprecated
    public ListService() {

        super("ListService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HANDLE_INTENT", "onHandleIntent start");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        final String theProgram = intent.getExtras().getString("theProgram");
        final int httpErrorCode = intent.getExtras().getInt("http_error_code", -999);
        BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
        Bundle downloadOptionsBundle = utils.getDownloadOptionsBundle(this, theProgram);
        Class theActivityClass;
        try {
            theActivityClass = Class.forName("org.mfri.bbcworldservicepodcastdownloader.ItemListActivity"+ StringUtils.capitalize(theProgram));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Intent itemListIntent = new Intent(this, theActivityClass);
        itemListIntent.putExtra("RESULT_LIST", downloadOptionsBundle);
        itemListIntent.putExtra("http_error_code", httpErrorCode);
        itemListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(itemListIntent);

        Log.d("HANDLE_INTENT", "onHandleIntent end");
        stopSelf();
    }




}
