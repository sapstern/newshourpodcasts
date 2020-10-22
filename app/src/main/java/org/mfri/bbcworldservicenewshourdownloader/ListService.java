package org.mfri.bbcworldservicenewshourdownloader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

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
        Utils utils = new Utils();
        Bundle theDownloadedPodcastBundle;
        try {
            theDownloadedPodcastBundle = utils.getDownloadedPodcasts();
        } catch (IOException e) {
            e.printStackTrace();
            theDownloadedPodcastBundle = null;
        }
        Bundle downloadOptionsBundle;
        while (true){
            try {
                downloadOptionsBundle = utils.getCurrentDownloadOptions();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }


        if(theDownloadedPodcastBundle!=null && downloadOptionsBundle!=null){
            int theSizeOfDownloadOptions = downloadOptionsBundle.getInt("LIST_SIZE");
            int theSizeOfDownloadedPodcasts = theDownloadedPodcastBundle.getInt("LIST_SIZE");
            int sizeAll = theSizeOfDownloadOptions;

            for (int i=0;i<theSizeOfDownloadedPodcasts;i++){
                DownloadListItem item = theDownloadedPodcastBundle.getParcelable("ITEM_"+i);
                boolean isFound = false;
                for (int j=0;j<theSizeOfDownloadOptions;j++){
                    DownloadListItem itemOptions = downloadOptionsBundle.getParcelable("ITEM_"+j);
                    if(item.fileName.equals(itemOptions.fileName)){

                            downloadOptionsBundle.remove("ITEM_" + j);
                            //Neuer ITEM, weil url final ist, somit nicht auf "none" gesetzt werden kann, also ersetzen
                            //DownloadListItem itemTemp = new DownloadListItem(String.valueOf(j), itemOptions.content, "none", itemOptions.dateOfPublication, itemOptions.fileName);
                            downloadOptionsBundle.putParcelable("ITEM_" + j, item);

                        isFound = true;
                        break;
                    }
                }
                //Nicht gefunden, anhaengen an original download optionss
                if(isFound==false){
                    sizeAll++;
                    downloadOptionsBundle.putParcelable("ITEM_"+sizeAll, item);
                }
            }
            downloadOptionsBundle.remove("LIST_SIZE");
            downloadOptionsBundle.putInt("LIST_SIZE",sizeAll);
        }

        Intent itemListIntent = new Intent(this, ItemListActivity.class);
        itemListIntent.putExtra("RESULT_LIST", downloadOptionsBundle);
        itemListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(itemListIntent);

        Log.d("HANDLE_INTENT", "onHandleIntent end");
        stopSelf();
    }




}
