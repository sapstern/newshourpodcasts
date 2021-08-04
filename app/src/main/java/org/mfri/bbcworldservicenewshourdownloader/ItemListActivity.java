package org.mfri.bbcworldservicenewshourdownloader;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkManager;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.io.File;
import java.util.ArrayList;


public class ItemListActivity extends AppCompatActivity {


    private BBCWorldServiceDownloaderUtils utils = null;
    private TableLayout.LayoutParams rowParams = null;
    private TableRow.LayoutParams colParams = null;
    private ItemList theItemList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CREATE", "onCreate start");
        utils = BBCWorldServiceDownloaderUtils.getInstance();

        Bundle listBundle = this.getIntent().getExtras().getBundle("RESULT_LIST");

        //MFRI 
        theItemList = new ItemList(listBundle);
        setContentView(R.layout.activity_item_list);
        setupTableLayout(theItemList);
        //add swipe refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            Intent intent = new Intent(getApplicationContext(), ListService.class);
            getApplication().startService(intent);
            pullToRefresh.setRefreshing(false);

        });
        Toolbar myToolbar = findViewById(R.id.bbc_toolbar);
        setSupportActionBar(myToolbar);

        Log.d("CREATE", "onCreate end");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_favorite:
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("tc_installed", false)==true) {
                    Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.ghisler.android.TotalCommander");

                    String root = PreferenceManager.getDefaultSharedPreferences(this).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
                    File file = new File(root);
                    BBCWorldServiceDownloaderUtils.checkDir(file, this);
                    Uri fileURI = BBCWorldServicePodcastDownloaderFileProvider.getUriForFile(this, this.getPackageName() + ".provider", file);
                    if (intent != null) {
                        intent.setClassName("com.ghisler.android.TotalCommander", "com.ghisler.android.TotalCommander.DirBrowseActivity");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(fileURI, "resource/folder");
                        startActivity(intent);
                    }
                }
                else {
                    //Setup of implicit intend
                    Intent manageIntent = new Intent(Intent.ACTION_DEFAULT);
                    String root = PreferenceManager.getDefaultSharedPreferences(this).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
                    File file = new File(root+"/");
                    BBCWorldServiceDownloaderUtils.checkDir(file, this);
                    Uri fileURI = BBCWorldServicePodcastDownloaderFileProvider.getUriForFile(this, this.getPackageName() + ".provider", file);
                    manageIntent.setDataAndType(fileURI, "resource/folder");
                    manageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(manageIntent, "manage files"));
                }
                break;

            case R.id.action_start_settings:
                startActivity(utils.getSettingsIntend(this));
                break;
            case R.id.action_documentation:
                Intent intent_doc = new Intent(this, WebViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("page", "docu");
                intent_doc.putExtra("bundle_page", bundle);
                startActivity(intent_doc);
                break;
            case R.id.action_about:
                Intent intent_about = new Intent(this, WebViewActivity.class);
                Bundle bundle_about = new Bundle();
                bundle_about.putString("page", "about");
                intent_about.putExtra("bundle_page", bundle_about);
                startActivity(intent_about);
                break;
            case R.id.action_licenses:
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                break;
            default:
                break;
        }

        return true;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bbc_menu, menu);
        return true;
    }
    private void setupTableLayout(ItemList items) {
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
            tableLayout.addView(addRow(items.ITEMS.get(i), i));
        }

        //display the table
        ScrollView layMain = findViewById(R.id.table);
        layMain.removeAllViews();
        layMain.addView(tableLayout);
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("messageFromDownloadService"));
    }


    public TableRow addRow(DownloadListItem item, int rowNumber) {
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(this.getResources().getColor(R.color.table_background));

        tr.setLayoutParams(rowParams);

        for (int i = 0; i < 2; i++) {
            TextView tvCol = null;
            switch (i)
            {
                case 0:
                    if ( rowNumber > 0 ){
                        tvCol = setupColumn(true, item.content);
                        setSubmitButtonOnClickListener((Button)tvCol, item);
                    }
                    else
                        tvCol = setupColumn(false, item.content);
                    break;
                default:
                    tvCol = setupColumn(false, item.dateOfPublication);
                    break;
            }
            //Set background color according to the download state
            if(item.url!=null&&item.url.equals("none")&&item.content!=null&&!item.content.equals("Content"))
                tvCol.setBackgroundColor(this.getResources().getColor(R.color.aqua_marine_downloaded));
            if(item.url!=null&&!item.url.equals("none")&&item.content!=null&&!item.content.equals("Content"))
                tvCol.setBackgroundColor(this.getResources().getColor(R.color.aqua_pink_downloaded));
            tvCol.setGravity(Gravity.CENTER);
            tvCol.setPadding(3, 3, 3, 3);
            tvCol.setTextColor(this.getResources().getColor(R.color.text_black));
            tvCol.setLayoutParams(colParams);

            tr.addView(tvCol);
        }

        return tr;
    }

    private TextView  setupColumn(boolean isClickable, String theText) {
        TextView tvCol = new Button(this);
        tvCol.setClickable(isClickable);
        tvCol.setText(theText);
        tvCol.setBackgroundColor(this.getResources().getColor(R.color.row_background));
        return tvCol;
    }

    public void setSubmitButtonOnClickListener(Button button, final DownloadListItem item) {

        Log.d("ItemListActivity", "setSubmitButtonOnClickListener()start => URL: "+item.url);

        button.setOnClickListener(view -> {
            Log.d("DOWNLOAD_ITEM", "onClick start");
            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dl_background", true)==true){
                WorkManager
                        .getInstance(getApplicationContext())
                        .cancelWorkById(utils.getDownLoadRequest().getId());
            }
            Intent theDownloadIntent = utils.prepareItemDownload(item,getApplicationContext(),true, false);
            utils.showNotification("BBC podcast download", "Downloading or retrieving: "+theDownloadIntent.getExtras().get("fileName"), getApplicationContext(), (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
            startService(theDownloadIntent);
        });
    }


    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String fileName = intent.getExtras().getString("fileName");
            if(fileName!= null && !fileName.equals("")) {
                if(intent.getExtras().getBoolean("isStartedInBackground")!=true) {
                    //Setup of implicit intend
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    String root = PreferenceManager.getDefaultSharedPreferences(context).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
                    File file = new File(root+"/"+fileName);
                    Uri fileURI = BBCWorldServicePodcastDownloaderFileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                    viewIntent.setDataAndType(fileURI, "audio/*");
                    viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(viewIntent, fileName));
                    if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dl_background", true)==true) {
                        WorkManager
                                .getInstance(getApplicationContext())
                                .enqueue(utils.getDownLoadRequest());
                    }
                    return;
                }
                //should not be null anyway
                if(theItemList!=null) {
                    //Refresh the view with every downloade
                    setupTableLayout(theItemList);
                    if (findViewById(R.id.item_list)!=null)
                        findViewById(R.id.item_list).invalidate();
                }
                String fileNameWithoutDir = intent.getExtras().getString("fileNameWithoutDir");
                utils.showNotification("BBC podcast download", "Podcast downloaded or retrieved: "+fileName, context, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

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
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.d("SAVE_STATE", "onSaveInstanceState start");
        savedInstanceState.putParcelableArrayList("ITEM_LIST", (ArrayList<? extends Parcelable>) theItemList.ITEMS);
        Log.d("SAVE_STATE", "onSaveInstanceState exit");
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true); // exist app
    }

}
