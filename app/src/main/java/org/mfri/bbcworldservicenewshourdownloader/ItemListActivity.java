package org.mfri.bbcworldservicenewshourdownloader;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
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


    private Utils utils = null;
    private TableLayout.LayoutParams rowParams = null;
    private TableRow.LayoutParams colParams = null;
    private ScrollView layMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_item_list);
//
        Log.d("CREATE", "onCreate start");
        utils = new Utils();
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getTitle());
//
//
//
//        View recyclerView = findViewById(R.id.item_list);
//        assert recyclerView != null;

        Bundle listBundle = this.getIntent().getExtras().getBundle("RESULT_LIST");
        //setupRecyclerView((RecyclerView) recyclerView, new ItemList(listBundle));

        //MFRI neu
        //View tabView = findViewById(R.id.tableLayout1);
        ItemList items = new ItemList(listBundle);
        setContentView(R.layout.activity_item_list);
        rowParams = new TableLayout.LayoutParams();
        rowParams.setMargins(0, 0, 0, 1);
        colParams = new TableRow.LayoutParams();
        colParams.setMargins(0, 0, 1, 0);
        colParams.width = 0;
        colParams.height = TableRow.LayoutParams.FILL_PARENT;
        TableLayout tableLayout = new TableLayout(this);
        TableLayout.LayoutParams tabLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        tableLayout.setLayoutParams(tabLayoutParams);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setBackgroundColor(this.getResources().getColor(
                R.color.table_background));

        for(int i=0; i<items.ITEMS.size();i++){
            //add rows
           tableLayout.addView(addRow(items.ITEMS.get(i)));
        }

        //display the table
        layMain = (ScrollView)findViewById(R.id.table);
        layMain.removeAllViews();
        layMain.addView(tableLayout);
        Log.d("CREATE", "onCreate end");
    }



    public TableRow addRow(DownloadListItem item) {
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(this.getResources().getColor(
                R.color.table_background));

        tr.setLayoutParams(rowParams);

        for (int iCol = 0; iCol < 2; iCol++) {
            TextView tvCol = null;
            switch (iCol)
            {
                case 0:
                    tvCol = new Button(this);
                    tvCol.setText(item.content);
                    setSubmitButtonOnClickListener((Button)tvCol, item);
                    tvCol.setBackgroundColor(this.getResources().getColor(
                            R.color.row_background));
                    if(item.url!=null&&item.url.equals("none")
                            &&item.content!=null&&!item.content.equals("Content"))
                        tvCol.setBackgroundColor(Color.parseColor("#7fffd4"));
                    if(item.url!=null&&!item.url.equals("none")
                            &&item.content!=null&&!item.content.equals("Content"))
                        tvCol.setBackgroundColor(Color.RED);
                    break;
                default:
                    tvCol = new TextView(this);
                    tvCol.setText(item.dateOfPublication);
                    tvCol.setBackgroundColor(this.getResources().getColor(
                            R.color.row_background));
                    break;
            }


            tvCol.setGravity(Gravity.CENTER | Gravity.CENTER);
            tvCol.setPadding(3, 3, 3, 3);
            tvCol.setTextColor(this.getResources().getColor(
                    R.color.text_black));
            tvCol.setLayoutParams(colParams);

            tr.addView(tvCol);
        }

        return tr;
    }

    public void setSubmitButtonOnClickListener(Button button, final DownloadListItem item) {

        Log.d("ItemListActivity", "setSubmitButtonOnClickListener()start => URL: "+item.url);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DOWNLOAD_ITEM", "onClick start");


                Intent theDownloadIntent = utils.prepareItemDownload(item,getApplicationContext(),true);
                showNotification("BBC podcast download", "Downloading or retrieving: "+theDownloadIntent.getExtras().get("fileName"), false, null);
                startService(theDownloadIntent);

            }
        });


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