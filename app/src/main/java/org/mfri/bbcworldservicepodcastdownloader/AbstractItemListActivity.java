package org.mfri.bbcworldservicepodcastdownloader;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkManager;

import com.dd.CircularProgressButton;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

public abstract class AbstractItemListActivity  extends AppCompatActivity implements BBCWorldServiceDownloaderStaticValues{

    protected BBCWorldServiceDownloaderUtils utils = null;

    protected TableLayout tableLayout = null;
    protected TableLayout.LayoutParams rowParams = null;
    protected TableRow.LayoutParams colParams = null;
    protected ItemList theItemList;
    protected String theProgram = null;

    /**
     * onActivityResult has been overridden in order to refresh the table of podcasts after
     * return of the implicit intend which was fired to play the fresh downloaded podcast;
     * the requestCode = 777 has been set on startup of implicit intent
     * IMPLICIT_INTENT_START_PODCAST
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void  onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ABS_IL_ACTIVITY", "receive callback from implicit intend: " + requestCode);
        if (requestCode == 777)
            utils.startListService(this, theProgram, -1);
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
            case R.id.action_licenses:
                new LibsBuilder().withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR).withActivityTitle("Open Source Libs used by "+getString(R.string.app_name)).start(this);
                break;
            case R.id.action_about:
                Intent intent_about = new Intent(this, WebViewActivity.class);
                Bundle bundle_about = new Bundle();
                bundle_about.putString("page", "about");
                intent_about.putExtra("bundle_page", bundle_about);
                startActivity(intent_about);
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

    /**
     * Settup of table for downloadable podcasts
     * @param items
     */
    protected void setupTableLayout(ItemList items) {
        rowParams = new TableLayout.LayoutParams();
        rowParams.setMargins(0, 0, 0, 1);
        colParams = new TableRow.LayoutParams();
        colParams.setMargins(0, 0, 1, 0);
        colParams.width = 0;
        colParams.height = TableRow.LayoutParams.FILL_PARENT;
        tableLayout = new TableLayout(this);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("IMPLICIT_INTENT_START_PODCAST"));
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("DOWNLOAD_VOLLEY_ERROR"));
    }

    /**
     * Creates table ror
     * @param item
     * @param rowNumber
     * @return
     */
    private TableRow addRow(DownloadListItem item, int rowNumber) {
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
                        setSubmitButtonOnClickListener((CircularProgressButton)tvCol, item, theProgram, rowNumber);
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

    /**
     * Creates Button for each row/column
     * @param isClickable
     * @param theText
     * @return
     */
    private TextView  setupColumn(boolean isClickable, String theText) {
        TextView tvCol = new CircularProgressButton(this);
        tvCol.setClickable(isClickable);
        tvCol.setText(theText);
        tvCol.setBackgroundColor(this.getResources().getColor(R.color.row_background));
        return tvCol;
    }

    /**
     * Implements the button behavior
     * @param button
     * @param item
     * @param theProgram
     * @param rowNumber
     */
    private void setSubmitButtonOnClickListener(CircularProgressButton button, final DownloadListItem item, String theProgram, int rowNumber) {

        Log.d("ItemListActivityNews", "setSubmitButtonOnClickListener()start => URL: "+item.url);

        button.setId(rowNumber);
        Log.d("ItemListActivity", "setSubmitButtonOnClickListener() ID of button: "+button.getId());
        button.setOnClickListener(view -> {
            Log.d("DOWNLOAD_ITEM", "onClick start");
            button.setText(getResources().getString(R.string.download_state));


            //refreshTable();
            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dl_background", true)==true){
                WorkManager
                        .getInstance(getApplicationContext())
                        .cancelWorkById(utils.getDownLoadRequest(theProgram).getId());
            }
            Intent theDownloadIntent = utils.prepareItemDownload(item,getApplicationContext(),true, false, theProgram, rowNumber);

            utils.showNotification("BBC podcast download", "Downloading or retrieving: "+theDownloadIntent.getExtras().get("fileName"), getApplicationContext(), (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
            startService(theDownloadIntent);



        });
    }

    /**
     * Tis receiver gets called on return of successful download in class DownloadService
     * as well as on error handling of any http error inside the Volley call
     */
    protected final  BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){
                case "IMPLICIT_INTENT_START_PODCAST":
                    String fileName = intent.getExtras().getString("fileName");
                    if(fileName!= null && !fileName.equals("")) {
                        if(intent.getExtras().getBoolean("isStartedInBackground")!=true) {


                            //Setup of implicit intend to play the podcast after download
                            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                            String root = PreferenceManager.getDefaultSharedPreferences(context).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
                            File file = new File(root+"/"+theProgram+"/"+fileName);
                            Uri fileURI = BBCWorldServicePodcastDownloaderFileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                            viewIntent.setDataAndType(fileURI, "audio/*");
                            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(Intent.createChooser(viewIntent, fileName), 777);
                            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dl_background", true)==true) {
                                WorkManager
                                        .getInstance(getApplicationContext())
                                        .enqueue(utils.getDownLoadRequest(theProgram));
                            }
                            return;
                        }
                        //should not be null anyway
                        //refreshTable();

                        utils.showNotification("BBC podcast download", "Podcast downloaded or retrieved: "+fileName, context, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

                    }
                    break;
                case "DOWNLOAD_VOLLEY_ERROR":
                    //http error, show popup if not in background
                    utils.startListService(context, theProgram, intent.getExtras().getInt("http_error_code"));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Method for display of http error popup
     * @param context
     */
    protected void showPopup(Context context){
        int httpCode = this.getIntent().getIntExtra("http_error_code", -777);
        Log.d("ABS_IL_ACTIVITY", "showPopup http error code: " + httpCode);
        if(httpCode <= 0) {
            return;
        }
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setTitle("Error downloading");
        dlgAlert.setPositiveButton("Ok",
                (dialog, which) -> {
                    //dismiss the dialog
                });
        dlgAlert.setMessage("http "+httpCode+" "+HTTP_STATUS_MAP.get(String.valueOf(httpCode)));
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    /**
     * Refresh the table layout
     */
    private void refreshTable() {
        if(theItemList!=null) {
            //Refresh the view with every download
            setupTableLayout(theItemList);
            if (findViewById(R.id.item_list)!=null)
                findViewById(R.id.item_list).invalidate();
        }
    }

    /**
     * Generic setup of layout for all program activities
     * @param theProgram
     */
    protected void setupLayout(String theProgram) {

        this.theProgram = theProgram;
        utils = BBCWorldServiceDownloaderUtils.getInstance();
        Bundle listBundle = this.getIntent().getExtras().getBundle("RESULT_LIST");

        theItemList = new ItemList(listBundle);
        setContentView(R.layout.activity_item_list);
        setupTableLayout(theItemList);

        //add swipe refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            utils.startListService(this, theProgram, -1);
            pullToRefresh.setRefreshing(false);

        });
        tableLayout.getViewTreeObserver().addOnScrollChangedListener(() -> {
            ScrollView layMain = findViewById(R.id.table);
            Log.d("SCROLL", "onScrollChanged start Y: "+layMain.getScrollY());
            if (layMain.getScrollY() == 0)
                pullToRefresh.setEnabled(true);
            else
                pullToRefresh.setEnabled(false);
        });
        Toolbar myToolbar = findViewById(R.id.bbc_toolbar);
        setSupportActionBar(myToolbar);
        Spinner theSpinner = findViewById(R.id.spinner_nav);

        ArrayAdapter<String> bbcProgramsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.bbc_programs));
        bbcProgramsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        theSpinner.setAdapter(bbcProgramsAdapter);
        theSpinner.setSelection(bbcProgramsAdapter.getPosition(INVERSE_PROGRAM_TITLES_MAP.get(theProgram)));
        theSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (theSpinner.getSelectedItem().toString() != null && !theSpinner.getSelectedItem().toString().equals(INVERSE_PROGRAM_TITLES_MAP.get(theProgram))) {
                    utils.startListService(getApplicationContext(), PROGRAM_TITLES_MAP.get(theSpinner.getSelectedItem().toString()), -1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_LONG).show();

            }
        });

        //Only show Popup on http error
        showPopup(this);
    }

}
