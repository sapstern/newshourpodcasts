package org.mfri.bbc.mediamanager;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class RadioLiveService extends IntentService {



    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    private Notification notification;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        RadioLiveService getService() {
            return RadioLiveService.this;
        }
    }

    @Override
    public void onCreate() {

        mNM = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        assert mNM != null;
        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService start");
        showNotification();

        synchronized (this) {
            BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();

            utils.startRadioLive(this);
        }


        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService end");

    }


    /**
     * @deprecated
     */
    @Deprecated
    public RadioLiveService() {

        super("RadioLiveService");


    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    protected void onHandleIntent(Intent intent) {
//        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService start");
//        startForeground(NOTIFICATION,notification);
//        synchronized (intent) {
//            BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
//
//            utils.startRadioLive(this);
//        }
//
//
//        Log.d("INTENT_RADIO_LIVE", "onHandleIntent RadioLiveService end");

    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("BBC_RADIO_LIVE",
                    "BBC_RADIO_LIVE",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Listen Radio Live");
            mNM.createNotificationChannel(channel);
            //mNM.setChannelId("NOTIFICATION_ID");
        }
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ItemListActivityRadiolive.class), PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this, "BBC_RADIO_LIVE");
        // Set the info for the views that show in the notification panel.
        notification = notificationBuilder
                .setSmallIcon(R.mipmap.radio_icon_32_px)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
        //Start service as forground service (prevents stopping of the service once app is swapped out)
        startForeground(NOTIFICATION,notification);
    }

}
