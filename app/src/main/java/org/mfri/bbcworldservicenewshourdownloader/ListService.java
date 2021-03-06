package org.mfri.bbcworldservicenewshourdownloader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;


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
    public ListService() {

        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HANDLE_INTENT", "onHandleIntent start");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
        Bundle downloadOptionsBundle = utils.getDownloadOptionsBundle(this);

        Intent itemListIntent = new Intent(this, ItemListActivity.class);
        itemListIntent.putExtra("RESULT_LIST", downloadOptionsBundle);
        itemListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(itemListIntent);

        Log.d("HANDLE_INTENT", "onHandleIntent end");
        stopSelf();
    }




}
