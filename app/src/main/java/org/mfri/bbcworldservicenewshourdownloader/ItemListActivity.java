package org.mfri.bbcworldservicenewshourdownloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,

 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {


    Utils utils = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Log.d("CREATE", "onCreate start");
        utils = new Utils();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());



        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;

        Bundle listBundle = this.getIntent().getExtras().getBundle("RESULT_LIST");
        setupRecyclerView((RecyclerView) recyclerView, new ItemList(listBundle));
        Log.d("CREATE", "onCreate end");
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, ItemList itemList) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, itemList.ITEMS, false));
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<DownloadListItem> mValues;


        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<DownloadListItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.mContentView.setText(mValues.get(position).content);
            holder.mDateOfPublicationView.setText(mValues.get(position).dateOfPublication);
            holder.setSubmitButtonOnClickListener(position);

            holder.itemView.setTag(mValues.get(position));

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            //final TextView mIdView;
            final Button mContentView;
            public TextView mDateOfPublicationView;

            ViewHolder(View view) {
                super(view);
                mContentView = (Button) view.findViewById(R.id.content);
                mDateOfPublicationView = (TextView) view.findViewById(R.id.dateOfPublication);
            }

            public void setSubmitButtonOnClickListener(final int position) {

                Log.d("ItemListActivity", "setSubmitButtonOnClickListener("+position+")start => URL: "+mValues.get(position).url);
                if(mValues.get(position).url!=null&&mValues.get(position).url.equals("none"))
                     mContentView.setBackgroundColor(Color.BLUE);
                else {
                    if(position!=0)
                        mContentView.setBackgroundColor(Color.RED);
                }
                mContentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("DOWNLOAD_ITEM", "onClick start");
                        //Header row, no download
                        if (position == 0) {
                            return;
                        }
                        Intent theDownloadIntent = utils.prepareItemDownload(mValues.get(position),getApplicationContext(),true);
                        showNotification("BBC podcast download", "Downloading or retrieving: "+theDownloadIntent.getExtras().get("fileName"), false, null);
                        startService(theDownloadIntent);

                    }
                });

            }
        }
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String fileName = intent.getExtras().getString("fileName");
            if(fileName!= null && !fileName.equals("")) {

                String fileNameWithoutDir = intent.getExtras().getString("fileNameWithoutDir");
                showNotification("BBC podcast download", "Podcast downloaded or retrieved: "+fileName, true, fileNameWithoutDir);
                Intent mediaPlayerintent = new Intent(getApplicationContext(), MediaPlayerActivity.class);
                mediaPlayerintent.putExtra("fileNameWithoutDir", fileNameWithoutDir);
                startActivity(mediaPlayerintent);
            }



        }
    };

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("messageFromDownloadService"));
    }

    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    public void showNotification(String title, String message, boolean isIntend, String fileNameWithoutDir) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1234567",
                    "BBC_POD_DL",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Download podcasts ongoing");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "1234567")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
        if (isIntend) {
            Intent intent = new Intent(getApplicationContext(), MediaPlayerActivity.class);
            //MFRI hier noch mediaplayer activity richtig aufrufen
            intent.putExtra("fileNameWithoutDir", fileNameWithoutDir);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
        }
        mNotificationManager.notify(0, mBuilder.build());
    }

}